package com.example.wechatproj.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Friend {

//    @NonNull
//    @PrimaryKey(autoGenerate = true)
//    private int NO;

    @NonNull
    @PrimaryKey
    private String username;

    @ColumnInfo(name = "nickname")
    private String nickname;

    @ColumnInfo(name = "sex")
    private String sex;

    @ColumnInfo(name = "country")
    private String country;

    @ColumnInfo(name = "province")
    private String province;

    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "headPicPath")
    private String headPicPath;

    public Friend(String username, String nickname, String sex, String country, String province, String city, String headPicPath) {
        this.username = username;
        this.nickname = nickname;
        this.sex = sex;
        this.country = country;
        this.province = province;
        this.city = city;
        this.headPicPath = headPicPath;
    }

//    public int getNO() {
//        return NO;
//    }
//
//    public void setNO(int NO) {
//        this.NO = NO;
//    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHeadPicPath() {
        return headPicPath;
    }

    public void setHeadPicPath(String headPicPath) {
        this.headPicPath = headPicPath;
    }
}
