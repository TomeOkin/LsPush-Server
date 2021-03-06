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
import app.data.model.Response;
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
    public Response<CollectionBinding> getFavor(@RequestParam(value = "colId") long colId) {
        CollectionBinding colBinding = mColBindingRepo.findByCollectionId(colId);
        return new Response<>(colBinding);
    }

    @PostMapping("/remove")
    public BaseResponse removeFavor(@RequestHeader(value = "token") String token, @RequestBody long colId) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        mColBindingRepo.removeFavor(colId, uid);
        return new BaseResponse();
    }

    @PostMapping("/set")
    public BaseResponse setFavor(@RequestHeader(value = "token") String token,
        @RequestBody CollectionBinding colBinding) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        if (colBinding == null || colBinding.getFavors() == null || colBinding.getFavors().size() != 1) {
            return ResultCode.error(ResultCode.ARGUMENT_ERROR);
        }
        mColBindingRepo.addFavor(colBinding.getCollectionId(), uid, colBinding.getFavors().get(0));
        return new BaseResponse();
    }
}
