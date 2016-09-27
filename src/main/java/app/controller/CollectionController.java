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
import app.data.model.BaseResponse;
import app.data.model.Collection;
import app.data.model.CollectionResponse;
import app.data.model.CryptoToken;
import app.service.AuthService;
import app.service.CollectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {
    private final AuthService authService;
    private final CollectionService collectionService;

    @Autowired
    public CollectionController(AuthService authService, CollectionService collectionService) {
        this.authService = authService;
        this.collectionService = collectionService;
    }

    @PostMapping("/post")
    public BaseResponse postCollection(@RequestHeader CryptoToken token, @RequestBody Collection collection) {
        String uid = authService.checkIfAuthBind(token);
        if (StringUtils.isEmpty(uid)) {
            return new BaseResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE));
        }

        collectionService.postCollection(uid, collection);
        return new BaseResponse();
    }

    @GetMapping("/get")
    public CollectionResponse getCollections(@RequestParam(value = "uid") String uid,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size,
        @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
        @RequestParam(value = "sort", defaultValue = "update_date", required = false) String sortProperty) {

        if (StringUtils.isEmpty(uid) || !authService.isExistUser(uid)) {
            return new CollectionResponse(ResultCode.USER_AUTH_FAILURE,
                ResultCode.errorCode.get(ResultCode.USER_AUTH_FAILURE), null);
        }

        List<Collection> collectionList =
            collectionService.findByUser(uid, page, size, new Sort(direction, sortProperty));
        return new CollectionResponse(collectionList);
    }

    @GetMapping("/getByUrl")
    public CollectionResponse getCollectionsFromLink(@RequestParam(value = "url") String url,
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size,
        @RequestParam(value = "order", defaultValue = "DESC", required = false) Sort.Direction direction,
        @RequestParam(value = "sort", defaultValue = "updateDate", required = false) String sortProperty) {

        List<Collection> collectionList =
            collectionService.findCollection(url, page, size, new Sort(direction, sortProperty));
        return new CollectionResponse(collectionList);
    }

    @GetMapping("/getLatest")
    public CollectionResponse getLatestCollections(
        @RequestParam(value = "page", defaultValue = "0", required = false) int page,
        @RequestParam(value = "size", defaultValue = "20", required = false) int size) {
        List<Collection> colList = collectionService.getLatestCollection(page, size);
        return new CollectionResponse(colList);
    }
}
