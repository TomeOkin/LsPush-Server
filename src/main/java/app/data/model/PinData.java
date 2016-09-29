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

import javax.validation.constraints.NotNull;
import java.util.Date;

public class PinData {
    @NotNull private Collection collection;
    private Date pinDate;

    public PinData() {}

    public PinData(Collection collection, Date pinDate) {
        this.collection = collection;
        this.pinDate = pinDate;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Date getPinDate() {
        return pinDate;
    }

    public void setPinDate(Date pinDate) {
        this.pinDate = pinDate;
    }

    @Override
    public String toString() {
        return "PinData{" +
            "collection=" + collection +
            ", pinDate=" + pinDate +
            '}';
    }
}
