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
package app.service.internal;

import com.google.common.hash.Hashing;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class UrlHashTest {
    private static final Logger logger = LoggerFactory.getLogger(UrlHashTest.class);

    @Test
    public void hashUrl() {
        String url =
            "http://google.github.io/guava/releases/12.0/api/docs/com/google/common/hash/Hashing.html#murmur3_128()";
        String result = Hashing.murmur3_128().hashString(url, StandardCharsets.UTF_8).toString() + Hashing.sipHash24()
            .hashString(url, StandardCharsets.UTF_8);
        logger.info(result);
    }

    @Test
    public void twice() {
        hashUrl();
        hashUrl();
    }
}
