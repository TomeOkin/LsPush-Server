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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(CollectionBindingRepositoryImpl.class);
    private final MongoTemplate mMongoTemplate;

    @Autowired
    public CollectionBindingRepositoryImpl(MongoTemplate mongoTemplate) {
        mMongoTemplate = mongoTemplate;
    }

    @Override
    public void addFavor(long colId, String uid, @Nonnull CollectionBinding.Data data) {
        if (hasExistFavor(colId, data)) {
            return;
        }
        data.uid = uid;

        Query query = new Query();
        query.addCriteria(Criteria.where("collectionId").is(colId));

        Update update = new Update();
        update.addToSet("favors", data);

        mMongoTemplate.upsert(query, update, CollectionBinding.class);
    }

    private boolean hasExistFavor(long colId, @Nonnull CollectionBinding.Data data) {
        Query query = new Query();
        query.addCriteria(
            Criteria.where("collectionId").is(colId).and("favors").elemMatch(Criteria.where("uid").is(data.uid)));
        return mMongoTemplate.findOne(query, CollectionBinding.class) != null;
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
    public List<CollectionBinding> findByUid(String uid, @Nullable Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("favors").elemMatch(Criteria.where("uid").is(uid))).with(pageable);

        return mMongoTemplate.find(query, CollectionBinding.class);
    }

    @Nullable
    @Override
    public List<CollectionBinding> findByTags(List<String> tags, @Nullable Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tags").in(tags)).with(pageable);
        return mMongoTemplate.find(query, CollectionBinding.class);

        //        // such as [{$project: {key:"$key", count: {$size: {$ifNull: ["$value",[]]}}}}, {$sort: {"count":1}}]
        //        // FIXME: 2016/9/30 [block] waiting for spring-data-mongodb 1.10 release
        //        // https://github.com/spring-projects/spring-data-mongodb/blob/eb1392cc1a0fcf25aa9e636863d36e6fee73e632/src/main/asciidoc/new-features.adoc
        //        // due to $ifNull
        //        List<AggregationOperation> operations = new ArrayList<>();
        //        operations.add(Aggregation.match(Criteria.where("tags").in(tags)));
        //
        //        String op = "{ $project : { \"collectionId\" : \"$collectionId\" , \"favorsCount\" : { $size : { \"$ifNull\" : [\"$favors\", []]}}}}";
        //        // but the result will be modify to ...{ "$ifNull" : [ "$favors" , { }]}...
        //        DBObject dbObject = BasicDBObject.parse(op);
        //        operations.add(new CommonAggregationOperation(dbObject));
        ////        operations.add(Aggregation.project("collectionId")
        ////            .and(ifNull("favors", new ArrayList<CollectionBinding.Data>())).size().as("favorsCount"));
        //        //operations.add(Aggregation.sort(Sort.Direction.DESC, "favorsCount"));
        //        if (pageable != null) {
        //            operations.add(Aggregation.skip(pageable.getOffset()));
        //            operations.add(Aggregation.limit(pageable.getPageSize()));
        //        }
        //        TypedAggregation<CollectionBinding> aggregation = Aggregation.newAggregation(CollectionBinding.class, operations);
        //        logger.info(aggregation.toString());
        ////        Aggregation aggregation = Aggregation.newAggregation(operations);
        //        AggregationResults<CollectionBindingTemp> results =
        //            mMongoTemplate.aggregate(aggregation, CollectionBindingTemp.class);
        //        List<CollectionBindingTemp> tempList = results.getMappedResults();
        //
        //        List<CollectionBinding> colBindings = new ArrayList<>(tempList.size());
        //        for (CollectionBindingTemp item : tempList) {
        //            CollectionBinding colBinding = findByCollectionId(item.getCollectionId());
        //            // colBinding is non-null
        //            colBindings.add(colBinding);
        //        }
        //        return colBindings;
    }

//    public static IfNullOperator ifNull(String field, Object replacement) {
//        return IfNullOperator.newBuilder().ifNull(field).thenReplaceWith(replacement);
//    }

    @Override
    public void dropAll() {
        mMongoTemplate.dropCollection(CollectionBinding.class);
    }
}
