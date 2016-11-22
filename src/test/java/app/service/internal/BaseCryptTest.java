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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RunWith(JUnit4.class)
public class BaseCryptTest {

    @Test
    public void keyCode() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
        NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // generate key and format it
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyPairGen.initialize(512, random);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKey = Base64.getEncoder().encode(keyPair.getPublic().getEncoded());
        byte[] privateKey = Base64.getEncoder().encode(keyPair.getPrivate().getEncoded());
        System.out.println(new String(publicKey));
        System.out.println(new String(privateKey));

        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
        PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);

        Assert.assertEquals(pubKey, keyPair.getPublic());
        Assert.assertEquals(priKey, keyPair.getPrivate());

        // encrypt
        KP kp = new KP();
        String data = "hello-world";
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        random = new SecureRandom();
        keygen.init(random);
        SecretKey key = keygen.generateKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, pubKey);
        byte[] wrappedKey = cipher.wrap(key); // wrap RSA public key

        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypt = cipher.doFinal(data.getBytes());
        kp.key = Base64.getEncoder().encodeToString(wrappedKey);
        kp.content = Base64.getEncoder().encodeToString(encrypt);
        System.out.println(kp.key);
        System.out.println(kp.content);

        // decrypt
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, priKey);
        Key newKey = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
        Assert.assertEquals(key, newKey);

        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypt = cipher.doFinal(encrypt);
        Assert.assertArrayEquals(data.getBytes(), decrypt);
    }

    static class KP {
        public String key;
        public String content;
    }
}
