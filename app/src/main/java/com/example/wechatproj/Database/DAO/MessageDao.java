package com.example.wechatproj.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wechatproj.Database.Entity.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    void insertMessage(Message... messages);

    @Update
    void updateMessage(Message... messages);

    @Delete
    void deleteMessage(Message... messages);

    //一般用于查询某个好友的全部消息
    //按时间戳从旧到新排列
    @Query("SELECT * FROM MESSAGE WHERE SID=:username OR RID=:username ORDER BY M_Time")
    LiveData<List<Message>>getAllMessageLive(String username);

    @Query("SELECT * FROM MESSAGE WHERE SID=:username OR RID=:username ORDER BY M_Time DESC")
    List<Message> getAllMessages(String username);

    //查询具体某一条消息
    @Query("SELECT * FROM MESSAGE WHERE M_Time=:M_Time")
    Message findMessage(long M_Time);

    //查询所有新消息
    @Query("SELECT * FROM MESSAGE WHERE IF_Readed=0")
    LiveData<List<Message>> findNewMessages();

    //查询某个好友的新消息(倒叙，方便找到第一条消息用作显示）
    @Query("SELECT * FROM MESSAGE WHERE IF_Readed=0 AND SID=:username ORDER BY M_Time DESC")
    LiveData<List<Message>> findNewMessageOf(String username);


    //清空好友的所有消息，可以用于删除好友时调用
    @Query("DELETE FROM MESSAGE WHERE SID=:username")
    void deleteAllMessageOfFriend(String username);

    //读完一个好友所有的消息
    @Query("UPDATE MESSAGE SET IF_Readed=1 WHERE SID=:username")
    void readMessages(String username);

    @Query("SELECT * From Message WHERE M_Time =(SELECT MAX(M_Time) FROM MESSAGE WHERE SID=:username OR RID=:username)")
    Message getLastMessage(String username);





//    @Query("SELECT * FROM MESSAGE WHERE RID=:username OR SID=:username ORDER BY M_Time DESC" )
//    LiveData<List<Message>> findAllMessageOf(String username);

}
