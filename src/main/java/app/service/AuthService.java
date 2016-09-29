/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.service;

import app.data.crypt.Crypto;
import app.data.local.UserRepository;
import app.data.model.*;
import app.data.model.internal.AccountSession;
import app.data.validator.UserInfoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static app.config.ResultCode.*;

@Service
public class AuthService {
    private static final int EXPIRE_TIME = 24; // hours
    private static final int REFRESH_TIME = 8; // days

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final ObjectMapper mObjectMapper;
    private final CaptchaService mCaptchaService;
    private final UserInfoValidator mUserInfoValidator;
    private final UserRepository mUserRepo;
    private final Funnel<AccountSession> mExpireSessionFunnel;
    private final Funnel<AccountSession> mRefreshSessionFunnel;

    @Autowired
    public AuthService(ObjectMapper objectMapper, CaptchaService captchaService, UserInfoValidator userInfoValidator,
        UserRepository userRepo) {
        mObjectMapper = objectMapper;
        mCaptchaService = captchaService;
        mUserInfoValidator = userInfoValidator;
        mUserRepo = userRepo;

        mExpireSessionFunnel =
            (Funnel<AccountSession>) (from, into) -> into.putString(from.getUserId(), StandardCharsets.UTF_8)
                .putLong(from.getExpireTime());
        mRefreshSessionFunnel =
            (Funnel<AccountSession>) (from, into) -> into.putString(from.getUserId(), StandardCharsets.UTF_8)
                .putLong(from.getRefreshTime());
    }

    /**
     * for take full advantage of cache, use findOne instead of exists
     */
    public boolean isExistUser(String uid) {
        return mUserRepo.findOne(uid) != null;
    }

    /**
     * 注册需要使用邮箱或者手机号，将会返回口令、口令有效时间、用户基本信息
     */
    public int register(CryptoToken cryptoToken, AccessResponse accessResponse) {
        RegisterData registerData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            registerData = mObjectMapper.readValue(json, RegisterData.class);
        } catch (Exception e) {
            logger.warn("decrypt register crypt-token failure", e);
            return INVALID_TOKEN;
        }

        // 如果用户验证码验证通过，而 uid 或者密码没有验证通过，验证码就会失效，为了避免这个问题
        // 同时保证验证码先验证，使用预验证方式，不清除验证码
        CaptchaRequest captchaRequest = registerData.getCaptchaRequest();
        int result = mCaptchaService.checkCaptcha(captchaRequest, registerData.getAuthCode(), false);
        if (result != BaseResponse.COMMON_SUCCESS) {
            return result;
        }

        // check user info
        boolean check = mUserInfoValidator.checkUserId(registerData.getUserId());
        check = check && mUserInfoValidator.checkPasswordStrengthBaseline(registerData.getPassword());
        if (!check) {
            return ILLEGAL_ACCOUNT_INFO;
        }

        // add user to db
        if (isExistUser(registerData.getUserId())) {
            return UID_EXISTED;
        }

        User user = new User();
        user.setUid(registerData.getUserId());
        user.setNickname(registerData.getNickname());
        if (captchaRequest.getSendObject().contains("@")) {
            user.setEmail(captchaRequest.getSendObject());
            user.setValidate(User.EMAIL_VALID);
        } else {
            user.setPhone(captchaRequest.getSendObject());
            user.setRegion(captchaRequest.getRegion());
            user.setValidate(User.PHONE_VALID);
        }
        user.setPassword(registerData.getPassword());
        user.setImage(registerData.getImage());
        try {
            mUserRepo.save(user);
        } catch (Exception e) {
            logger.warn("update user info failure", e);
            return UPDATE_USER_INFO_FAILURE;
        }

        // 在服务器端已经将数据记录到数据库后，不关心结果地清除验证码记录
        mCaptchaService.checkCaptcha(captchaRequest, registerData.getAuthCode());

