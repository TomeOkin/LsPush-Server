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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RunWith(JUnit4.class)
public class JsonBooleanTest {
    private static final Logger logger = LoggerFactory.getLogger(JsonBooleanTest.class);

    @Test
    public void booleanTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BooleanField field = new BooleanField();
        field.setOn(true);
        field.setHasDone(true);

        String json = mapper.writeValueAsString(field);
        logger.info(json);
        Assert.assertTrue("key contain 'hasDone'", json.contains("hasDone"));
        Assert.assertTrue("key contain 'isOn", json.contains("isOn"));

        BooleanField item = mapper.readValue(json, BooleanField.class);
        Assert.assertTrue("item.hasDone", item.hasDone);
        Assert.assertTrue("item.isOn", item.isOn);
    }

    public static class BooleanField {
        private boolean hasDone;
        @JsonProperty private boolean isOn;

        public boolean isHasDone() {
            return hasDone;
        }

        public void setHasDone(boolean hasDone) {
            this.hasDone = hasDone;
        }

        @JsonIgnore
        public boolean isOn() {
            return isOn;
        }

        public void setOn(boolean on) {
            isOn = on;
        }
    }
}
