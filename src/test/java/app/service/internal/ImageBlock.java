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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RunWith(JUnit4.class)
public class ImageBlock {
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ImageBlock.class);

    @Test
    public void testImageJson() throws IOException {
        String json = "{\"image\":\"{\\\"url\\\":\\\"https\\\",\\\"color\\\":-1,\\\"width\\\":1,\\\"height\\\":1}\"}";
        Body body = mapper.readValue(json, Body.class);
        logger.info("body: {}", body.toString());
    }

    public static class Body {
        public String image;

        @Override
        public String toString() {
            return "Body{" +
                "image=" + image +
                '}';
        }
    }
}
