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
import app.data.model.Collection;
import app.data.model.UrlCollectionResponse;
import app.service.AuthService;
import app.service.FetchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/link")
public class LinkController {
    private final FetchService mFetchService;
    private final AuthService mAuthService;

    @Autowired
    public LinkController(FetchService fetchService, AuthService authService) {
        mFetchService = fetchService;
        mAuthService = authService;
    }

    @GetMapping("/fetch")
    public UrlCollectionResponse fetchUrInfo(@RequestHeader(value = "token") String token,
        @RequestParam(value = "url") String url) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return new UrlCollectionResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE), null);
        }

        Collection col = StringUtils.isEmpty(url) ? null : mFetchService.getUrlInfo(uid, url);
        if (col != null) {
            return new UrlCollectionResponse(col);
        }

        return new UrlCollectionResponse(ResultCode.ARGUMENT_ERROR, ResultCode.errorCode.get(ResultCode.ARGUMENT_ERROR),
            null);
    }
}
