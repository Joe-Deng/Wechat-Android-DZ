package com.example.wechatproj.Database.ViewModel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Database.Repository.MessageRepository;
import java.util.List;

public class MessagesViewModel extends AndroidViewModel {
    private MessageRepository messageRepository;
    Context context;

    public LiveData<List<Message>>getAllMessageLive(){
        return messageRepository.getAllMessagesLive();
    }

    public MessagesViewModel(@NonNull Application application){
        super(application);
        context = application;
    }

    public void setMessageRepository(String username){
        this.messageRepository = new MessageRepository(context,username);
    }
    
//————————方法区————————
    public void insertMessages(Message...messages){
        messageRepository.insertMessages(messages);
    }

    public  void updateMessages(Message...messages){
        messageRepository.updateMessages(messages);
    }

    public  void deleteMessages(Message...messages){
        messageRepository.deleteMessages(messages);
    }

    public Message getMessage(long M_Time){
       return messageRepository.findMessage(M_Time);
    }

    //获取所有新消息
    public LiveData<List<Message>> getAllNewMessageLive(){
        return messageRepository.getAllNewMessagesLive();
    }

    //更新读过的消息
    public void updateReadMessage(String username){
        messageRepository.readFriendMessages(username);
    }

    //获取好友最后一条消息
    public Message getFriendLastMessage(String username){
        return messageRepository.getLastMessage(username);
    }

    public LiveData<List<Message>> getFriendNewMessages(String username){
        return messageRepository.getFriendNewMessages(username);
    }

}
