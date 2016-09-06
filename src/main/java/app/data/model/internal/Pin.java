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

import app.data.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Pin implements Serializable {
    private static final long serialVersionUID = -1516276877849657104L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;
    private String pins;

    @JsonIgnoreProperties(allowGetters = true) @Column(name = "pin_date") @Temporal(TemporalType.TIMESTAMP)
    private Date pinDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "pin_user_id_constraint")) private User user;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPins() {
        return pins;
    }

    public void setPins(String pins) {
        this.pins = pins;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override public String toString() {
        return "Pin{" +
            "id=" + id +
            ", pins='" + pins + '\'' +
            ", pinDate=" + pinDate +
            ", user=" + user +
            '}';
    }
}
