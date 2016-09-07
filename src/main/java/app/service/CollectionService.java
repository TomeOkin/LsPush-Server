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

import app.data.local.CollectionRepository;
import app.data.local.LinkRepository;
import app.data.model.Collection;
import app.data.model.Link;
import app.data.model.User;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final LinkRepository linkRepository;

    @Autowired
    public CollectionService(CollectionRepository collectionRepository, LinkRepository linkRepository) {
        this.collectionRepository = collectionRepository;
        this.linkRepository = linkRepository;
    }

    public List<Collection> findByUser(String uid, int page, int size, Sort sort) {
        if (sort.getOrderFor("createDate") != null || sort.getOrderFor("updateDate") != null) {
            if (sort.getOrderFor("id") == null) {
                sort = sort.and(new Sort(Sort.Direction.DESC, "id"));
            }
        }

        User user = new User();
        user.setUid(uid);
        Page<Collection> colPage = collectionRepository.findByUser(user, new PageRequest(page, size, sort));
        List<Collection> collections = new ArrayList<>(colPage.getContent().size());
        // set user field null, because what we find is just for one user
        // otherwise, when we need to change the data but not want to apply to the database, do like follow
        colPage.forEach(
            col -> {
                Collection collection = new Collection(null, col.getLink(), col.getDescription(), col.getImage());
                collection.setId(col.getId());
                collection.setCreateDate(col.getCreateDate());
                collection.setUpdateDate(col.getUpdateDate());
                collections.add(collection);
            });
        return collections;
    }

    public boolean isExistCollection(long id) {
        return collectionRepository.findOne(id) != null;
    }

    @Transactional
    public void postCollection(String uid, Collection collection) {
        User user = new User();
        user.setUid(uid);
        collection.setUser(user);

        Link link = collection.getLink();
        if (StringUtils.isEmpty(link.getUrlUnique())) {
            link = new Link(link.getUrl(), link.getTitle()); // update link, add unique code
        }
        Link one = linkRepository.findFirstByUrl(link.getUrl());
        if (one == null) {
            linkRepository.save(link);
        }
        collection.setLink(link);

        Collection col = collectionRepository.findOneByUserAndLink(user, link);
        if (col == null) {
            collection.setCreateDate(DateTime.now().toDate());
        }
        collection.setUpdateDate(DateTime.now().toDate());
        collectionRepository.save(collection);
    }

    public Collection findByID(long id) {
        return collectionRepository.findOne(id);
    }
}
