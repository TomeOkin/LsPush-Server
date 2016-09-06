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
    private final AuthService authService;
    private final PinService pinService;

    @Autowired
    public PinController(AuthService authService, PinService pinService) {
        this.authService = authService;
        this.pinService = pinService;
    }

    @GetMapping("/get")
    public PinResponse getPinData(@RequestParam(value = "uid") String uid) {
        if (StringUtils.isEmpty(uid) || !authService.isExistUser(uid)) {
            return new PinResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE), null);
        }

        List<PinData> pinDatas = new ArrayList<>(5);
        int result = pinService.getPin(uid, pinDatas);
        if (result == BaseResponse.COMMON_SUCCESS) {
            return new PinResponse(pinDatas);
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Get Pin-Data Failure");
        return new PinResponse(result, description, null);
    }

    @PostMapping("/post")
    public BaseResponse updatePinData(@RequestHeader CryptoToken token, @RequestBody PinRequest request) {
        String uid = authService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return new BaseResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE));
        }

        int result = pinService.updatePin(uid, request.getPinDatas());
        if (result == BaseResponse.COMMON_SUCCESS) {
            return new BaseResponse();
        }

        String description = ResultCode.errorCode.getOrDefault(result, "Update Pin-Data Failure");
        return new BaseResponse(result, description);
    }
}
