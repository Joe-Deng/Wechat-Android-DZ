package com.example.wechatproj.Database.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.Database.Repository.FriendCircleRepository;

import java.util.List;

public class FriendCirclesViewModel extends AndroidViewModel {
    private FriendCircleRepository friendCircleRepository;
    Context context;
    public FriendCirclesViewModel(@NonNull Application application) {
        super(application);
        context = application;
    }

    public void setFriendCircleRepository(String username){
        this.friendCircleRepository = new FriendCircleRepository(username,context);
    }

    public LiveData<List<FriendCircle>> getAllFriendCircleLive(){
        return friendCircleRepository.getAllFriendCirclesLive();
    }


//    ——————————————方法区————————————————
    public void insertFriendCircle(FriendCircle friendCircle){
        friendCircleRepository.insertFriendCircle(friendCircle);
    }

    private void deleteFriendCircle(FriendCircle friendCircle){
        friendCircleRepository.deleteFriendCircle(friendCircle);
    }

    public void updateFriendCircle(FriendCircle friendCircle){
        friendCircleRepository.updateFriendCircle(friendCircle);
    }

    //额外查询方法
    public FriendCircle findFriendCircleBySID(String SID){
       return friendCircleRepository.findFriendCircleBySID(SID);
    }

    public FriendCircle findFriendCircleByTime(Long M_Time){
        return friendCircleRepository.findFriendCircleByTime(M_Time);
    }
}
