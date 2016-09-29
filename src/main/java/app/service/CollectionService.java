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
import app.data.local.CollectionTagRepository;
import app.data.local.FavorRepository;
import app.data.local.LinkRepository;
import app.data.model.*;
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
    private final CollectionRepository mColRepo;
    private final LinkRepository mLinkRepo;
    private final FavorRepository mFavorRepo;
    private final CollectionTagRepository mColTagRepo;
    private final Sort mCreateSort;
    private final Sort mLatestSort;

    @Autowired
    public CollectionService(CollectionRepository colRepo, LinkRepository linkRepo, FavorRepository favorRepo,
        CollectionTagRepository colTagRepo) {
        mColRepo = colRepo;
        mLinkRepo = linkRepo;
        mFavorRepo = favorRepo;
        mColTagRepo = colTagRepo;
        mCreateSort = new Sort(Sort.Direction.ASC, "createDate", "id");
        mLatestSort = new Sort(Sort.Direction.DESC, "updateDate", "id");
    }

    public List<Collection> findByUser(String uid, int page, int size) {
        User user = new User();
        user.setUid(uid);
        Page<Collection> colPage = mColRepo.findByUser(user, new PageRequest(page, size, mLatestSort));
        return getFromPage(colPage, false);
    }

    public Collection findByID(long id) {
        return mColRepo.findOne(id);
    }

    public boolean isExistCollection(long id) {
        return mColRepo.findOne(id) != null;
    }

    @Transactional
    public void postCollection(String uid, Collection collection) {
        // region: update collection
        User user = new User();
        user.setUid(uid);
        collection.setUser(user);

        Link link = collection.getLink();
        if (StringUtils.isEmpty(link.getUrlUnique())) {
            link = link.cloneSelf(); // update link, add unique code
        }
        Link one = mLinkRepo.findFirstByUrl(link.getUrl());
        if (one == null) {
            mLinkRepo.save(link);
        }
        collection.setLink(link);

        Collection col = mColRepo.findOneByUserAndLink(user, link);
        if (col == null) {
            collection.setCreateDate(DateTime.now().toDate());
        } else {
            collection.setCreateDate(col.getCreateDate());
        }
        collection.setUpdateDate(DateTime.now().toDate());
        mColRepo.save(collection);
        // endregion

        // region: update collection tag
        CollectionTag colTag = new CollectionTag(collection.getId(), collection.getTags());
        mColTagRepo.update(colTag);
        // endregion
    }

    public List<Collection> findCollection(String url, int page, int size, Sort sort) {
        Link link = mLinkRepo.findFirstByUrl(url);
        Page<Collection> colPage = mColRepo.findByLink(link, new PageRequest(page, size, sort));
        List<Collection> collectionList = getFromPage(colPage, true);
        fillCollection(collectionList);
        return  collectionList;
    }

    public List<Collection> getLatestCollection(int page, int size) {
        Page<Collection> collectionPage = mColRepo.findAll(new PageRequest(page, size, mLatestSort));
        List<Collection> collectionList = getFromPage(collectionPage, true);
        fillCollection(collectionList);
        return collectionList;
    }

    private void fillCollection(List<Collection> collectionList) {
        for (Collection collection : collectionList) {
            // region: collection favor count
            Favor favor = mFavorRepo.findFavor(collection.getId());
            final long count = favor != null && favor.dataList != null ? favor.dataList.size() : 0;
            collection.setFavorCount(count);
            // endregion

            // region: link explorers
            Link link = mLinkRepo.findFirstByUrl(collection.getLink().getUrl());
            // 默认只提供前 5 个发现者
            Page<Collection> linkCols = mColRepo.findByLink(link, new PageRequest(0, 5, mCreateSort));
            List<User> users = new ArrayList<>(linkCols.getContent().size());
            linkCols.forEach(col -> {
                User user = col.getUser();
                user = user.cloneProfile();
                user.setColId(col.getId());
                users.add(user);
            });
            collection.setExplorers(users);
            // endregion

            // region: collection tags
            CollectionTag colTag = mColTagRepo.findByCollectionId(collection.getId());
            List<String> tags = colTag != null ? colTag.getTags() : null;
            collection.setTags(tags);
            // endregion
        }
    }

    private List<Collection> getFromPage(Page<Collection> colPage, boolean withUser) {
        List<Collection> collections = new ArrayList<>(colPage.getContent().size());
        // set user field null, because what we find is just for one user
        // otherwise, when we need to change the data but not want to apply to the database, do like follow
        colPage.forEach(
            col -> {
                Collection collection = new Collection(null, col.getLink(), col.getDescription(), col.getImage());
                if (withUser) {
                    collection.setUser(col.getUser().cloneProfile());
                }
                collection.setId(col.getId());
                collection.setCreateDate(col.getCreateDate());
                collection.setUpdateDate(col.getUpdateDate());
                collections.add(collection);
            });
        return collections;
    }
}
