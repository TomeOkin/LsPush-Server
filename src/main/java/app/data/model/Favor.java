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
import java.util.Date;
import java.util.List;

@Document
public class Favor {
    @Id public long collectionId;
    public List<Data> dataList;

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
    }

    @Override
    public String toString() {
        return "Favor{" +
            "collectionId=" + collectionId +
            ", dataList=" + dataList +
            '}';
    }
}
