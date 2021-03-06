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
package app.data.model;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document
public class CollectionBinding implements Serializable {
    private static final long serialVersionUID = 1814208429279250834L;

    @Id private long collectionId;
    private List<CollectionBinding.Data> favors;
    private List<String> tags;

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public List<CollectionBinding.Data> getFavors() {
        return favors;
    }

    public void setFavors(List<CollectionBinding.Data> favors) {
        this.favors = favors;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "CollectionBinding{" +
            "collectionId=" + collectionId +
            ", favors=" + favors +
            ", tags=" + tags +
            '}';
    }

    public static class Data {
        @Id public String uid;
        public Date date;

        @Override
        public String toString() {
            return "Data{" +
                "uid='" + uid + '\'' +
                ", date=" + date +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            return uid.equals(data.uid);

        }

        @Override
        public int hashCode() {
            return uid.hashCode();
        }
    }
}
