package com.example.wechatproj.Database.Entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {
    @NonNull
    @PrimaryKey
    private long M_Time; //发送时间（排列依据）

    @ColumnInfo(name = "RID")
    private String RID;     // 接收者username

    @ColumnInfo(name = "SID")
    private String SID;     //发送者username

    @ColumnInfo(name = "M_Type")
    private int M_Type; //消息类型（详见服务器数据库表）

    @ColumnInfo(name = "M_Content")
    private String M_Content; //消息内容

    @ColumnInfo(name = "IF_Send")
    private Boolean IF_Send;

    @ColumnInfo(name = "IF_Readed")
    private Boolean IF_Readed;

    public Boolean getIF_Readed() {
        return IF_Readed;
    }

    public void setIF_Readed(Boolean IF_Readed) {
        this.IF_Readed = IF_Readed;
    }

    public Boolean getIF_Send() {
        return IF_Send;
    }

    public void setIF_Send(Boolean IF_Send) {
        this.IF_Send = IF_Send;
    }

    public long getM_Time() {
        return M_Time;
    }

    public void setM_Time(long m_Time) {
        M_Time = m_Time;
    }

    public Message(long M_Time,String RID,String SID,int M_Type,String M_Content,Boolean IF_Send,Boolean IF_Readed) {
        this.M_Time = M_Time;
        this.RID = RID;
        this.SID = SID;
        this.M_Type = M_Type;
        this.M_Content = M_Content;
        this.IF_Send = IF_Send;
        this.IF_Readed = IF_Readed;
    }

    public String getM_Content() {
        return M_Content;
    }

    public void setM_Content(String m_Content) {
        M_Content = m_Content;
    }

    public int getM_Type() {
        return M_Type;
    }

    public void setM_Type(int m_Type) {
        M_Type = m_Type;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }


}
