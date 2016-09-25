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
import app.data.model.UrlFetchResponse;
import app.data.model.WebPageInfo;
import app.service.FetchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/link")
public class LinkController {
    private final FetchService fetchService;

    @Autowired
    public LinkController(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @GetMapping("/fetch")
    public UrlFetchResponse fetchUrInfo(@RequestParam(value = "url") String url) {
        WebPageInfo info = StringUtils.isEmpty(url) ? null : fetchService.getUrlInfo(url);
        if (info != null) {
            return new UrlFetchResponse(info);
        }

        return new UrlFetchResponse(ResultCode.ARGUMENT_ERROR, ResultCode.errorCode.get(ResultCode.ARGUMENT_ERROR),
            null);
    }
}
