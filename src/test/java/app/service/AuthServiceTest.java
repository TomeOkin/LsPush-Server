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

import app.App;
import app.config.LsPushProperties;
import app.data.crypt.Crypto;
import app.data.local.UserRepository;
import app.data.model.*;
import app.data.validator.UserInfoValidator;
import app.service.internal.MockCaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.TemplateEngine;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class AuthServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceTest.class);
    @Autowired LsPushProperties lsPushProperties;
    @Autowired JavaMailSender mailSender;
    @Autowired TemplateEngine templateEngine;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserInfoValidator userInfoValidator;
    @Autowired UserRepository userRepository;

    @Test
    public void checkExpireToken() {
        MockCaptchaService captchaService =
            new MockCaptchaService(userInfoValidator, lsPushProperties, mailSender, templateEngine, objectMapper,
                userRepository);
        AuthService authService =
            new AuthService(objectMapper, captchaService, userInfoValidator, userRepository);

        // 服务器端生成验证码
        CaptchaRequest request = new CaptchaRequest();
        request.setSendObject("123@abc.cn");
        int result = captchaService.sendAuthCode(request);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 客户端注册用户
        RegisterData registerData = new RegisterData();
        registerData.setUserId("joo");
        registerData.setNickname("John");
        registerData.setPassword("john123456");
        registerData.setAuthCode(captchaService.getCaptcha());
        registerData.setCaptchaRequest(request); // 别写漏

        CryptoToken registerToken = null;
        try {
            byte[] registerDataString = objectMapper.writeValueAsBytes(registerData);
            registerToken = Crypto.encrypt(registerDataString);
        } catch (Exception e) {
            logger.warn("encrypt register data failure", e);
        }
        Assert.assertNotNull(registerToken);

        // pre check captcha
        result = captchaService.checkCaptcha(registerToken);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 服务器端注册用户
        AccessResponse accessResponse = new AccessResponse();
        result = authService.register(registerToken, accessResponse);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 服务器端通过 expire token 进行身份验证
        result = authService.checkIfAuth(accessResponse.getExpireToken());
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 客户端通过 refresh token 刷新 expire token
        result = authService.refreshExpireToken(accessResponse.getRefreshToken(), accessResponse);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 验证新 expire token
        result = authService.checkIfAuth(accessResponse.getExpireToken());
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 客户端通过 refresh data 构造 refresh token 以刷新服务器端生成的 refresh token
        RefreshData refreshData = new RefreshData();
        refreshData.setUserId(accessResponse.getUser().getUid());
        refreshData.setRefreshToken(accessResponse.getRefreshToken());
        CryptoToken refreshToken = null;
        try {
            byte[] refreshDataString = objectMapper.writeValueAsBytes(refreshData);
            refreshToken = Crypto.encrypt(refreshDataString);
        } catch (Exception e) {
            logger.warn("encrypt register data failure", e);
        }
        Assert.assertNotNull(refreshToken);

        // 服务器刷新 refresh token
        result = authService.refreshRefreshToken(refreshToken, accessResponse);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 验证新生成的 expire token
        result = authService.checkIfAuth(accessResponse.getExpireToken());
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 登录测试
        LoginData loginData = new LoginData();
        loginData.setUid(accessResponse.getUser().getUid());
        loginData.setPassword(registerData.getPassword());
        CryptoToken loginToken = null;
        try {
            byte[] loginDataString = objectMapper.writeValueAsBytes(loginData);
            loginToken = Crypto.encrypt(loginDataString);
        } catch (Exception e) {
            logger.warn("encrypt login data failure", e);
        }
        Assert.assertNotNull(loginToken);

        result = authService.login(loginToken, accessResponse);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);

        // 还原 user 表
        userRepository.delete(loginData.getUid());
    }
}
