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
package app.service;

import app.config.ResultCode;
import app.data.local.PinRepository;
import app.data.model.BaseResponse;
import app.data.model.Collection;
import app.data.model.PinData;
import app.data.model.User;
import app.data.model.internal.Pin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PinService {
    public static final int PIN_LIMIT = 5;
    private static final Logger logger = LoggerFactory.getLogger(PinService.class);

    private final PinRepository mPinRepo;
    private final CollectionService mColService;
    private final ObjectMapper mObjectMapper;

    @Autowired
    public PinService(PinRepository pinRepo, CollectionService colService, ObjectMapper objectMapper) {
        mPinRepo = pinRepo;
        mColService = colService;
        mObjectMapper = objectMapper;
    }

    public int getPin(String uid, List<PinData> pinDatas) {
        User user = new User();
        user.setUid(uid);
        Pin pin = mPinRepo.findByUser(user);

        if (pin == null || StringUtils.isEmpty(pin.getPins())) {
            return BaseResponse.COMMON_SUCCESS;
        }

        try {
            String pins = pin.getPins();
            List<PinData> list = mObjectMapper.readValue(pins, new TypeReference<List<PinData>>() {});
            for (PinData pinData : list) {
                long colId = pinData.getCollection().getId();
                pinData.setCollection(mColService.findByID(colId));
            }
            pinDatas.addAll(list);
        } catch (IOException e) {
            logger.info("parse list-pinData failure", e);
            return ResultCode.PARSE_PIN_DATA_FAILED;
        }

        return BaseResponse.COMMON_SUCCESS;
    }

    public int updatePin(String uid, List<PinData> pinDatas) {
        if (pinDatas.size() > PIN_LIMIT) {
            return ResultCode.PIN_DATA_TOO_MUCH;
        }

        // get old pin-data' id
        User user = new User();
        user.setUid(uid);
        Pin old = mPinRepo.findByUser(user);

        List<Long> ids = new ArrayList<>(5);
        if (old != null && !StringUtils.isEmpty(old.getPins())) {
            try {
                String pins = old.getPins();
                List<PinData> list = mObjectMapper.readValue(pins, new TypeReference<List<PinData>>() {});
                for (PinData pinData : list) {
                    long colId = pinData.getCollection().getId();
                    ids.add(colId);
                }
            } catch (IOException e) {
                // for writing, ignore because we will overwrite it
                logger.info("parse list-pinData -> object failure", e);
            }
        }

        // prepare new pin-data
        for (PinData pinData : pinDatas) {
            long id = pinData.getCollection().getId();
            if (!mColService.isExistCollection(id)) {
                return ResultCode.COLLECTION_NOT_EXIST;
            }
            Collection collection = new Collection();
            collection.setId(id);
            pinData.setCollection(collection);

            // update pin-data' Date
            if (!ids.contains(id)) {
                pinData.setPinDate(DateTime.now().toDate());
            }
        }

        try {
            String json = mObjectMapper.writeValueAsString(pinDatas);
            Pin pin = new Pin();
            pin.setUser(user);
            pin.setPins(json);
            if (old != null) {
                pin.setId(old.getId());
            }
            mPinRepo.save(pin);
        } catch (JsonProcessingException e) {
            logger.info("parse list-pinData -> string failure", e);
            return ResultCode.PARSE_PIN_DATA_FAILED;
        }

        return BaseResponse.COMMON_SUCCESS;
    }
}
