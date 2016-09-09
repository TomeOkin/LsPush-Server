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
import app.data.crypt.Crypto;
import app.data.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class CaptchaServiceTest {
    @Autowired ObjectMapper objectMapper;
    @Autowired CaptchaService captchaService;
    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceTest.class);

    @Test
    public void sendEmail() {
        CaptchaRequest request = new CaptchaRequest();
        request.setSendObject("2560034435@qq.com");
        captchaService.sendAuthCode(request);
    }

    @Test
    public void checkPhoneCaptcha()
        throws JsonProcessingException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
        NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        CaptchaRequest request = new CaptchaRequest();
        request.setSendObject("15814943475");
        request.setRegion("CN");
        RegisterData registerData = new RegisterData();
        registerData.setCaptchaRequest(request);
        registerData.setAuthCode("1234");
        String json = objectMapper.writeValueAsString(registerData);
        CryptoToken token = Crypto.encrypt(json);
        logger.info("key: {}", token.key);
        logger.info("value: {}", token.value);
        int result = captchaService.checkCaptcha(token);
        Assert.assertEquals(BaseResponse.COMMON_SUCCESS, result);
    }
}
