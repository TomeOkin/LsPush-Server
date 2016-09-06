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
package app.data.validator;

import com.google.common.base.CharMatcher;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class UserInfoValidator {
    private final PhoneNumberUtil phoneValidator = PhoneNumberUtil.getInstance();
    private final EmailValidator emailValidator = EmailValidator.getInstance();

    public boolean isPhoneValid(String number, String region) {
        Phonenumber.PhoneNumber phone;
        try {
            phone = phoneValidator.parse(number, region);
        } catch (NumberParseException e) {
            return false;
        }
        return phoneValidator.isValidNumber(phone);
    }

    public boolean isEmailValid(String email) {
        return emailValidator.isValid(email);
    }

    public int checkPasswordStrength(String password) {
        // 检查字符串格式 ([a-zA-Z0-9\\.,;]){6,}
        int strength = (password == null || password.length() < 6) ? 0 :
            CharMatcher.anyOf(".,;").or(CharMatcher.JAVA_LETTER_OR_DIGIT).negate().indexIn(password);
        if (strength >= 0) {
            return -1;
        }

        strength = CharMatcher.DIGIT.indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.anyOf(".,;").indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.JAVA_LOWER_CASE.indexIn(password) >= 0 ? 1 : 0;
        strength += CharMatcher.JAVA_UPPER_CASE.indexIn(password) >= 0 ? 1 : 0;
        return strength;
    }

    public boolean checkPasswordStrengthBaseline(String password) {
        return checkPasswordStrength(password) >= 2;
    }

    public boolean checkUserId(String userId) {
        // 检查字符串格式 ([a-zA-Z0-9]){3,}
        int strength = (userId == null || userId.length() < 3) ? 0 :
            CharMatcher.JAVA_LETTER_OR_DIGIT.negate().indexIn(userId);
        return strength < 0;
    }

    public boolean checkUserInfo(String uid, String phone, String email, String password, String region) {
        // uid, phone, email 至少一个有效，password 一定要有效
        boolean check;
        check = !StringUtils.isEmpty(uid) && checkUserId(uid);
        check = !check && !StringUtils.isEmpty(phone) && isPhoneValid(phone, region);
        check = !check && isEmailValid(email);
        check = check && checkPasswordStrengthBaseline(password);
        return check;
    }
}
