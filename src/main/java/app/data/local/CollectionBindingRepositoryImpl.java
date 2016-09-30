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
package app.data.local;

import app.data.model.CollectionBinding;
import com.mongodb.BasicDBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component("CollectionBindingRepository")
public class CollectionBindingRepositoryImpl implements CollectionBindingRepository {
    private final MongoTemplate mMongoTemplate;

    @Autowired
    public CollectionBindingRepositoryImpl(MongoTemplate mongoTemplate) {
        mMongoTemplate = mongoTemplate;
    }

    @Override
    public void addFavor(long colId, @Nonnull CollectionBinding.Data data) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        Update update = new Update();
        update.addToSet("favors", data);

        mMongoTemplate.upsert(query, update, CollectionBinding.class);
    }

    @Override
    public void removeFavor(long colId, String uid) {
        Query query = new Query();
        query.addCriteria(
            Criteria.where("collectionId").is(colId).and("favors").elemMatch(Criteria.where("uid").is(uid)));

        Update update = new Update();
        //update.unset("dataList.$");
        //mMongoTemplate.upsert(query, update, Favor.class);
        // http://stackoverflow.com/questions/35600557/mongodb-how-using-spring-cryteria-remove-element-from-nested-object-array
        // http://ufasoli.blogspot.fr/2012/09/mongodb-spring-data-remove-elements.html?view=sidebar
        update.pull("favors", new BasicDBObject("uid", uid));
        mMongoTemplate.updateMulti(query, update, CollectionBinding.class);
    }

    @Override
    public void updateTags(long colId, @Nullable List<String> tags) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        Update update = new Update();
        update.set("tags", tags);

        mMongoTemplate.upsert(query, update, CollectionBinding.class);
    }

    @Nullable
    @Override
    public CollectionBinding findByCollectionId(long colId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        return mMongoTemplate.findOne(query, CollectionBinding.class);
    }

    @Nullable
    @Override
    public List<CollectionBinding> findByUid(String uid) {
        Query query = new Query();
        query.addCriteria(Criteria.where("favors").elemMatch(Criteria.where("uid").is(uid)));

        // TODO: 2016/9/29 add a test
        return mMongoTemplate.find(query, CollectionBinding.class);
    }

    @Nullable
    @Override
    public List<CollectionBinding> findByTags(List<String> tags, @Nullable Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(tags)).with(pageable);

        return mMongoTemplate.find(query, CollectionBinding.class);
    }

    @Override
    public void dropAll() {
        mMongoTemplate.dropCollection(CollectionBinding.class);
    }
}
