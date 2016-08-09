package com.umanets.seconfdemoapp.model;

/**
 * Created by ko3ak_zhn on 8/9/16.
 */
public class UserModel {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_profile() {
        return photo_profile;
    }

    public void setPhoto_profile(String photo_profile) {
        this.photo_profile = photo_profile;
    }

    private String name;

    public UserModel(String id, String name, String photo_profile) {
        this.id = id;
        this.name = name;
        this.photo_profile = photo_profile;
    }

    private String photo_profile;
}
