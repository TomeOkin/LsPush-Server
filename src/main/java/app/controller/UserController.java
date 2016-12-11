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
import app.data.model.*;
import app.service.AuthService;
import app.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    //    @Autowired JmsTemplate jmsTemplate;

    @GetMapping("")
    public String hello() {
        //        FreshEvent event = new FreshEvent();
        //        event.event = FreshEvent.EVENT_COLLECTION;
        //        event.colId = 101;
        //        jmsTemplate.convertAndSend(FreshService.DESTINATION, event);
        return "hello person!";
    }

    @PostMapping("/sendCaptcha")
    public BaseResponse sendCaptcha(@RequestBody CaptchaRequest request) {
        logger.info("----------sendCaptcha request access-------------");

        int result = captchaService.sendAuthCode(request);
        return ResultCode.get(result, "Send Captcha Failed", null);
    }

    @PostMapping("/checkCaptcha")
    public BaseResponse checkCaptcha(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------checkCaptcha request access-------------");

        int result = captchaService.checkCaptcha(cryptoToken);
        return ResultCode.get(result, "Check Captcha Failed", cryptoToken);
    }

    @PostMapping("/register")
    public Response<AccessResponse> register(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------register request access-------------");

        AccessResponse accessResponse = new AccessResponse();
        int result = authService.register(cryptoToken, accessResponse);
        return ResultCode.get(result, "Register Failed", accessResponse);
    }

    @PostMapping("/refreshExpireToken")
    public Response<AccessResponse> refreshExpireToken(@RequestBody CryptoToken refreshToken) {
        logger.info("----------refreshExpireToken request access-------------");

        AccessResponse accessResponse = new AccessResponse();
        int result = authService.refreshExpireToken(refreshToken, accessResponse);
        return ResultCode.get(result, "Refresh Expire Token Failed", accessResponse);
    }

    @PostMapping("/refreshRefreshToken")
    public Response<AccessResponse> refreshRefreshToken(@RequestBody CryptoToken refreshToken) {
        logger.info("----------refreshRefreshToken request access-------------");

        AccessResponse accessResponse = new AccessResponse();
        int result = authService.refreshRefreshToken(refreshToken, accessResponse);
        return ResultCode.get(result, "Refresh Refresh Token Failed", accessResponse);
    }

    @PostMapping("/login")
    public Response<AccessResponse> login(@RequestBody CryptoToken cryptoToken) {
        logger.info("----------login request access-------------");

        AccessResponse accessResponse = new AccessResponse();
        int result = authService.login(cryptoToken, accessResponse);
        return ResultCode.get(result, "Login Failed", accessResponse);
    }

    @GetMapping("/checkUIDExisted/{uid:.*}")
    public BaseResponse checkUIDExisted(@PathVariable String uid) {
        logger.info("----------checkUIDExisted request access-------------");

        int result = authService.isExistUser(uid) ? ResultCode.UID_EXISTED : BaseResponse.COMMON_SUCCESS;
        return ResultCode.get(result, "Uid has existed", null);
    }
}
