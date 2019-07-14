package com.jiowhere;

public class Friends {
    private String name;

    public Friends(String name, String userId, String image) {
        this.name = name;
        this.userId = userId;
        this.image = image;
    }

    private String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Friends() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String image;
}
