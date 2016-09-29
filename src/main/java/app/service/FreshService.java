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

import app.data.model.internal.FreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FreshService {
    public static final String DESTINATION = "collection";
    private static final String FRESH_COLLECTION_SET = "FRESH_COLLECTION_SET";
    private static final Logger logger = LoggerFactory.getLogger(FreshService.class);
    private final StringRedisTemplate template;

    @Autowired
    public FreshService(StringRedisTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void init() {
        //        long count = template.opsForZSet().count(FRESH_COLLECTION_SET, 0, Double.MAX_VALUE);
        //        Set<ZSetOperations.TypedTuple<String>> values = new HashSet<>();
        //        for (int i = (int) count; i < 100; i++) {
        //            values.add(new DefaultTypedTuple<>(String.valueOf(i - 100), 0d));
        //        }
        //        template.opsForZSet().add(FRESH_COLLECTION_SET, values);
    }

    @JmsListener(destination = DESTINATION, containerFactory = "defaultFactory")
    public void receiveMessage(FreshEvent event) {
        if (event == null
            || event.event < 1
            || event.event > 2
            || event.colId < 0) {
            return;
        }

        //        logger.info(event.toString());
        //        String value = String.valueOf(event.colId);
        //        if (event.event == FreshEvent.EVENT_COLLECTION) {
        //            //template.opsForZSet().
        //            template.opsForZSet().add(FRESH_COLLECTION_SET, value, 1);
        //        }
    }
}
