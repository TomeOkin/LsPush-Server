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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class JPATest {
    private static final Logger logger = LoggerFactory.getLogger(JPATest.class);
    @Autowired UserRepository userRepository;
    @Autowired LinkRepository linkRepository;
    @Autowired CollectionRepository collectionRepository;
    @Autowired CollectionService collectionService;
    @Autowired ObjectMapper objectMapper;

    @Test
    @Transactional
    public void jpaTest() {
        User user = new User();
        user.setUid("abcd");
        user.setNickname("acdf");
        user.setEmail("123456@qq.com");
        user.setPassword("abc123456");
        user.setValidate(User.EMAIL_VALID);
        userRepository.save(user);

        Link link = new Link("https://www.google.com", "Google");
        linkRepository.save(link);

        Collection collection = new Collection(user, link, "google search", "");
        collectionRepository.save(collection);
        Assert.assertEquals(1, collectionRepository.count());

        collection = new Collection(user, link, "google search", "");
        Page<Collection> collectionPage = collectionRepository.findByUser(user, new PageRequest(0, 1));
        for (Collection col : collectionPage) {
            logger.info(col.toString());
        }

        List<Collection> collections =
            collectionService.findByUser(user.getUid(), 0, 1, new Sort(Sort.Direction.DESC, "createDate"));
        for (Collection col : collections) {
            logger.info(col.toString());
            Assert.assertNull(col.getUser());
        }

        collectionRepository.removeByUserAndLink(collection.getUser(), collection.getLink());

        //collectionRepository.delete(1L);
        //Assert.assertEquals(0, collectionRepository.count());

        logger.info("collection count: {}", collectionRepository.count());
        logger.info("user count: {}", userRepository.count());
        logger.info("link: {}", linkRepository.count());

        user = userRepository.findOne("abcd");
        Assert.assertNotNull(user);

        link = linkRepository.findFirstByUrl("https://www.google.com");
        Assert.assertNotNull(link);
    }

    @Test
    public void linkTest() throws IOException {
        Link link = new Link();
        link.setTitle("google");
        link.setUrl("https://www.google.com");
        String json = objectMapper.writeValueAsString(link);
        Link clone = objectMapper.readValue(json, Link.class);
        logger.info(clone.toString());
        Assert.assertNotNull(clone.getUrlUnique());
    }
}
