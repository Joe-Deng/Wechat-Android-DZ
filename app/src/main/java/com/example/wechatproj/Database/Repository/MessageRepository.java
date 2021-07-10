package com.example.wechatproj.Database.Repository;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.wechatproj.Database.DAO.MessageDao;
import com.example.wechatproj.Database.Database.MessageDatabase;
import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.Base64Encoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//监听主动修改的数据使用livedata,被动修改的数据
//查询一次结果就得清空结果的使用普通数据（如果是普通类型清不清空值没关系，如果是数组、集合，在每次使用前清空值）
//输入值唯一的使用构造方法主动初始化（比如所有消息）
//输入值不唯一的使用普通public方法被动初始化（比如输入好友账号获取好友消息）
public class MessageRepository {

//    public LiveData<Integer> getNewMessageCountLive() {
//        return newMessageCountlive;
//    }

    String myUsername = null;
    LiveData<List<Message>>allMessageslive;
    MessageDao messageDao;
    MessageDatabase messageDatabase;
    Message amessage;
    LiveData<List<Message>> allNewMessagesLive;
    LiveData<List<Message>> FriendMessagesLive;
    LiveData<List<Message>> FriendNewMessagesLive;
    List<Message> FriendMessage;
    Message lastMessage;

    //查询所有消息  这条暂时没用
    public LiveData<List<Message>> getAllMessagesLive() {
        return allMessageslive;
    }
    //查询所有新消息
    public LiveData<List<Message>> getAllNewMessagesLive(){
        return allNewMessagesLive;
    }

    //构造，保证所有实体静态唯一
    public MessageRepository(Context context,String myUsername) {
        this.myUsername = myUsername;
        messageDatabase = MessageDatabase.getDatabase(context.getApplicationContext());
        messageDao = messageDatabase.getMessageDao();
        allMessageslive = messageDao.getAllMessageLive(myUsername);
        allNewMessagesLive = messageDao.findNewMessages();
    }


//    ————————————————————————————
//    方法区
    public void insertMessages(Message...messages){
        new InsertAsyncTask(messageDao).execute(messages);
    }

    public void updateMessages(Message...messages){
        new UpdateAsyncTask(messageDao).execute(messages);
    }

    public void deleteMessages(Message...messages){
        new DeleteAsyncTask(messageDao).execute(messages);
    }

    //查询具体某一条消息（根据时间）
    public Message findMessage(long M_Time){
        new FindAsyncTask(messageDao).execute(M_Time);
        while (true){
            try {
                Thread.sleep(10);   //预判10ms查到，否则会返回空或者上一次查询结果
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return amessage;
        }
    }

    //查询朋友所有消息
    public LiveData<List<Message>> getFriendMessagesLive(String username) {
        new FindAllFriendMessageTask(messageDao).execute(username);
        while (FriendMessagesLive==null || FriendMessagesLive.getValue() == null){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return FriendMessagesLive;
    }


    //这个好友的消息已经读完了
    public void readFriendMessages(String username){
        new readFriendMessagesAsync(messageDao).execute(username);
    }


    //查询最后一条消息
    public Message getLastMessage(String username){
        lastMessage = null;
        new GetLastMessageAsync(messageDao,username).start();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        Log.d("重点排查25x", "lastMessage: "+lastMessage);
        return lastMessage;
    }


    public LiveData<List<Message>> getFriendNewMessages(String username){
        new FindFriendNewMessage(messageDao).execute(username);
        while (FriendNewMessagesLive==null){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return FriendNewMessagesLive;
    }







//    ______________________________________________
//    异步任务区
    static class InsertAsyncTask extends AsyncTask<Message,Void,Void> {
        private MessageDao messageDao;

        public InsertAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.insertMessage(messages);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Message,Void,Void> {
        private MessageDao messageDao;

        public UpdateAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.updateMessage(messages);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessageDao messageDao;

        public DeleteAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }
        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.deleteMessage(messages);
            return null;
        }
    }

    //根据时间查询具体的一条消息
    class FindAsyncTask extends AsyncTask<Long,Void,Void>{
        private MessageDao messageDao;
        public FindAsyncTask(MessageDao messageDao){
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Long... longs) {
            amessage = messageDao.findMessage(longs[0]);
            return null;
        }
    }

    //异步查询所有新消息   没用
    class FindNewMessageTask extends AsyncTask<Void,Void,Void>{
        private MessageDao messageDao;

        public FindNewMessageTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(Void...voids) {
            allNewMessagesLive = messageDao.findNewMessages();
            return null;
        }
    }

    //朋友新消息
    public class FindFriendNewMessage extends AsyncTask<String,Void,Void>{
        private MessageDao messageDao;

        public FindFriendNewMessage(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            FriendNewMessagesLive = messageDao.findNewMessageOf(strings[0]);
            Log.d("重点排查26", "FriendNewMessages: "+FriendNewMessagesLive);
            return null;
        }
    }


    //朋友所有消息
    public class FindAllFriendMessageTask extends AsyncTask<String,Void,Void>{
        private MessageDao messageDao;

        public FindAllFriendMessageTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            FriendMessagesLive = messageDao.getAllMessageLive(strings[0]);
            return null;
        }
    }

    //消息全部已读
    static class readFriendMessagesAsync extends AsyncTask<String,Void,Void> {
        MessageDao messageDao;
        public readFriendMessagesAsync(MessageDao messageDao) {
            this.messageDao = messageDao;
        }
        @Override
        protected Void doInBackground(String... strings) {
            messageDao.readMessages(strings[0]);
            return null;
        }
    }

    //查询最后一条消息
    public class GetLastMessageAsync extends Thread implements Runnable{
        MessageDao messageDao;
        String username;
        public GetLastMessageAsync(MessageDao messageDao,String username) {
            this.messageDao = messageDao;
            this.username = username;
        }

        @Override
        public void run() {
            super.run();
            lastMessage = messageDao.getLastMessage(username);
            Log.d("重点排查25", "lastMessage: "+lastMessage);
        }
    }

}
