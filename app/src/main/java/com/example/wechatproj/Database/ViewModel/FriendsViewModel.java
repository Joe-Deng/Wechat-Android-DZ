package com.example.wechatproj.Database.ViewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Repository.FriendRepository;

import java.util.List;

public class FriendsViewModel extends AndroidViewModel {
    private FriendRepository friendRepository;

    public LiveData<List<Friend>>getAllFriendLive(){
        return friendRepository.getAllFriendsLive();
    }

    public FriendsViewModel(@NonNull Application application){
        super(application);
        friendRepository = new FriendRepository(application);
    }

    public void insertFriends(Friend...friends){
        friendRepository.insertFriends(friends);
    }

    public void updateFriends(Friend...friends){
        friendRepository.updateFriends(friends);
    }

    public void deleteFriends(Friend...friends){
        friendRepository.deleteFriends(friends);
    }

    public Friend findFriend(String username){
        return friendRepository.findFriend(username);
    }

    public LiveData<Friend> findFriendLive(String username){
        return friendRepository.getFriendLive(username);
    }
}
