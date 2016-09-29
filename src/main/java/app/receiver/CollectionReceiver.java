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
package app.receiver;

import app.data.model.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component
public class CollectionReceiver {
    private static final Logger logger = LoggerFactory.getLogger(CollectionReceiver.class);

    public void receiveMessage(Object message) {
        Collection collection = (Collection) message;
        logger.info("receive message: {}", collection.toString());
    }
}