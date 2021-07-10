package com.example.wechatproj.ViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StartPageViewModel extends ViewModel {
    MutableLiveData<Boolean> isFirstStart;

    public MutableLiveData<Boolean> getIsFirstStart() {
        if (isFirstStart == null)
        {
            isFirstStart = new MutableLiveData<>(true);
        }
        return isFirstStart;
    }

    public void setIsFirstStart(MutableLiveData<Boolean> isFirstStart) {
        this.isFirstStart = isFirstStart;
    }
}
