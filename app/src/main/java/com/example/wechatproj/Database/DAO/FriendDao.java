package com.example.wechatproj.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wechatproj.Database.Entity.Friend;

import java.util.List;

@Dao
public interface FriendDao {
    @Insert
    void insertFriend(Friend...friends);

    @Update
    void updateFriend(Friend...friends);

    @Delete
    void deleteFriend(Friend...friends);

    @Query("SELECT * FROM FRIEND")
    LiveData<List<Friend>>getAllFriendLive();

    @Query("SELECT * FROM FRIEND WHERE USERNAME = :username")
    Friend getFriend(String username);

    @Query("SELECT * FROM FRIEND WHERE USERNAME = :username")
    LiveData<Friend> getFriendLive(String username);
}
