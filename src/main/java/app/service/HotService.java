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

//@Service
public class HotService {
    //    //private final RedisTemplate<String, Long> template;
    //    private static final Logger logger = LoggerFactory.getLogger(HotService.class);
    //
    //    @Autowired
    //    public HotService(RedisTemplate<String, Long> template) {
    //        this.template = template;
    //        init();
    //    }
    //
    //    public void init() {
    //        ValueOperations<String, Long> values = template.opsForValue();
    //        values.set("col:1", 10L, 10, TimeUnit.SECONDS);
    //        values.set("col:2", 10L, 10, TimeUnit.SECONDS);
    //    }
    //
    //    //@Scheduled(fixedDelay = 5000)
    //    public void done() {
    //        ValueOperations<String, Long> values = template.opsForValue();
    //        long value = values.get("col:2");
    //        values.set("col:2", value);
    //        Set<String> keys = template.keys("col:*");
    //        logger.info("------------------------------");
    //        for (String key : keys) {
    //            logger.info("(key, value) = ({}, {})", key, template.opsForValue().get(key));
    //        }
    //    }
}
