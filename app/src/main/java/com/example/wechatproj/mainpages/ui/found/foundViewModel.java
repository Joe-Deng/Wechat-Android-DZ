package com.example.wechatproj.mainpages.ui.found;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class foundViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public foundViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}