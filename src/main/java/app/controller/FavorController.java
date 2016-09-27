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
import app.data.local.FavorRepository;
import app.data.model.BaseResponse;
import app.data.model.CryptoToken;
import app.data.model.Favor;
import app.data.model.FavorResponse;
import app.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favor")
public class FavorController {
    private final AuthService authService;
    private final FavorRepository favorRepository;

    @Autowired
    public FavorController(AuthService authService, FavorRepository favorRepository) {
        this.authService = authService;
        this.favorRepository = favorRepository;
    }

    @GetMapping("/get")
    public FavorResponse getFavor(@RequestParam(value = "colId") long colId) {
        Favor favor = favorRepository.findFavor(colId);
        return new FavorResponse(favor);
    }

    @PostMapping("/set")
    public BaseResponse setFavor(@RequestHeader CryptoToken token, @RequestBody Favor favor) {
        String uid = authService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return new BaseResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE));
        }

        if (favor == null || favor.dataList == null || favor.dataList.size() != 1) {
            return new BaseResponse(ResultCode.ARGUMENT_ERROR, ResultCode.errorCode.get(ResultCode.ARGUMENT_ERROR));
        }
        favorRepository.addFavor(favor.collectionId, favor.dataList.get(0));
        return new BaseResponse();
    }
}
