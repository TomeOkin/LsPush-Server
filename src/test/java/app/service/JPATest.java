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
import app.data.local.CollectionRepository;
import app.data.local.LinkRepository;
import app.data.local.UserRepository;
import app.data.model.Collection;
import app.data.model.Image;
import app.data.model.Link;
import app.data.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class JPATest {
    private static final Logger logger = LoggerFactory.getLogger(JPATest.class);
    @Autowired UserRepository mUserRepo;
    @Autowired LinkRepository mLinkRepo;
    @Autowired CollectionRepository mColRepo;
    @Autowired CollectionService mColService;
    @Autowired ObjectMapper mObjectMapper;

    @Test
    @Transactional
    public void jpaTest() {
        User user = new User();
        user.setUid("abcd");
        user.setNickname("acdf");
        user.setEmail("123456@qq.com");
        user.setPassword("abc123456");
        user.setValidate(User.EMAIL_VALID);
        mUserRepo.save(user);

        Link link = new Link("https://www.google.com", "Google");
        mLinkRepo.save(link);

        Collection collection = new Collection(user, link, "google search", new Image());
        mColRepo.save(collection);
        Assert.assertEquals(1, mColRepo.count());

        Page<Collection> collectionPage = mColRepo.findByUser(user, new PageRequest(0, 1));
        for (Collection col : collectionPage) {
            logger.info(col.toString());
        }

        List<Collection> collections = mColService.findByUser(user.getUid(), 0, 1);
        for (Collection col : collections) {
            logger.info(col.toString());
            Assert.assertNull(col.getUser());
        }
        Assert.assertNotNull(collections);
        Assert.assertEquals(1, collections.size());
        collection = collections.get(0);
        Assert.assertNotNull(collection);
        Assert.assertNotNull(collection.getId());
        mColRepo.delete(collection.getId());
        //mColRepo.removeByUserAndLink(collection.getUser(), collection.getLink());

        //mColRepo.delete(1L);
        //Assert.assertEquals(0, mColRepo.count());

        logger.info("collection count: {}", mColRepo.count());
        logger.info("user count: {}", mUserRepo.count());
        logger.info("link: {}", mLinkRepo.count());

        user = mUserRepo.findOne("abcd");
        Assert.assertNotNull(user);

        link = mLinkRepo.findFirstByUrl("https://www.google.com");
        Assert.assertNotNull(link);
    }

    @Test
    public void linkTest() throws IOException {
        Link link = new Link();
        link.setTitle("google");
        link.setUrl("https://www.google.com");
        String json = mObjectMapper.writeValueAsString(link);
        Link clone = mObjectMapper.readValue(json, Link.class);
        logger.info(clone.toString());
        Assert.assertNotNull(clone.getUrlUnique());
    }
}
