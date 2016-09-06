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

import app.App;
import app.data.crypt.Crypto;
import app.data.model.CryptoToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class CryptoTest {
    private static final Logger logger = LoggerFactory.getLogger(CryptoTest.class);

    @Test
    public void test()
        throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
        BadPaddingException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        CryptoToken token = new CryptoToken();
        token.key = "py/yIGKe+t0GLIu1GB0TWMABx++N4r/mhbk0yF/OXD52+PB9yeaAgjlru+KN2erLGW12AEvVHQoNL1e0XGT1pQ==";
        token.param = "r24h0tKb+54YgvfcontZ6Q==";
        token.value = "XahMWrPYGDq/0f9tf2d7Nw==";
        byte[] data = Crypto.decrypt(token);
        logger.info(new String(data));
    }

}
