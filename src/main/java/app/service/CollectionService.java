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

import app.data.local.CollectionBindingRepository;
import app.data.local.CollectionRepository;
import app.data.local.LinkRepository;
import app.data.model.Collection;
import app.data.model.CollectionBinding;
import app.data.model.Link;
import app.data.model.User;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    private final CollectionRepository mColRepo;
    private final LinkRepository mLinkRepo;
    private final CollectionBindingRepository mColBindingRepo;
    private final Sort mCreateSort;
    private final Sort mLatestSort;

    @Autowired
    public CollectionService(CollectionRepository colRepo, LinkRepository linkRepo,
        CollectionBindingRepository colBindingRepo) {
        mColRepo = colRepo;
        mLinkRepo = linkRepo;
        mColBindingRepo = colBindingRepo;
        mCreateSort = new Sort(Sort.Direction.ASC, "createDate", "id");
        mLatestSort = new Sort(Sort.Direction.DESC, "updateDate", "id");
    }

    public Collection findByID(long id) {
        return mColRepo.findOne(id);
    }

    public boolean isExistCollection(long id) {
        return mColRepo.findOne(id) != null;
    }

    @Transactional
    public void postCollection(String uid, Collection col) {
        // region: update collection
        User user = prepareUser(uid);
        col.setUser(user);

        Link link = prepareAndSaveLink(col.getLink());
        col.setLink(link);

        Collection old = mColRepo.findOneByUserAndLink(user, link);
        if (old == null) {
            col.setCreateDate(DateTime.now().toDate());
        } else {
            col.setId(old.getId());
            col.setCreateDate(old.getCreateDate());
        }
        col.setUpdateDate(DateTime.now().toDate());
        mColRepo.save(col);
        // endregion

        // region: update collection binding
        mColBindingRepo.updateTags(col.getId(), col.getTags());
        CollectionBinding.Data favor = new CollectionBinding.Data();
        favor.uid = uid;
        favor.date = DateTime.now().toDate();
        mColBindingRepo.addFavor(col.getId(), uid, favor);
        // endregion
    }

    private User prepareUser(String uid) {
        return new User(uid);
    }

    private Link prepareLink(@Nonnull Link old) {
        Link link = old;
        if (StringUtils.isEmpty(link.getUrlUnique())) {
            link = link.cloneSelf(); // update link, add unique code
        }
        return link;
    }

    private Link prepareAndSaveLink(Link old) {
        Link link = prepareLink(old);
        Link one = mLinkRepo.findFirstByUrl(link.getUrl());
        if (one == null) {
            mLinkRepo.save(link);
        }
        return link;
    }

    public Collection findByUserAndLink(String uid, String url) {
        User user = prepareUser(uid);
        Link one = mLinkRepo.findFirstByUrl(url);
        Collection col = null;
        if (one != null) {
            col = mColRepo.findOneByUserAndLink(user, one);
            if (col != null) {
                fillCollection(col, uid);
            }
        }

        return col;
    }

    public List<Collection> findByUser(String uid, int page, int size) {
        if (StringUtils.isEmpty(uid)) {
            return null;
        }

        User user = prepareUser(uid);
        Page<Collection> colPage = mColRepo.findByUser(user, new PageRequest(page, size, mLatestSort));
        List<Collection> colList = getFromPage(colPage, false);
        colList.forEach(collection -> fillCollection(collection, uid));
        return colList;
    }

    public List<Collection> findByUrl(@Nullable String uid, String url, int page, int size, Sort sort) {
        Link link = mLinkRepo.findFirstByUrl(url);
        Page<Collection> colPage = mColRepo.findByLink(link, new PageRequest(page, size, sort));
        List<Collection> colList = getFromPage(colPage, true);
        colList.forEach(collection -> fillCollection(collection, uid));
        return colList;
    }

    public List<Collection> getLatestCollections(String uid, int page, int size) {
        Page<Collection> colPage = mColRepo.findAll(new PageRequest(page, size, mLatestSort));
        List<Collection> colList = getFromPage(colPage, true);
        colList.forEach(collection -> fillCollection(collection, uid));
        return colList;
    }

    public List<Collection> findByTags(String uid, List<String> tags, int page, int size) {
        List<CollectionBinding> colBindings = mColBindingRepo.findByTags(tags, new PageRequest(page, size));
        if (colBindings == null) {
            return null;
        }
        List<Collection> colList = new ArrayList<>(colBindings.size());
        for (CollectionBinding item : colBindings) {
            Collection col = mColRepo.findOne(item.getCollectionId());
            // set favor count and tags
            final long count = item.getFavors() != null ? item.getFavors().size() : 0;
            col.setFavorCount(count);
            col.setHasFavor(hasFavor(uid, item.getFavors()));
            col.setTags(item.getTags());
            // set explorers
            fillExplorers(col);

            colList.add(col);
        }
        return colList;
    }

    private void fillCollection(Collection col, @Nullable String uid) {
        fillBinding(col, uid);
        fillExplorers(col);
        logger.error("col: {}", col.toString());
    }

    @SuppressWarnings("ConstantConditions")
    private void fillBinding(Collection col, @Nullable String uid) {
        CollectionBinding colBinding = mColBindingRepo.findByCollectionId(col.getId());
        final List<CollectionBinding.Data> favors = colBinding == null ? null : colBinding.getFavors();
        if (favors == null) {
            col.setFavorCount(0);
            col.setHasFavor(false);
            col.setTags(null);
        } else {
            col.setFavorCount(favors.size());
            final boolean favor = hasFavor(uid, favors);
            col.setHasFavor(favor);
            col.setTags(colBinding == null ? null : colBinding.getTags());
        }
    }

    private boolean hasFavor(String uid, List<CollectionBinding.Data> favors) {
        if (favors == null || StringUtils.isEmpty(uid)) {
            return false;
        }

        for (CollectionBinding.Data item : favors) {
            if (item.uid.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    private void fillExplorers(Collection col) {
        Link link = mLinkRepo.findFirstByUrl(col.getLink().getUrl());
        // 默认只提供前 5 个发现者
        Page<Collection> linkCols = mColRepo.findByLink(link, new PageRequest(0, 5, mCreateSort));
        List<User> users = new ArrayList<>(linkCols.getContent().size());
        linkCols.forEach(item -> {
            User user = item.getUser();
            user = user.cloneProfile();
            user.setColId(item.getId());
            users.add(user);
        });
        col.setExplorers(users);
    }

    private List<Collection> getFromPage(Page<Collection> colPage, boolean withUser) {
        List<Collection> collections = new ArrayList<>(colPage.getContent().size());
        // set user field null, because what we find is just for one user
        // otherwise, when we need to change the data but not want to apply to the database, do like follow
        colPage.forEach(
            item -> {
                Collection col = new Collection(null, item.getLink(), item.getDescription(), item.getImage());
                if (withUser) {
                    col.setUser(item.getUser().cloneProfile());
                }
                col.setId(item.getId());
                col.setCreateDate(item.getCreateDate());
                col.setUpdateDate(item.getUpdateDate());
                collections.add(col);
            });
        return collections;
    }
}
