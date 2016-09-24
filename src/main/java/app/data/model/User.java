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

import app.data.model.internal.Pin;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class User implements Serializable {
    private static final long serialVersionUID = -7099274936845256485L;

    public static final int WITHOUT_VALID = 0;
    public static final int PHONE_VALID = 1;
    public static final int EMAIL_VALID = 2;
    public static final int PHONE_EMAIL_VALID = 3;

    @Id private String uid;
    private String nickname;
    private String email;
    private String phone;
    private String region; // 地区码, use for phone
    private String password; // it will not be send to client, but it can read from client.
    private int validate; // 00：未验证，01：手机号已验证，02：email已验证，03：手机号和 email都已验证
    private String image; // user image

    // it only use for database operation
    @JsonIgnore @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Collection> collections;

    @JsonIgnore @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Pin pin;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @JsonIgnore public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getValidate() {
        return validate;
    }

    public void setValidate(int validate) {
        this.validate = validate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public Pin getPin() {
        return pin;
    }

    public User cloneSelfPublic() {
        User user = new User();
        user.setUid(uid);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRegion(region);
        user.setImage(image);
        user.setValidate(validate);
        return user;
    }

    @Override public String toString() {
        return "User{" +
            "uid='" + uid + '\'' +
            ", nickname='" + nickname + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", region='" + region + '\'' +
            ", password='" + password + '\'' +
            ", validate=" + validate +
            '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return uid.equals(user.uid);

    }

    @Override public int hashCode() {
        return uid.hashCode();
    }
}
