package com.example.wechatproj.Database.Entity;

public class Comment {
    String SID;
    String RID;
    String text;

    public Comment(String SID, String RID, String text) {
        this.SID = SID;
        this.RID = RID;
        this.text = text;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
