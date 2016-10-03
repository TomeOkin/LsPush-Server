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
package app.data.model.internal;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document
public class CollectionBindingTemp {
    @Id private long collectionId;
    private int favorsCount;

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public int getFavorsCount() {
        return favorsCount;
    }

    public void setFavorsCount(int favorsCount) {
        this.favorsCount = favorsCount;
    }
}
