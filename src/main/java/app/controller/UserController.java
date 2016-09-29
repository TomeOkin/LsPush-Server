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
package app.controller;

import app.config.ResultCode;
import app.data.model.AccessResponse;
import app.data.model.BaseResponse;
import app.data.model.CaptchaRequest;
import app.data.model.CryptoToken;
import app.data.model.internal.FreshEvent;
import app.service.AuthService;
import app.service.CaptchaService;
import app.service.FreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final CaptchaService captchaService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(CaptchaService captchaService, AuthService authService) {
        this.captchaService = captchaService;
        this.authService = authService;
    }

    //    @Autowired RedisTemplate<String, Collection> template;
    @Autowired JmsTemplate jmsTemplate;

    @GetMapping("")
    public String hello() {
        FreshEvent event = new FreshEvent();
        event.event = FreshEvent.EVENT_COLLECTION;
        event.colId = 101;
        jmsTemplate.convertAndSend(FreshService.DESTINATION, event);
        //        logger.info("send collection");
        //        template.convertAndSend("collection", new Collection(null, null, "hello", null));
        return "hello person!";
    }

    @PostMapping("/sendCaptcha")
    public BaseResponse sendCaptcha(@RequestBody CaptchaRequest request) {
        logger.info("----------sendCaptcha request access-------------");
        int result = captchaService.sendAuthCode(request);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return new BaseResponse();
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Send Captcha Failure");
        return new BaseResponse(result, description);
    }

    @PostMapping("/checkCaptcha")
    public BaseResponse checkCaptcha(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------checkCaptcha request access-------------");

        int result = captchaService.checkCaptcha(cryptoToken);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return new BaseResponse();
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Check Captcha Failure");
        return new BaseResponse(result, description);
    }

    @PostMapping("/register")
    public AccessResponse register(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------register request access-------------");
        AccessResponse accessResponse = new AccessResponse();
        int result = authService.register(cryptoToken, accessResponse);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return accessResponse;
        }

        accessResponse.setResultCode(result);
        String description = ResultCode.errorCode.getOrDefault(result, "Register Failure");
        accessResponse.setResult(description);
        return accessResponse;
    }

    @PostMapping("/refreshExpireToken")
    public AccessResponse refreshExpireToken(@RequestBody CryptoToken refreshToken) {
        logger.info("----------refreshExpireToken request access-------------");
        AccessResponse accessResponse = new AccessResponse();
        int result = authService.refreshExpireToken(refreshToken, accessResponse);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return accessResponse;
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Refresh Expire Token Failure");
        accessResponse.setResult(description);
        return accessResponse;
    }

    @PostMapping("/refreshRefreshToken")
    public AccessResponse refreshRefreshToken(@RequestBody CryptoToken refreshToken) {
        logger.info("----------refreshRefreshToken request access-------------");
        AccessResponse accessResponse = new AccessResponse();
        int result = authService.refreshRefreshToken(refreshToken, accessResponse);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return accessResponse;
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Refresh Refresh Token Failure");
        accessResponse.setResult(description);
        return accessResponse;
    }

    @PostMapping("/login")
    public AccessResponse login(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------login request access-------------");
        AccessResponse accessResponse = new AccessResponse();
        int result = authService.login(cryptoToken, accessResponse);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return accessResponse;
        }

        accessResponse.setResultCode(result);
        String description = ResultCode.errorCode.getOrDefault(result, "Login Failure");
        accessResponse.setResult(description);
        return accessResponse;
    }

    @GetMapping("/checkUIDExisted/{uid:.*}")
    public BaseResponse checkUIDExisted(@PathVariable String uid) {
        logger.info("----------checkUIDExisted request access-------------");
        BaseResponse response = new BaseResponse();
        if (authService.isExistUser(uid)) {
            response.setResultCode(ResultCode.UID_EXISTED);
            response.setResult(ResultCode.errorCode.get(ResultCode.UID_EXISTED));
        }
        return response;
    }
}
