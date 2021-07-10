package com.example.wechatproj.Database.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.wechatproj.Database.Entity.FriendRequest;
import com.example.wechatproj.Database.Repository.FriendRequestRepository;

import java.util.List;

public class FriendRequestsViewModel extends AndroidViewModel {
  private FriendRequestRepository friendRequestRepository;
    Context context;


    public FriendRequestsViewModel(@NonNull Application application) {
        super(application);
        context = application;
    }

    public void setFriendRequestRepository(String username) {
        this.friendRequestRepository = new FriendRequestRepository(context,username);
    }

    public LiveData<List<FriendRequest>> getAllFriendRequestLive(){
        return friendRequestRepository.getAllFriendRequests();
    }

    public LiveData<List<FriendRequest>> getNewFriendRequestLive(){
        return friendRequestRepository.getNewFriendRequests();
    }
    //————————————方法区————————
    public void insetFriendRequest(FriendRequest friendRequest){
        friendRequestRepository.insetFriendRequest(friendRequest);
    }

    public void deleteFriendRequest(FriendRequest friendRequest){
        friendRequestRepository.deleteFriendRequest(friendRequest);
    }

    //三个常用的
    public void deleteFriendRequestByUsername(String username){
        friendRequestRepository.deleteFriendRequestByUsername(username);
    }

    public void yesFriendRequest(String username){
        friendRequestRepository.yesFriendRequest(username);
    }

    public void noFriendRequest(String username){
        friendRequestRepository.noFriendRequest(username);
    }

    //根据用户名去查找对应请求
    public FriendRequest findFriendRequest(String username){
        return friendRequestRepository.findFriendRequestByUsername(username);
    }
}
