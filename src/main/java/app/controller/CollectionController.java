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
import app.service.CollectionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);
    private final AuthService mAuthService;
    private final CollectionService mColService;

    @Autowired
    public CollectionController(AuthService authService, CollectionService colService) {
        mAuthService = authService;
        mColService = colService;
    }

    @PostMapping("/post")
    public BaseResponse postCollection(@RequestHeader(value = "token") String token, @RequestBody Collection col) {
        String uid = mAuthService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        logger.info("collection: {}", col);
        mColService.postCollection(uid, col);
        return new BaseResponse();
    }

    @GetMapping("/get")
    public Response<List<Collection>> getCollections(@RequestParam(value = "uid") String uid,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {

        if (StringUtils.isEmpty(uid) || !mAuthService.isExistUser(uid)) {
            return ResultCode.error(ResultCode.USER_AUTH_FAILED);
        }

        List<Collection> colList = mColService.findByUser(uid, page, size);
        return new Response<>(colList);
    }

    @GetMapping("/getByUrl")
    public Response<List<Collection>> getCollectionsByUrl(
        @RequestParam(value = "url") String url,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size,
        @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
        @RequestParam(value = "sort", defaultValue = "updateDate", required = false) String sortProperty,
        @RequestParam(value = "uid", defaultValue = "", required = false) String uid) {

        List<Collection> colList = mColService.findByUrl(uid, url, page, size, new Sort(direction, sortProperty));
        return new Response<>(colList);
    }

    @GetMapping("/getByTags")
    public Response<List<Collection>> getCollectionsByTags(
        @RequestParam(value = "tags") List<String> tags,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size,
        @RequestParam(value = "uid", defaultValue = "", required = false) String uid) {

        List<Collection> colList = mColService.findByTags(uid, tags, page, size);
        return new Response<>(colList);
    }

    @GetMapping("/getLatest")
    public Response<List<Collection>> getLatestCollections(
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size,
        @RequestParam(value = "uid", defaultValue = "", required = false) String uid) {
        List<Collection> colList = mColService.getLatestCollections(uid, page, size);
        return new Response<>(colList);
    }
}
