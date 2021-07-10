package com.example.wechatproj.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class FriendCircle {
    @NonNull
    @PrimaryKey
    Long M_Time;
    @ColumnInfo(name = "SID")
    String SID;
    @ColumnInfo(name = "nickname")
    String nickname;
    @ColumnInfo(name = "headPicPath")
    String headPicPath;
    @ColumnInfo(name = "text")
    String text;
    @ColumnInfo(name = "imagePath")
    String imagePath;
    @ColumnInfo(name = "comments")
    String comments;        //  以json格式存储，存取都需要转化

    @NonNull
    public Long getM_Time() {
        return M_Time;
    }

    public void setM_Time(@NonNull Long m_Time) {
        M_Time = m_Time;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadPicPath() {
        return headPicPath;
    }

    public void setHeadPicPath(String headPicPath) {
        this.headPicPath = headPicPath;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public FriendCircle(@NonNull Long M_Time, String SID, String nickname, String headPicPath, String text, String imagePath, String comments) {
        this.M_Time = M_Time;
        this.SID = SID;
        this.nickname = nickname;
        this.headPicPath = headPicPath;
        this.text = text;
        this.imagePath = imagePath;
        this.comments = comments;
    }
}
