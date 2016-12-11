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
import app.service.PinService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user/pin")
public class PinController {
    private final AuthService mAuthService;
    private final PinService mPinService;

    @Autowired
    public PinController(AuthService authService, PinService pinService) {
        mAuthService = authService;
        mPinService = pinService;
    }

    @GetMapping("/get")
    public Response<List<PinData>> getPinData(@RequestParam(value = "uid") String uid) {
        if (StringUtils.isEmpty(uid) || !mAuthService.isExistUser(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        List<PinData> pinDatas = new ArrayList<>(5);
        int result = mPinService.getPin(uid, pinDatas);
        return ResultCode.get(result, "Get Pin-Data Failed", pinDatas);
    }

    @PostMapping("/post")
    public BaseResponse updatePinData(@RequestHeader(value = "token") String token, @RequestBody PinRequest request) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        int result = mPinService.updatePin(uid, request.getPinDatas());
        return ResultCode.get(result, "Update Pin-Data Failure", null);
    }
}
