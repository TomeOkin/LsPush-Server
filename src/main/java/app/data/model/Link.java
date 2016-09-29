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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Hashing;

import javax.persistence.*;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Link implements Serializable {
    private static final long serialVersionUID = 6830880058118823515L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private long id;
    private String url;
    @Column(name = "url_unique") private String urlUnique;
    private String title;

    @JsonIgnore
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Collection> collections = new ArrayList<>();

    public Link() {}

    @JsonCreator
    public Link(@JsonProperty(value = "url") String url, @JsonProperty(value = "title") String title) {
        this.url = url;
        this.title = title;
        // 碰撞率大概为 1/(0.5% * 0.5%)，一般一张数据库表容纳大概 50 万条记录就达到极限，这个比率已经足够了
        this.urlUnique = Hashing.murmur3_128().hashString(url, StandardCharsets.UTF_8).toString() + Hashing.sipHash24()
            .hashString(url, StandardCharsets.UTF_8);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    public String getUrlUnique() {
        return urlUnique;
    }

    public void setUrlUnique(String urlUnique) {
        this.urlUnique = urlUnique;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public Link cloneSelf() {
        return new Link(url, title);
    }

    @Override
    public String toString() {
        return "Link{" +
            "id='" + id + '\'' +
            ", url='" + url + '\'' +
            ", urlUnique='" + urlUnique + '\'' +
            ", title='" + title + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (url != null ? !url.equals(link.url) : link.url != null) return false;
        return urlUnique != null ? urlUnique.equals(link.urlUnique) : link.urlUnique == null;

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (urlUnique != null ? urlUnique.hashCode() : 0);
        return result;
    }
}
