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
import app.data.local.CollectionBindingRepository;
import app.data.local.UserRepository;
import app.data.model.Collection;
import app.data.model.CollectionBinding;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class CollectionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CollectionServiceTest.class);
    @Autowired CollectionService mColService;
    @Autowired UserRepository mUserRepo;
    @Autowired CollectionBindingRepository mColBindingRepo;

    private User one;
    private User two;

    @Before
    public void initUser() {
        mUserRepo.deleteAll();

        one = new User();
        one.setUid("one");
        one.setNickname("One");
        one.setEmail("123456@qq.com");
        one.setPassword("abc123456");
        one.setValidate(User.EMAIL_VALID);
        mUserRepo.save(one);

        two = new User();
        two.setUid("two");
        two.setNickname("Two");
        two.setPhone("1310890");
        two.setPassword("pwd123456");
        two.setValidate(User.PHONE_VALID);
        mUserRepo.save(two);
    }

    @Test
    public void collectionTest() {
        mColBindingRepo.dropAll();

        Link link = new Link("https://www.google.com", "Google");
        Collection googleCol = new Collection(one, link, "google search", "");
        mColService.postCollection(one.getUid(), googleCol);

        link = new Link("https://www.baidu.com", "Baidu");
        Collection baiduCol = new Collection(one, link, "baidu search", "");
        mColService.postCollection(one.getUid(), baiduCol);

        baiduCol = new Collection(one, link, "Baidu Search", "");
        mColService.postCollection(two.getUid(), baiduCol);

        List<Collection> cols = mColService.findByUser(one.getUid(), 0, 5);
        Assert.assertNotNull(cols);

        Collection col = cols.get(0);
        logger.info(col.toString());
        Assert.assertTrue(col.toString().contains("baidu"));

        long colId = col.getId();

        CollectionBinding.Data data = new CollectionBinding.Data();
        data.date = DateTime.now().toDate();
        data.uid = one.getUid();
        mColBindingRepo.addFavor(colId, data);
        mColBindingRepo.addFavor(colId, data);

        // prepare favor
        data = new CollectionBinding.Data();
        data.date = DateTime.now().toDate();
        data.uid = two.getUid();
        mColBindingRepo.addFavor(colId, data);

        // getLatestCollections
        List<Collection> latestCols = mColService.getLatestCollections(one.getUid() ,0, 5);
        Assert.assertNotNull(latestCols);
        logger.info("Latest Collections: ");
        for (Collection item : latestCols) {
            logger.info(item.toString());
        }

        List<CollectionBinding> uidFavors = mColBindingRepo.findByUid(one.getUid(), null);
        Assert.assertNotNull(uidFavors);
        logger.info("User Favors: ");
        for (CollectionBinding item : uidFavors) {
            logger.info(item.toString());
        }

        mColBindingRepo.removeFavor(colId, one.getUid());
    }

    @Test
    public void testCollectionTag() {
        mColBindingRepo.dropAll();

        mColBindingRepo.updateTags(101, Arrays.asList("test", "hello"));
        mColBindingRepo.updateTags(102, Arrays.asList("abc", "hello"));

        CollectionBinding colTag = mColBindingRepo.findByCollectionId(101);
        Assert.assertNotNull(colTag);
        logger.info(colTag.toString());

        colTag = mColBindingRepo.findByCollectionId(102);
        Assert.assertNotNull(colTag);
        logger.info(colTag.toString());

        List<CollectionBinding> colTags = mColBindingRepo.findByTags(Arrays.asList("hello"), null);
        Assert.assertNotNull(colTags);
        Assert.assertEquals("colTags count is", 2, colTags.size());
        for (CollectionBinding tag : colTags) {
            logger.info(tag.toString());
        }
    }
}
