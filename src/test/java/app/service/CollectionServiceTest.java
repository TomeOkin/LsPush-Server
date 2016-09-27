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
import app.data.local.FavorRepository;
import app.data.local.UserRepository;
import app.data.model.Collection;
import app.data.model.Favor;
import app.data.model.Link;
import app.data.model.User;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
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
    @Autowired CollectionService colService;
    @Autowired UserRepository userRepository;
    @Autowired FavorRepository favorRepository;

    private User one;
    private User two;

    @Before
    public void initUser() {
        one = new User();
        one.setUid("one");
        one.setNickname("One");
        one.setEmail("123456@qq.com");
        one.setPassword("abc123456");
        one.setValidate(User.EMAIL_VALID);
        userRepository.save(one);

        two = new User();
        two.setUid("two");
        two.setNickname("Two");
        two.setPhone("1310890");
        two.setPassword("pwd123456");
        two.setValidate(User.PHONE_VALID);
        userRepository.save(two);
    }

    @Test
    public void collectionTest() {
        Link link = new Link("https://www.google.com", "Google");
        Collection googleCol = new Collection(one, link, "google search", "");
        colService.postCollection(one.getUid(), googleCol);

        link = new Link("https://www.baidu.com", "Baidu");
        Collection baiduCol = new Collection(one, link, "baidu search", "");
        colService.postCollection(one.getUid(), baiduCol);

        baiduCol = new Collection(one, link, "Baidu Search", "");
        colService.postCollection(two.getUid(), baiduCol);

        List<Collection> cols = colService.findByUser(one.getUid(), 0, 5, new Sort(Sort.Direction.DESC, "updateDate"));
        Assert.assertNotNull(cols);

        Collection col = cols.get(0);
        logger.info(col.toString());
        Assert.assertTrue(col.toString().contains("baidu"));

        long colId = col.getId();

        Favor.Data data = new Favor.Data();
        data.date = DateTime.now().toDate();
        data.uid = one.getUid();
        favorRepository.addFavor(colId, data);
        favorRepository.addFavor(colId, data);

        // prepare favor
        data = new Favor.Data();
        data.date = DateTime.now().toDate();
        data.uid = two.getUid();
        favorRepository.addFavor(colId, data);

        // getLatestCollection
        List<Collection> latestCols = colService.getLatestCollection(0, 5);
        for (Collection item : latestCols) {
            logger.info(item.toString());
        }

        favorRepository.removeFavor(colId, one.getUid());
    }
}
