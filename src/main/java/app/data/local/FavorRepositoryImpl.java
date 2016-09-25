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

import app.data.model.Favor;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("favorRepository")
public class FavorRepositoryImpl implements FavorRepository {
    private static final Logger logger = LoggerFactory.getLogger(FavorRepositoryImpl.class);
    private final MongoTemplate mongoTemplate;

    @Autowired
    public FavorRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override public void addFavor(long colId, Favor.Data data) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        Update update = new Update();
        update.addToSet("dataList", data);

        mongoTemplate.upsert(query, update, Favor.class);
    }

    @Override public void removeFavor(long colId, String uid) {
        Query query = new Query();
        query.addCriteria(
            Criteria.where("collectionId").is(colId).and("dataList").elemMatch(Criteria.where("uid").is(uid)));

        Update update = new Update();
        //update.unset("dataList.$");
        //mongoTemplate.upsert(query, update, Favor.class);
        // http://stackoverflow.com/questions/35600557/mongodb-how-using-spring-cryteria-remove-element-from-nested-object-array
        // http://ufasoli.blogspot.fr/2012/09/mongodb-spring-data-remove-elements.html?view=sidebar
        update.pull("dataList", new BasicDBObject("uid", uid));
        mongoTemplate.updateMulti(query, update, Favor.class);
    }

    @Override public List<Favor.Data> findFavor(long colId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        Favor favor = mongoTemplate.findOne(query, Favor.class);
        return favor != null ? favor.dataList : null;
    }

    @Override public void dropAll() {
        mongoTemplate.dropCollection(Favor.class);
    }

}
