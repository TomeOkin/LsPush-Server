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

import app.data.model.WebPageInfo;
import app.data.parse.WebPageUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FetchService {
    private static final Logger logger = LoggerFactory.getLogger(FetchService.class);
    private final Cache<String, WebPageInfo> urlInfoCache;

    public FetchService() {
        urlInfoCache = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();
    }

    public WebPageInfo getUrlInfo(String url) {
        try {
            return WebPageUtil.parse(url, urlInfoCache);
        } catch (Exception e) {
            logger.warn("get url info false", e);
        }
        return null;
    }
}
