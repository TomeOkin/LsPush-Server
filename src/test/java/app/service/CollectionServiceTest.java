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
import app.data.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class CollectionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CollectionServiceTest.class);
    @Autowired CollectionService collectionService;
    @Autowired UserRepository userRepository;

    @Test
    public void collectionTest() {
        User user = new User();
        user.setUid("abcd");
        user.setNickname("acdf");
        user.setEmail("123456@qq.com");
        user.setPassword("abc123456");
        user.setValidate(User.EMAIL_VALID);
        userRepository.save(user);

        Link link = new Link("https://www.google.com", "Google");
        Collection collection = new Collection(user, link, "google search", "");
        collectionService.postCollection(user.getUid(), collection);

        link = new Link("https://www.baidu.com", "Baidu");
        collection = new Collection(user, link, "baidu search", "");
        collectionService.postCollection(user.getUid(), collection);

        List<Collection> collections =
            collectionService.findByUser(user.getUid(), 0, 5, new Sort(Sort.Direction.DESC, "createDate"));
        Assert.assertNotNull(collections);

        Collection col = collections.get(0);
        Assert.assertTrue(col.toString().contains("baidu"));
        logger.info(col.toString());
    }
}
