package com.example.wechatproj.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.wechatproj.Database.Entity.FriendRequest;

import java.util.List;

@Dao
public interface FriendRequestDao {
    @Insert
    void insertFriendRequest(FriendRequest...friendRequests);

    @Delete
    void deleteFriendRequest(FriendRequest...friendRequests);

    //根据好友账号删除好友请求
    @Query("DELETE FROM FRIENDREQUEST WHERE USERNAME=:username")
    void deleteFriendRequestByUsername(String username);

    //查询所有好友请求
    @Query("SELECT * FROM FRIENDREQUEST")
    LiveData<List<FriendRequest>> findAllFriendRequests();

    //查询所有未处理的请求
    @Query("SELECT * FROM FRIENDREQUEST WHERE STATUS=:status")
    LiveData<List<FriendRequest>> findAllNewFriendRequestsLive(String status);

    //修改好友请求状态为同意
    @Query("UPDATE FRIENDREQUEST SET STATUS='yes' WHERE USERNAME=:username")
    void yesFriendRequest(String username);
    //修改好友请求状态为拒绝
    @Query("UPDATE FRIENDREQUEST SET STATUS='no' WHERE USERNAME=:username")
    void noFriendRequest(String username);

    //查询具体某一个用户的好友请求
    @Query("SELECT * FROM FRIENDREQUEST WHERE USERNAME=:username")
    FriendRequest findFriendRequestByUsername(String username);
}