        accessResponse.setUser(user.cloneSelfPublic());
        return refreshAccessResponse(registerData.getUserId(), accessResponse);
    }

    /**
     * 登录只允许使用 uid 方式进行登录，将会返回口令、口令有效时间、用户基本信息
     */
    public int login(CryptoToken cryptoToken, AccessResponse accessResponse) {
        LoginData loginData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            loginData = mObjectMapper.readValue(json, LoginData.class);
        } catch (Exception e) {
            logger.warn("decrypt register crypt-token failure", e);
            return INVALID_TOKEN;
        }

        boolean check = mUserInfoValidator.checkUserId(loginData.getUid())
            && mUserInfoValidator.checkPasswordStrengthBaseline(loginData.getPassword());
        if (!check) {
            return ILLEGAL_ACCOUNT_INFO;
        }

        User user;
        try {
            user = mUserRepo.findOne(loginData.getUid());
            check = user != null && loginData.getUid().equals(user.getUid()) && loginData.getPassword()
                .equals(user.getPassword());
            if (!check) {
                return ACCOUNT_NOT_MATCH;
            }
            accessResponse.setUser(user.cloneSelfPublic());
        } catch (Exception e) {
            logger.warn("query user failure", e);
            return QUERY_USER_FAILURE;
        }

        // 检查通过，创建用户口令并返回
        return refreshAccessResponse(user.getUid(), accessResponse);
    }

    public int checkIfAuth(CryptoToken cryptoToken) {
        AccountSession expireSession = getSessionWithCheck(cryptoToken, true);
        if (expireSession == null) {
            return INVALID_TOKEN;
        }

        DateTime now = DateTime.now();
        DateTime deadline = new DateTime(expireSession.getExpireTime());
        if (now.isBefore(deadline) || now.isEqual(deadline)) {
            return BaseResponse.COMMON_SUCCESS;
        }

        return OVERDUE_TOKEN;
    }

    public String checkIfAuthBind(CryptoToken cryptoToken) {
        AccountSession expireSession = getSessionWithCheck(cryptoToken, true);
        if (expireSession == null) {
            return null;
        }

        DateTime now = DateTime.now();
        DateTime deadline = new DateTime(expireSession.getExpireTime());
        if (now.isBefore(deadline) || now.isEqual(deadline)) {
            return expireSession.getUserId();
        }

        return null;
    }

    public int refreshExpireToken(CryptoToken cryptoToken, AccessResponse accessResponse) {
        AccountSession refreshSession = getSessionWithCheck(cryptoToken, false);
        if (refreshSession == null) {
            return INVALID_TOKEN;
        }

        // 验证 refreshToken
        DateTime now = DateTime.now();
        DateTime deadline = new DateTime(refreshSession.getRefreshTime());
        if (now.isAfter(deadline)) {
            return OVERDUE_TOKEN;
        }

        // 刷新 expireToken
        final long expireTime = now.plusHours(EXPIRE_TIME).getMillis();
        refreshSession.setExpireTime(expireTime);
        CryptoToken expireToken = newSessionToken(refreshSession, true);
        if (expireToken == null) {
            return ENCRYPT_TOKEN_FAILURE;
        }

        accessResponse.setExpireTime(expireTime);
        accessResponse.setExpireToken(expireToken);
        return BaseResponse.COMMON_SUCCESS;
    }

    public int refreshRefreshToken(CryptoToken cryptoToken, AccessResponse accessResponse) {
        RefreshData refreshData;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            refreshData = mObjectMapper.readValue(json, RefreshData.class);
        } catch (Exception e) {
            logger.warn("decrypt refresh crypt-token failure", e);
            return INVALID_TOKEN;
        }

        AccountSession old = getSessionWithCheck(refreshData.getRefreshToken(), false);
        if (old == null || !old.getUserId().equals(refreshData.getUserId())) {
            return INVALID_TOKEN;
        }

        return refreshAccessResponse(refreshData.getUserId(), accessResponse);
    }

    protected int refreshAccessResponse(String userId, AccessResponse accessResponse) {
        DateTime now = DateTime.now();
        final long expireTime = now.plusHours(EXPIRE_TIME).getMillis();
        final long refreshTime = now.plusDays(REFRESH_TIME).getMillis();

        AccountSession session = new AccountSession();
        session.setUserId(userId);
        session.setExpireTime(expireTime);
        session.setRefreshTime(refreshTime);

        CryptoToken expireToken = newSessionToken(session, true);
        if (expireToken == null) {
            return ENCRYPT_TOKEN_FAILURE;
        }

        CryptoToken refreshToken = newSessionToken(session, false);
        if (refreshToken == null) {
            return ENCRYPT_TOKEN_FAILURE;
        }

        accessResponse.setExpireTime(expireTime);
        accessResponse.setExpireToken(expireToken);
        accessResponse.setRefreshTime(refreshTime);
        accessResponse.setRefreshToken(refreshToken);
        return BaseResponse.COMMON_SUCCESS;
    }

    protected CryptoToken newSessionToken(AccountSession session, boolean isExpire) {
        Funnel<AccountSession> sessionFunnel = isExpire ? mExpireSessionFunnel : mRefreshSessionFunnel;
        byte[] data = Hashing.sipHash24().hashObject(session, sessionFunnel).asBytes();
        String sessionString = new String(data, Charset.forName("UTF-8"));
        session.setSession(sessionString);
        CryptoToken token;
        try {
            byte[] sessionData = mObjectMapper.writeValueAsBytes(session);
            token = Crypto.encrypt(sessionData);
        } catch (Exception e) {
            logger.warn("encrypt session failure", e);
            return null;
        }
        return token;
    }

    protected AccountSession getSessionWithCheck(CryptoToken cryptoToken, boolean isExpire) {
        Funnel<AccountSession> sessionFunnel = isExpire ? mExpireSessionFunnel : mRefreshSessionFunnel;
        AccountSession session;
        try {
            byte[] json = Crypto.decrypt(cryptoToken);
            session = mObjectMapper.readValue(json, AccountSession.class);
        } catch (Exception e) {
            logger.warn("decrypt token failure", e);
            return null;
        }

        byte[] sessionData = Hashing.sipHash24().hashObject(session, sessionFunnel).asBytes();
        String sessionString = new String(sessionData, StandardCharsets.UTF_8);
        if (!sessionString.equals(session.getSession())) {
            logger.warn("session not equal");
            return null;
        }

        return session;
    }
}