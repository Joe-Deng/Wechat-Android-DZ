package com.example.wechatproj.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.wechatproj.Database.Entity.FriendCircle;

import java.util.List;

@Dao
public interface FriendCircleDao{
    @Insert
    void insertFriendCircle(FriendCircle...friendCircles);

    @Update
    void updateFriendCircle(FriendCircle...friendCircles);

    @Delete
    void deleteFriendCircle(FriendCircle...friendCircles);

    @Query("SELECT * FROM FRIENDCircle ORDER BY M_Time DESC")
    LiveData<List<FriendCircle>> getAllFriendCircleLive();

    @Query("SELECT * FROM FRIENDCircle WHERE SID = :username")
    FriendCircle getFriendCircleBySID(String username);

    @Query("SELECT * FROM FriendCircle WHERE M_Time=:M_Time")
    FriendCircle getFriendCircleByTime(Long M_Time);
}
