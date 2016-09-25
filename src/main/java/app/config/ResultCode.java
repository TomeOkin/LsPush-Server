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
package app.config;

import java.util.HashMap;

public class ResultCode {
    // send captcha
    public static final int TIME_INTERVAL_TOO_CLOSE = 1;
    public static final int SEND_CAPTCHA_FAILURE = 2;

    // check captcha
    public static final int TRYING_TOO_MUCH = 3;
    public static final int MATCHING_FAILURE = 4;
    public static final int INVALID_CAPTCHA = 5;

    // AuthService
    public static final int INVALID_TOKEN = 6;
    public static final int ENCRYPT_TOKEN_FAILURE = 7;
    public static final int OVERDUE_TOKEN = 8;

    public static final int ILLEGAL_ACCOUNT_INFO = 9;
    public static final int UID_EXISTED = 10;

    public static final int UPDATE_USER_INFO_FAILURE = 11;
    public static final int QUERY_USER_FAILURE = 12;
    public static final int MORE_THAN_ONE_ACCOUNT = 13;
    public static final int ACCOUNT_NOT_MATCH = 14;

    public static final int FILE_EMPTY = 15;
    public static final int UPLOAD_FILE_FAILURE = 16;
    public static final int UNKNOWN_RESOURCE = 17;
    public static final int RESOURCE_NOT_EXIT = 18;
    public static final int TRANSFER_FILE_FAILURE = 19;
    public static final int USER_AUTH_FAILURE = 20;

    public static final int PIN_DATA_TOO_MUCH = 21;
    public static final int COLLECTION_NOT_EXIST = 22;
    public static final int PARSE_PIN_DATA_FAILURE = 23;

    public static final int ARGUMENT_ERROR = 24;

    /**
     * It not contain all the status, only those can send to user.
     */
    public static final HashMap<Integer, String> errorCode = new HashMap<>(100);

    static {
        errorCode.put(TIME_INTERVAL_TOO_CLOSE, "Time Interval Too Close");
        errorCode.put(SEND_CAPTCHA_FAILURE, "Send Captcha Failure");

        errorCode.put(TRYING_TOO_MUCH, "Trying Too Much");
        errorCode.put(MATCHING_FAILURE, "Matching Failure");
        errorCode.put(INVALID_CAPTCHA, "Invalid Captcha");

        errorCode.put(INVALID_TOKEN, "Invalid Token");
        errorCode.put(OVERDUE_TOKEN, "Overdue Token");

        errorCode.put(ILLEGAL_ACCOUNT_INFO, "Illegal Account Info");
        errorCode.put(UID_EXISTED, "User id has existed, please try another");

        errorCode.put(MORE_THAN_ONE_ACCOUNT,
            "Your phone is associated with multiple accounts, please using your user id to login");
        errorCode.put(ACCOUNT_NOT_MATCH, "Please confirm what you input is correctly");

        errorCode.put(FILE_EMPTY, "File is empty");
        errorCode.put(UPLOAD_FILE_FAILURE, "Upload file failure");
        errorCode.put(RESOURCE_NOT_EXIT, "Resource not exit");
        errorCode.put(TRANSFER_FILE_FAILURE, "Transfer file failure");
        errorCode.put(USER_AUTH_FAILURE, "User auth failure");

        errorCode.put(PIN_DATA_TOO_MUCH, "Pin Data Too Much");
        errorCode.put(COLLECTION_NOT_EXIST, "Collection Not Exist");

        errorCode.put(ARGUMENT_ERROR, "Argument Error");
    }
}
