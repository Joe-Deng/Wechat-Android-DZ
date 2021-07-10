package com.example.wechatproj.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FriendRequest {
    @NonNull
    @PrimaryKey
    private String username;
    @ColumnInfo(name="headPicPath")
    private String headPicPath;
    @ColumnInfo(name="name")
    private String name;
    @ColumnInfo(name="text")
    private String text;
    @ColumnInfo(name="STATUS")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHeadPicPath() {
        return headPicPath;
    }

    public void setHeadPicPath(String headPicPath) {
        this.headPicPath = headPicPath;
    }

    public String getName() {
        return name;
    }

    public FriendRequest(String headPicPath, String username, String name, String text,String status) {
        this.headPicPath = headPicPath;
        this.username = username;
        this.name = name;
        this.text = text;
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
