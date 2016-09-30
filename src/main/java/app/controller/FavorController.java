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
import app.data.local.CollectionBindingRepository;
import app.data.model.BaseResponse;
import app.data.model.CollectionBinding;
import app.data.model.CollectionBindingResponse;
import app.data.model.CryptoToken;
import app.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favor")
public class FavorController {
    private final AuthService mAuthService;
    private final CollectionBindingRepository mColBindingRepo;

    @Autowired
    public FavorController(AuthService authService, CollectionBindingRepository colBindingRepo) {
        mAuthService = authService;
        mColBindingRepo = colBindingRepo;
    }

    @GetMapping("/get")
    public CollectionBindingResponse getFavor(@RequestParam(value = "colId") long colId) {
        CollectionBinding colBinding = mColBindingRepo.findByCollectionId(colId);
        return new CollectionBindingResponse(colBinding);
    }

    @PostMapping("/set")
    public BaseResponse setFavor(@RequestHeader CryptoToken token, @RequestBody CollectionBinding colBinding) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return new BaseResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE));
        }

        if (colBinding == null || colBinding.getFavors() == null || colBinding.getFavors().size() != 1) {
            return new BaseResponse(ResultCode.ARGUMENT_ERROR, ResultCode.errorCode.get(ResultCode.ARGUMENT_ERROR));
        }
        mColBindingRepo.addFavor(colBinding.getCollectionId(), colBinding.getFavors().get(0));
        return new BaseResponse();
    }
}
