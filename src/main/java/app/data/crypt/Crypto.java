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
package app.data.crypt;

import app.data.model.CryptoToken;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypto {
    private static Crypto crypt;
    private static PublicKey pubKey;
    private static PrivateKey priKey;

    public static void init(final String pubKey, final String priKey)
        throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        if (crypt == null) {
            crypt = new Crypto(pubKey, priKey);
        }
    }

    /**
     * @param pubKey has encoding by Base64
     */
    private Crypto(final String pubKey, final String priKey)
        throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        initKey(pubKey, priKey);
    }

    private void initKey(final String pubKey, final String priKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        final X509EncodedKeySpec pubKeySpec =
            new X509EncodedKeySpec(Base64.getDecoder().decode(pubKey.getBytes(StandardCharsets.UTF_8)));
        Crypto.pubKey = keyFactory.generatePublic(pubKeySpec);
        final PKCS8EncodedKeySpec priKeySpec =
            new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKey.getBytes(StandardCharsets.UTF_8)));
        Crypto.priKey = keyFactory.generatePrivate(priKeySpec);
    }

    /**
     * @see Crypto#encrypt
     */
    public static byte[] decrypt(CryptoToken cryptoToken)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
        IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchProviderException {
        byte[] key = Base64.getDecoder().decode(cryptoToken.key.getBytes(StandardCharsets.UTF_8));
        byte[] param = Base64.getDecoder().decode(cryptoToken.param.getBytes(StandardCharsets.UTF_8));
        byte[] value = Base64.getDecoder().decode(cryptoToken.value.getBytes(StandardCharsets.UTF_8));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.UNWRAP_MODE, priKey);
        Key secretKey = cipher.unwrap(key, "AES", Cipher.SECRET_KEY);

        IvParameterSpec ivParams = new IvParameterSpec(param);

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        return cipher.doFinal(value);
    }

    public static CryptoToken encrypt(String data)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        return encrypt(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @see Cipher, there have a table of support transformations
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#cipherTable">javase-7-support
     * transformations</a>，可通过 Cipher.getInstance("RSA").getProvider().getClass().getName() 查看默认提供者。
     * @see <a href="https://nelenkov.blogspot.com/2012/04/using-password-based-encryption-on.html">Android
     * 下的最佳实践</>
     * @see <a href="http://grokbase.com/t/gg/android-developers/12cd9pdwer/cipher-wrap-not-working-in-android-4-2">Wrap
     * 方式下存在的问题及解决方案</a>
     * @see <a href="http://www.droidsec.cn/android%E5%BA%94%E7%94%A8%E5%AE%89%E5%85%A8%E5%BC%80%E5%8F%91%E4%B9%8B%E6%B5%85%E8%B0%88%E5%8A%A0%E5%AF%86%E7%AE%97%E6%B3%95%E7%9A%84%E5%9D%91/">Android应用安全开发之浅谈加密算法的坑</a>
     * it tell you some best way to use those algorithm.
     * @see <a href="http://techmedia-think.hatenablog.com/entry/20110527/1306499951">JavaとRubyで暗号化/復号化</a>, a good post
     * of AES.
     * @see <a href="http://qiita.com/f_nishio/items/485490dea126dbbb5001">a example of
     * RSA/ECB/OAEPWithSHA-256AndMGF1Padding</a>
     */
    public static CryptoToken encrypt(byte[] data)
        throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        // get a AES key for encrypt
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        // Android default key size is not support in standard java
        keygen.init(128);
        SecretKey secretKey = keygen.generateKey();

        // AES + ECB is not secure, suggest to use AES + CBC
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        byte[] encrypt = cipher.doFinal(data);

        // Android default RSA transformation is different from standard JCE
        // when using RSA/ECB/OAEPWithSHA-256AndMGF1Padding, it will throw ArrayOutOfBound exception.
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.WRAP_MODE, pubKey);
        byte[] wrappedKey = cipher.wrap(secretKey); // wrap RSA public key

        CryptoToken cryptoToken = new CryptoToken();
        cryptoToken.key = Base64.getEncoder().encodeToString(wrappedKey);
        cryptoToken.param = Base64.getEncoder().encodeToString(iv);
        cryptoToken.value = Base64.getEncoder().encodeToString(encrypt);
        return cryptoToken;
    }
}
