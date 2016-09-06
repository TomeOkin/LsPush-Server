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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class Collection implements Serializable {
    private static final long serialVersionUID = 6379948649053059650L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;
    @ManyToOne @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "collection_user_id_constraint")) private User user;
    @ManyToOne @JoinColumn(name = "link_id", foreignKey = @ForeignKey(name = "collection_link_id_constraint")) private Link link;

    private String description;
    private String image;

    @JsonIgnoreProperties(allowGetters = true) @Column(name = "create_date") @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @JsonIgnoreProperties(allowGetters = true) @Column(name = "update_date") @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    public Collection() {
    }

    public Collection(User user, Link link, String description, String image) {
        this.user = user;
        this.link = link;
        this.description = description;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collection that = (Collection) o;

        return id == that.id;

    }

    @Override public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override public String toString() {
        return "Collection{" +
            "id=" + id +
            ", user=" + user +
            ", link=" + link +
            ", description='" + description + '\'' +
            ", image='" + image + '\'' +
            ", createDate=" + createDate +
            ", updateDate=" + updateDate +
            '}';
    }
}
