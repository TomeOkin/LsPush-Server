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
        User user = new User();
        user.setUid(uid);
        col.setUser(user);

        Link link = col.getLink();
        if (StringUtils.isEmpty(link.getUrlUnique())) {
            link = link.cloneSelf(); // update link, add unique code
        }
        Link one = mLinkRepo.findFirstByUrl(link.getUrl());
        if (one == null) {
            mLinkRepo.save(link);
        }
        col.setLink(link);

        Collection old = mColRepo.findOneByUserAndLink(user, link);
        if (old == null) {
            col.setCreateDate(DateTime.now().toDate());
        } else {
            col.setCreateDate(old.getCreateDate());
        }
        col.setUpdateDate(DateTime.now().toDate());
        mColRepo.save(col);
        // endregion

        // region: update collection tag
        mColBindingRepo.updateTags(col.getId(), col.getTags());
        // endregion
    }

    public List<Collection> findByUser(String uid, int page, int size) {
        User user = new User();
        user.setUid(uid);
        Page<Collection> colPage = mColRepo.findByUser(user, new PageRequest(page, size, mLatestSort));
        List<Collection> colList = getFromPage(colPage, false);
        colList.forEach(this::fillCollection);
        return colList;
    }

    public List<Collection> findByUrl(String url, int page, int size, Sort sort) {
        Link link = mLinkRepo.findFirstByUrl(url);
        Page<Collection> colPage = mColRepo.findByLink(link, new PageRequest(page, size, sort));
        List<Collection> colList = getFromPage(colPage, true);
        colList.forEach(this::fillCollection);
        return colList;
    }

    public List<Collection> getLatestCollections(int page, int size) {
        Page<Collection> colPage = mColRepo.findAll(new PageRequest(page, size, mLatestSort));
        List<Collection> colList = getFromPage(colPage, true);
        colList.forEach(this::fillCollection);
        return colList;
    }

    public List<Collection> findByTags(List<String> tags, int page, int size) {
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
            col.setTags(item.getTags());
            // set explorers
            fillExplorers(col);

            colList.add(col);
        }
        return colList;
    }

    private void fillCollection(Collection col) {
        fillBinding(col);
        fillExplorers(col);
    }

    private void fillBinding(Collection col) {
        CollectionBinding colBinding = mColBindingRepo.findByCollectionId(col.getId());
        if (colBinding == null) {
            col.setFavorCount(0);
            col.setTags(null);
        } else {
            final long count = colBinding.getFavors() != null ? colBinding.getFavors().size() : 0;
            col.setFavorCount(count);
            col.setTags(colBinding.getTags());
        }
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
