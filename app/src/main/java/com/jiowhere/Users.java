package com.jiowhere;

public class Users {
    private String image;
    private String userId;


    public Users() {
    }

    public Users(String userId, String image) {
        this.userId = userId;
        this.image = image;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
