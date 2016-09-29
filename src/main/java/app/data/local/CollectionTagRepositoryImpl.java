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

import app.data.model.CollectionTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component("collectionTagRepository")
public class CollectionTagRepositoryImpl implements CollectionTagRepository {
    private final MongoTemplate mMongoTemplate;

    @Autowired
    public CollectionTagRepositoryImpl(MongoTemplate mongoTemplate) {
        mMongoTemplate = mongoTemplate;
    }

    @Override
    public void update(CollectionTag colTag) {
        if (colTag == null || colTag.getCollectionId() < 0) {
            return;
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colTag.getCollectionId()));

        Update update = new Update();
        update.set("tags", colTag.getTags());

        mMongoTemplate.upsert(query, update, CollectionTag.class);
    }

    @Nullable
    @Override
    public CollectionTag findByCollectionId(long colId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        return mMongoTemplate.findOne(query, CollectionTag.class);
    }

    @Nullable
    @Override
    public List<CollectionTag> findByTags(List<String> tags, @Nullable Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(tags)).with(pageable);

        return mMongoTemplate.find(query, CollectionTag.class);
    }

    public void dropAll() {
        mMongoTemplate.dropCollection(CollectionTag.class);
    }
}
