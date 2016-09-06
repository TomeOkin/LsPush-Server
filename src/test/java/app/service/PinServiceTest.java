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

import app.App;
import app.data.local.UserRepository;
import app.data.model.Collection;
import app.data.model.Link;
import app.data.model.PinData;
import app.data.model.User;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class PinServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(PinServiceTest.class);
    @Autowired PinService pinService;
    @Autowired UserRepository userRepository;
    @Autowired CollectionService collectionService;

    @Test
    public void postTest() {
        User user = new User();
        user.setUid("abcd");
        user.setNickname("acdf");
        user.setEmail("123456@qq.com");
        user.setPassword("abc123456");
        user.setValidate(User.EMAIL_VALID);
        userRepository.save(user);

        List<PinData> pinDatas = new ArrayList<>();

        Link link = new Link("https://www.google.com", "Google");
        Collection collection = new Collection(user, link, "google search", "");
        collectionService.postCollection(user.getUid(), collection);
        pinDatas.add(new PinData(collection, DateTime.now().toDate()));

        link = new Link("https://www.baidu.com", "Baidu");
        collection = new Collection(user, link, "baidu search", "");
        collectionService.postCollection(user.getUid(), collection);
        pinDatas.add(new PinData(collection, DateTime.now().toDate()));

        User own = new User();
        own.setUid("own");
        own.setNickname("own");
        own.setEmail("own@qq.com");
        own.setPassword("abc123456");
        own.setValidate(User.EMAIL_VALID);
        userRepository.save(own);

        pinService.updatePin(own.getUid(), pinDatas);

        List<PinData> datas = new ArrayList<>();
        pinService.getPin(own.getUid(), datas);
        Assert.assertEquals(2, datas.size());
        for (PinData pin : datas) {
            logger.info(pin.toString());
        }


    }
}
