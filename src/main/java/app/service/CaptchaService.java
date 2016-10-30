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

import app.config.LsPushProperties;
import app.config.ResultCode;
import app.data.crypt.Crypto;
import app.data.local.UserRepository;
import app.data.model.BaseResponse;
import app.data.model.CaptchaRequest;
import app.data.model.CryptoToken;
import app.data.model.RegisterData;
import app.data.model.internal.Captcha;
import app.data.validator.UserInfoValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static app.config.ResultCode.*;

/**
 * 验证码服务
 */
@Service
public class CaptchaService {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);

    private final String serverName;
    private final String serverUrl;
    private final String serverEmail;

    private final UserInfoValidator mUserInfoValidator;
    private final JavaMailSender mMailSender;
    private final TemplateEngine mTemplateEngine;
    private final UserRepository mUserRepo;

    private final ObjectMapper mObjectMapper;
    private final Cache<CaptchaRequest, Captcha> mAuthCodeMap;
    private final Funnel<String> mStringFunnel;
    private BloomFilter<String> mAuthCodeFilter;

    @Autowired
    public CaptchaService(UserInfoValidator userInfoValidator, LsPushProperties lsPushProperties,
        JavaMailSender mailSender, TemplateEngine templateEngine, ObjectMapper objectMapper,
        UserRepository userRepo) {
        mUserInfoValidator = userInfoValidator;
        serverName = lsPushProperties.getServerName();
        serverUrl = lsPushProperties.getServerUrl();
        serverEmail = lsPushProperties.getServerEmail();
        mMailSender = mailSender;
        mTemplateEngine = templateEngine;
        mObjectMapper = objectMapper;
        mUserRepo = userRepo;
        mAuthCodeMap = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

        mStringFunnel = (Funnel<String>) (from, into) -> into.putString(from, StandardCharsets.UTF_8);
        resetBloomFilter();
    }

    public int sendAuthCode(final CaptchaRequest request) {
        // 合法检查
        String sendObject = request.getSendObject();
        if (StringUtils.isEmpty(sendObject)) {
            return INVALID_CAPTCHA;
        }

        boolean isValid;
        if (sendObject.contains("@")) {
            isValid = mUserInfoValidator.isEmailValid(sendObject) && mUserRepo.findFirstByEmail(sendObject) == null;
        } else {
            isValid = mUserInfoValidator.isPhoneValid(sendObject, request.getRegion());
        }
        if (!isValid) {
            return INVALID_CAPTCHA;
        }
        final Captcha found = mAuthCodeMap.getIfPresent(request);
        if (found != null) {
            // 如果两次发送间隔少于 1 分钟，就拒绝发送
            if (System.currentTimeMillis() - found.lastSentTime < 60_000) {
                return TIME_INTERVAL_TOO_CLOSE;
            }
        }

        try {
            Captcha captcha = new Captcha();
            captcha.authCode = generateCaptcha(6);
            captcha.lastSentTime = System.currentTimeMillis();
            mAuthCodeMap.put(request, captcha);

            if (request.getSendObject().contains("@")) {
                sendEmail(request.getSendObject(), captcha.authCode);
            } else {
                sendSMS(request.getSendObject(), request.getRegion(), captcha.authCode);
            }
        } catch (Exception e) {
            logger.error("send auth code failure", e);
            return SEND_CAPTCHA_FAILURE;
        }

        return BaseResponse.COMMON_SUCCESS;
    }

    public int checkCaptcha(CryptoToken cryptToken) {
        RegisterData registerData;
        try {
            byte[] json = Crypto.decrypt(cryptToken);
            registerData = mObjectMapper.readValue(json, RegisterData.class);
        } catch (Exception e) {
            logger.warn("decrypt register-base check-captcha crypt-token failure", e);
            return INVALID_TOKEN;
        }

        CaptchaRequest captchaRequest = registerData.getCaptchaRequest();
        return checkCaptcha(captchaRequest, registerData.getAuthCode(), false);
    }

    public int checkCaptcha(CaptchaRequest request, String authCode) {
        return checkCaptcha(request, authCode, true);
    }

    /**
     * @param successWithInvalidate provide it for pre-check captcha
     */
    public int checkCaptcha(CaptchaRequest request, String authCode, boolean successWithInvalidate) {
        // phone captcha is not send by server,
        // when parse request success and satisfy follow conditions, assume it is correct
        if (!request.getSendObject().contains("@")
            && mUserInfoValidator.isPhoneValid(request.getSendObject(), request.getRegion())
            && StringUtils.isNotEmpty(authCode)
            && authCode.length() >= 4
            && CharMatcher.digit().negate().matchesNoneOf(authCode)) {
            return BaseResponse.COMMON_SUCCESS;
        }

        Captcha found = mAuthCodeMap.getIfPresent(request);
        if (found != null) {
            if (found.accessTimes < 3) {
                if (found.authCode.equals(authCode)) {
                    if (successWithInvalidate) {
                        // when auth success, it will discards the key
                        mAuthCodeMap.invalidate(request);
                    }
                    return BaseResponse.COMMON_SUCCESS;
                } else {
                    found.accessTimes++;
                    return ResultCode.MATCHING_FAILURE;
                }
            }

            // when auth failure beyond 3 times, you can only auth success until the key is invalidate.
            mAuthCodeMap.put(request, found); // refresh key alive
            return TRYING_TOO_MUCH;
        }

        // not found or try beyond 3 times
        return MATCHING_FAILURE;
    }

    protected void sendEmail(String email, String authCode) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setSubject("欢迎加入 LsPush 大家庭");
        helper.setFrom(serverEmail, serverName);
        helper.setTo(email);

        String authLink = String.format("%s/user/auth?auth_code=%s", serverUrl, authCode);
        final Context ctx = new Context(Locale.CHINA);
        ctx.setVariable("serverUrl", serverUrl);
        ctx.setVariable("serverName", serverName);
        ctx.setVariable("email", email);
        ctx.setVariable("authCode", authCode);
        ctx.setVariable("authLink", authLink);

        String html = mTemplateEngine.process("lspush_captcha_email", ctx);

        helper.setText(html, true);
        mMailSender.send(mimeMessage);
    }

    protected void sendSMS(String phone, String region, String authCode) {

    }

    private void resetBloomFilter() {
        // 生成一个六位的数字字符串时，每 200 个独特的选择中只选择 1 个，一方面用于减少重复率，另一方面避免被推断出生成的数字
        mAuthCodeFilter = BloomFilter.create(mStringFunnel, 5000, 0.01);
    }

    protected String generateCaptcha(int length) throws ExecutionException {
        if (length <= 4) {
            length = 4;
        }

        String authCode;
        authCode = RandomStringUtils.random(length, false, true);
        // according to test, conflict is less than 45 per 10,000.
        if (mAuthCodeFilter.mightContain(authCode)) {
            if (mAuthCodeFilter.expectedFpp() >= 0.01f) { // has put too much (beyond 5000) into it
                resetBloomFilter();
            }
            authCode = RandomStringUtils.random(length, false, true);
        }
        mAuthCodeFilter.put(authCode); // mask authCode

        return authCode;
    }
}
