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
package app.service.internal;

import app.config.LsPushProperties;
import app.data.validator.UserInfoValidator;
import app.service.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.concurrent.ExecutionException;

public class MockCaptchaService extends CaptchaService {
    private String captcha;

    public MockCaptchaService(UserInfoValidator userInfoValidator, LsPushProperties lsPushProperties,
        JavaMailSender mailSender,
        TemplateEngine templateEngine, ObjectMapper objectMapper) {
        super(userInfoValidator, lsPushProperties, mailSender, templateEngine, objectMapper);
    }

    @Override
    protected void sendEmail(String email, String authCode) {

    }

    @Override
    protected void sendSMS(String phone, String region, String authCode) {

    }

    @Override
    protected String generateCaptcha(int length) throws ExecutionException {
        captcha = super.generateCaptcha(length);
        return captcha;
    }

    public String getCaptcha() {
        return captcha;
    }
}
