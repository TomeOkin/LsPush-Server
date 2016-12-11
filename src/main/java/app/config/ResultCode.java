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

import app.data.model.BaseResponse;
import app.data.model.Response;

import java.util.Map;
import java.util.TreeMap;

public class ResultCode {
    // send captcha
    public static final int TIME_INTERVAL_TOO_CLOSE = 1;
    public static final int SEND_CAPTCHA_FAILED = 2;

    // check captcha
    public static final int TRYING_TOO_MUCH = 3;
    public static final int MATCHING_FAILED = 4;
    public static final int INVALID_CAPTCHA = 5;

    // AuthService
    public static final int INVALID_TOKEN = 6;
    public static final int ENCRYPT_TOKEN_FAILED = 7;
    public static final int OVERDUE_TOKEN = 8;

    public static final int ILLEGAL_ACCOUNT_INFO = 9;
    public static final int UID_EXISTED = 10;

    public static final int UPDATE_USER_INFO_FAILED = 11;
    public static final int QUERY_USER_FAILED = 12;
    public static final int MORE_THAN_ONE_ACCOUNT = 13;
    public static final int ACCOUNT_NOT_MATCH = 14;

    public static final int FILE_EMPTY = 15;
    public static final int UPLOAD_FILE_FAILED = 16;
    public static final int UNKNOWN_RESOURCE = 17;
    public static final int RESOURCE_NOT_EXIT = 18;
    public static final int TRANSFER_FILE_FAILED = 19;
    public static final int USER_AUTH_FAILED = 20;

    public static final int PIN_DATA_TOO_MUCH = 21;
    public static final int COLLECTION_NOT_EXIST = 22;
    public static final int PARSE_PIN_DATA_FAILED = 23;

    public static final int ARGUMENT_ERROR = 24;

    public static final int FETCH_URL_DATA_FAILED = 25;

    /**
     * It not contain all the status, only those can send to user.
     */
    private static final Map<Integer, String> mErrorCode = new TreeMap<>();

    static {
        mErrorCode.put(BaseResponse.COMMON_SUCCESS, BaseResponse.COMMON_SUCCESS_MESSAGE);
        
        mErrorCode.put(TIME_INTERVAL_TOO_CLOSE, "Time interval too close");
        mErrorCode.put(SEND_CAPTCHA_FAILED, "Send captcha failed");

        mErrorCode.put(TRYING_TOO_MUCH, "Trying too much");
        mErrorCode.put(MATCHING_FAILED, "Matching failed");
        mErrorCode.put(INVALID_CAPTCHA, "Invalid captcha");

        mErrorCode.put(INVALID_TOKEN, "Invalid token");
        mErrorCode.put(OVERDUE_TOKEN, "Overdue token");

        mErrorCode.put(ILLEGAL_ACCOUNT_INFO, "Illegal account info");
        mErrorCode.put(UID_EXISTED, "User id has existed, please try another");

        mErrorCode.put(MORE_THAN_ONE_ACCOUNT,
            "Your phone is associated with multiple accounts, please using your user id to login");
        mErrorCode.put(ACCOUNT_NOT_MATCH, "Please confirm what you input is correctly");

        mErrorCode.put(FILE_EMPTY, "File is empty");
        mErrorCode.put(UPLOAD_FILE_FAILED, "Upload file failed");
        mErrorCode.put(RESOURCE_NOT_EXIT, "Resource not exit");
        mErrorCode.put(TRANSFER_FILE_FAILED, "Transfer file failed");
        mErrorCode.put(USER_AUTH_FAILED, "User auth failed");

        mErrorCode.put(PIN_DATA_TOO_MUCH, "Pin data too much");
        mErrorCode.put(COLLECTION_NOT_EXIST, "Collection not exist");

        mErrorCode.put(ARGUMENT_ERROR, "Argument error");
        mErrorCode.put(FETCH_URL_DATA_FAILED, "Fetch url data failed");
    }
    
    public static <T> Response<T> error(int errorCode) {
        return new Response<>(errorCode, mErrorCode.get(errorCode));
    }

    @Deprecated
    public static <T> Response<T> error(int errorCode, String message) {
        return new Response<>(errorCode, mErrorCode.getOrDefault(errorCode, message));
    }

    public static <T> Response<T> get(int resultCode, String message, T data) {
        if (resultCode == BaseResponse.COMMON_SUCCESS) {
            return new Response<>(data);
        }

        return new Response<>(resultCode, mErrorCode.getOrDefault(resultCode, message));
    }
}
