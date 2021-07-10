package com.example.wechatproj.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeBottomViewModel extends ViewModel {
    private MutableLiveData<Integer> newMsgNum = new MutableLiveData<>();
    private MutableLiveData<Integer> newFriendNum = new MutableLiveData<>();
    private MutableLiveData<Boolean> hasNewFound = new MutableLiveData<>();

    public MutableLiveData<Integer> getNewMsgNum() {
        if(newMsgNum==null ||newMsgNum.getValue()==null){
            newMsgNum.setValue(0);
        }
        return newMsgNum;
    }

    public void setNewMsgNum(int newMsgNum) {
        this.newMsgNum.setValue(newMsgNum);
    }

    public MutableLiveData<Integer> getNewFriendNum() {
        if(newFriendNum==null || newFriendNum.getValue()==null){
            newFriendNum.setValue(0);
        }
        return newFriendNum;
    }

    public void setNewFriendNum(int newFriendNum) {
        this.newFriendNum.setValue(newFriendNum);
    }

    public MutableLiveData<Boolean> getHasNewFound() {
        if(hasNewFound==null || hasNewFound.getValue()==null){
            hasNewFound.setValue(false);
        }
        return hasNewFound;
    }

    public void setHasNewFound(Boolean hasNewFound) {
        this.hasNewFound.setValue(hasNewFound);
    }
}
