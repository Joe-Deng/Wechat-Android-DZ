package com.example.wechatproj.Services;


import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.wechatproj.Database.DAO.MessageDao;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Entity.FriendRequest;
import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Database.ViewModel.MessagesViewModel;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.Base64Encoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class UpdateService extends Service {
    List<Message> allMessages = new ArrayList<>();
    List<Message> newMessages = new ArrayList<>();
    List<FriendRequest> allFriendRequests = new ArrayList<>();
    List<FriendRequest> newFriendRequsts = new ArrayList<>();
    List<Friend> allFriends = new ArrayList<>();    //这里保存的都是下载的头像
    List<Friend> newFriends = new ArrayList<>();    //这里保存的头像还未下载
    private static String Message_URL = MyConstants.GetMessage_URL;
    private static String FriendRequest_URL = MyConstants.FriendRequest_URL;
    private static String FriendResponse_URL = MyConstants.FriendResponse_URL;
    private static String AllFriends_URL = MyConstants.AllFriends_URL;
    private static String DeleteFriendRequest_URL = MyConstants.DeleteFriendResponse_URL;
    String username = null;
    Boolean isDownloading1 = false;     //判断是否正在下载消息图片
    Boolean isDownloading2 = false;     //判断是否正在下载好友头像
    Boolean isDownloading3 = false;     //判断是否正在下载好友头像(初始化好友列表）
    //    private static int[] IMMEDIATE_TYPE ={0,1,4,10,11};    //  分别表示文字，微信表情，拍一拍，好友请求,好友请求回应；
    private IBinder updateBinder = new UpdateBinder();
    Boolean ifGetAllMessages = false;
    Boolean ifGetAllFriendsRequest = false;
    Boolean ifGetAllFriends = false;
    Boolean isIniting = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        username = intent.getExtras().getString("username");
        new AutoRequestForData().start();   //自动更新轮询操作
        return updateBinder;
    }

    //和Activity的接口
    public class UpdateBinder extends Binder {
        public Boolean InitGetAllData() {
            isIniting = true;
           GetAllData();
           isIniting = false;
           return true;
        }  //初始化获取所有服务器数据

        public List<Message> getAllMessages() {
            return allMessages;
        }       //获取所有消息

        public List<FriendRequest> getAllFriendRequests() {
            return allFriendRequests;
        }   //获取所有好友请求

        public List<Friend> getAllFriends() {   //获取所有好友资料
            return allFriends;
        }

        public List<FriendRequest> getNewFriendRequests() {     //获取新消息
            return newFriendRequsts;
        }

        public List<Message> getNewMessages() {     //获取新好友请求
//            Log.d("UpdateService", "getNewMessages: "+newMessages);
            return newMessages;
        }

        public List<Friend> getNewFriends() {       //获取新好友资料
//            Log.d("UpdateService", "getNewFriends: "+newFriends);
            return newFriends;
        }

        //客户端已经成功获取到了消息，可以删除了
        public void deleteMessages() {
            newMessages.clear();
            newFriendRequsts.clear();
            newFriends.clear();
        }

        //这两个是用来反馈服务器我已收到消息和请求
        public void ReceiveOk(List<Long> mTimes) {
            new ReceiveOkAsync(mTimes).start();
        }

        public void ReceiveOk2(List<String> fUsernames) {
            Log.d("重点排查12", "ReceiveOk2: " + fUsernames.toString());
            new ReceiveOk2Async(fUsernames).start();
        }

        //这个用于FriendRequestAdapter 对好友请求的回应
        public void responseFriendRequest(String SID, String RID, String result) {
            new ResponseFriendRequestAsync(SID, RID, result).start();
        }

        //删除服务端好友请求，有始有终
        public void deleteFriendRequest(String SID, String RID) {
            new DeleteFriendRequestAsync().execute(RID, SID);
        }
    }


//    public class MyHandler extends Handler {
//        @Override
//        public void handleMessage(@NonNull android.os.Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 1) {
//
//            } else if (msg.what == 2) {//图片消息下载回调
//                Bundle bundle = msg.getData();
//                long M_Time = bundle.getLong("M_Time");
//                String RID = bundle.getString("RID");
//                String SID = bundle.getString("SID");
//                int M_Type = bundle.getInt("M_Type");
//                String M_Content = bundle.getString("M_Content");
//                boolean IF_Send = bundle.getBoolean("IF_Send");
//                Message message = new Message(M_Time, RID, SID, M_Type, M_Content, IF_Send, false);
//                messages.add(message);
//                Log.d("Service-Handler", "新加了一条图片消息 ");
//            } else if (msg.what == 3) {//好友请求头像下载回调
//                Bundle bundle = msg.getData();
//                String SID = bundle.getString("username");
//                String snickname = bundle.getString("nickname");
//                String headPicPath = bundle.getString("headPicPath");
//                String text = bundle.getString("text");
//                String status = bundle.getString("status");
//                FriendRequest friendRequest = new FriendRequest(headPicPath, SID, snickname, text, status);
//                friendRequests.add(friendRequest);
//                Log.d("Service-Handler", "新加了一条好友请求 ");
//            }
//        }
//    }

    //获取新消息（轮询）
    public class GetNewMessageTask extends Thread implements Runnable {
        @Override
        public void run() {
            super.run();

            //通过OkHttp建立联系
            final String TAG = "GetNewMessageTask";
            OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("type", "new")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(Message_URL)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "获取消息数据失败 - - 网络错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    Log.d(TAG, "接到回应--GetNewMessage");
                    String res = response.body().string();
//                    Log.d(TAG, "重点排查34："+res);
                    if (res.equals("fail")) {
//                        Log.d(TAG, "没新消息");
                        return;
                    }else if(res.equals("null")){
//                        Log.d(TAG, "onResponse: " + res);
//                        Log.d("重点排查17", "好友请求为空 ");
                        return;
                    }
                    String json = Base64Decoder.decode(res);
//                    Log.d(TAG, "result:" + json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        if (jsonArray == null || jsonArray.length() == 0) {
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            int M_Type = jsonObject.getInt("m_Type");
                            boolean IF_Send = false;
                            switch (M_Type) { //如果是这些类型，在接到的时候，就已经发送完成了（没有延时操作，比如下载图片）
                                case 0:
                                case 1:
                                case 4: {  //文字，表情，拍一拍
                                    IF_Send = true;
                                    Boolean isHas = false;  //判断是否重复
                                    long M_Time = jsonObject.getLong("m_Time");
                                    String RID = jsonObject.getString("rID");
                                    String SID = jsonObject.getString("sID");
                                    String M_Content = jsonObject.getString("m_Content");
//                                    Log.d(TAG, "重点排查4：" + newMessages);
                                    if (newMessages != null && !newMessages.isEmpty()) {    //  避免空指针问题
                                        for (Message msg : newMessages) {
                                            if (msg.getM_Time() == M_Time) {
                                                isHas = true;
                                                break; //避免插入重复的消息
                                            }
                                        }
                                    }
                                    if (isHas) {
                                        break;  //如果重复,跳过该消息，转向下一个
                                    }
                                    Message message = new Message(M_Time, RID, SID, M_Type, M_Content, IF_Send, false);
//                                    Log.d(TAG, "重点排查3：" + message.getM_Content());
                                    newMessages.add(message);
//                                    if (M_Type == 10) {
//                                        String headPicPath = getPackageName()+"/files/"+SID+".jpg";
//                                        FriendRequest friendRequest = new FriendRequest(jsonObject.getString("m_Content"),SID,jsonObject.getString("陌生人"),M_Content);
//                                        friendRequests.add(friendRequest);
//                                    } else {
//                                        newMessages.add(message);
//                                    }
                                }
                                break;
                                case 2: {    //图片
                                    IF_Send = true;
                                    long M_Time = jsonObject.getLong("m_Time");
                                    String RID = jsonObject.getString("rID");
                                    String SID = jsonObject.getString("sID");
                                    String M_Content = jsonObject.getString("m_Content");
                                    File file = new File(getFilesDir().getAbsolutePath(), M_Time + ".jpg");
                                    if (file.exists())
                                        break; //  如果图片已经存在，就不用继续下载图片了，也不必存取该消息了（下好图片说明存过一遍了）
                                    Message message = new Message(M_Time, RID, SID, M_Type, M_Content, false, false);
                                    new DownloadImgTask("new").execute(message);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Handler
                }
            });
        }
    }

    //获取所有消息，当初始化时
    public class GetAllMessageTask extends Thread implements Runnable {
        @Override
        public void run() {
            super.run();

            //通过OkHttp建立联系

            final String TAG = "GetAllMessageTask";
            OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("type", "all")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(Message_URL)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("重点排查16", "获取消息数据失败 - - 网络错误");
                    ifGetAllMessages = true;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应--GetMessage");
                    String res = response.body().string();
                    if (res.equals("fail")) {
                        Log.d(TAG, "onResponse: " + res);
                        Log.d("重点排查16", "消息下载失败 ");
                        ifGetAllMessages = true;
                        return;
                    }else if(res.equals("null")){
                        Log.d(TAG, "onResponse: " + res);
                        Log.d("重点排查16", "服务器消息为空 ");
                        ifGetAllMessages = true;
                        return;
                    }
                    String json = Base64Decoder.decode(res);
                    Log.d(TAG, "result:" + json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        if (jsonArray == null || jsonArray.length() == 0) {
                            Log.d("重点排查16", "消息为空 ");
                            ifGetAllMessages = true;
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            int M_Type = jsonObject.getInt("m_Type");
                            boolean IF_Send = false;
                            switch (M_Type) { //如果是这些类型，在接到的时候，就已经发送完成了（没有延时操作，比如下载图片）
                                case 0:
                                case 1:
                                case 4: {  //文字，表情，拍一拍
                                    IF_Send = true;
                                    Boolean isHas = false;  //判断是否重复
                                    long M_Time = jsonObject.getLong("m_Time");
                                    String RID = jsonObject.getString("rID");
                                    String SID = jsonObject.getString("sID");
                                    String M_Content = jsonObject.getString("m_Content");
                                    Boolean IF_Receive = jsonObject.getBoolean("iF_Receive");
                                    if (allMessages != null) {    //  避免空指针问题
                                        for (Message msg : allMessages) {
                                            if (msg.getM_Time() == M_Time) {
                                                isHas = true;
                                                break; //避免插入重复的消息
                                            }
                                        }
                                    }
                                    if (isHas) break;  //重复则跳过

                                    Message message = null;
                                    if(SID.equals(username)){   //如果是自己发的，直接标记已读
                                        message = new Message(M_Time, RID, SID, M_Type, M_Content, IF_Send, true);
                                    }else {
                                        message = new Message(M_Time, RID, SID, M_Type, M_Content, IF_Send, IF_Receive);
                                    }
                                    allMessages.add(message);
//                                    if (M_Type == 10) {
//                                        String headPicPath = getPackageName()+"/files/"+SID+".jpg";
//                                        FriendRequest friendRequest = new FriendRequest(jsonObject.getString("m_Content"),SID,jsonObject.getString("陌生人"),M_Content);
//                                        friendRequests.add(friendRequest);
//                                    } else {
//                                        messages.add(message);
//                                    }
                                }
                                break;
                                case 2: {    //图片
                                    long M_Time = jsonObject.getLong("m_Time");
                                    String RID = jsonObject.getString("rID");
                                    String SID = jsonObject.getString("sID");
                                    String M_Content = jsonObject.getString("m_Content");
                                    Boolean IF_Receive = jsonObject.getBoolean("iF_Receive");
                                    while (isDownloading1) {     //因为要准备下载图片了，判断是否在正在下载图片
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    File file = new File(getFilesDir().getAbsolutePath(), M_Time + ".jpg");
                                    if (file.exists())
                                        break; //  如果图片已经存在，就不用继续下载图片了，也不必存取该消息了（下好图片说明存过一遍了）
                                    Message message = new Message(M_Time, RID, SID, M_Type, M_Content, true, IF_Receive);
                                    new DownloadImgTask("all").execute(message);
                                    while (!new File(getFilesDir().getAbsolutePath(), M_Time + ".jpg").exists()) {
                                        //如果文件不存在就不继续进行
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        Log.d("重点排查16", "消息下载完毕 ");
                        ifGetAllMessages = true; //所有新消息获取成功
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Handler
                }
            });

        }
    }

    //获取新的好友请求（轮询）
    public class GetNewFriendRequests extends Thread implements Runnable {
        String RID = username;

        @Override
        public void run() {
            super.run();
            final String TAG = "GetNewFriendRequests";
            OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();


            final Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(FriendRequest_URL + "?username=" + RID + "&type=new")
                    .get()
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "获取消息数据失败 - - 网络错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    Log.d(TAG, "接到回应--GetNewFriendRequests");
                    String res = response.body().string();
                    if (res.equals("fail")) {
//                        Log.d(TAG, "onResponse: " + res);
                        return;
                    }else if(res.equals("null")){
//                        Log.d(TAG, "onResponse: " + res);
//                        Log.d("重点排查17", "好友请求为空 ");
                        return;
                    }
                    String json = Base64Decoder.decode(res);
//                    Log.d(TAG, "result:" + json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        if (jsonArray == null || jsonArray.length() == 0) {
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            String SID = jsonObject.getString("sID");
                            String nickname = jsonObject.getString("snickname");
                            String text = jsonObject.getString("text");
                            String status = jsonObject.getString("status");
                            String HPUrl = jsonObject.getString("hPUrl");
                            if (!status.equals("unknow")) {
                                continue;   //如果已经确认的，就不接收
                            }

                            while (isDownloading2) {     //因为要准备下载头像了，判断是否在正在下载头像
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Boolean isHas = false;  //判断是否重复
//                            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
//                            if (file.exists()) return; //如果头像已存在，说明下载过头像并保存过该好友请求
                            if (newFriendRequsts != null && !newFriendRequsts.isEmpty()) {    //  避免空指针问题
                                for (FriendRequest frq : newFriendRequsts) {
                                    if (frq.getUsername().equals(SID))
                                        isHas = true;
                                    break;     //如果集合存在该好友请求就跳过，避免重复
                                }
                            }
                            if (isHas) continue;    //如果重复就跳过
                            FriendRequest friendRequest = new FriendRequest(HPUrl, SID, nickname, text, status);
                            new DownloadImgTask2("new").execute(friendRequest);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Handler
                }
            });
        }
    }

    //获取所有好友请求，当初始化时
    public class GetAllFriendRequests extends Thread implements Runnable {
        String RID = username;

        @Override
        public void run() {
            super.run();
            final String TAG = "GetNewFriendRequests";
            OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();


            final Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(FriendRequest_URL + "?username=" + RID + "&type=all")
                    .get()
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.d("重点排查17", "获取消息数据失败 - - 网络错误");
                    ifGetAllFriendsRequest = true;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应--GetNewFriendRequests");
                    String res = response.body().string();
                    if (res.equals("fail")) {
                        Log.d(TAG, "onResponse: " + res);
                        Log.d("重点排查17", "好友请求下载失败 ");
                        ifGetAllFriendsRequest = true;
                        return;
                    }else if(res.equals("null")){
                        Log.d(TAG, "onResponse: " + res);
                        Log.d("重点排查17", "服务器好友请求为空 ");
                        ifGetAllFriendsRequest = true;
                        return;
                    }
                    String json = Base64Decoder.decode(res);
                    Log.d(TAG, "result:" + json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        if (jsonArray == null || jsonArray.length() == 0) {
                            Log.d("重点排查17", "好友请求为空 ");
                            ifGetAllFriendsRequest = true;
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            String SID = jsonObject.getString("sID");
                            String nickname = jsonObject.getString("snickname");
                            String text = jsonObject.getString("text");
                            String status = jsonObject.getString("status");
                            String HPUrl = jsonObject.getString("hPUrl");
//                            if (!status.equals("unknow")) {
//                                continue;   //如果已经确认的，就不接收
//                            }

                            Log.d("重点排查17", "准备下载头像，isDownloading2 = "+isDownloading2);
                            while (isDownloading2) {     //因为要准备下载头像了，判断是否在正在下载头像
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
//                            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
//                            if (file.exists()) return; //如果头像已存在，说明下载过头像并保存过该好友请求

                            Boolean isHas = false;
                            if (allFriendRequests != null && !allFriendRequests.isEmpty()) {    //  避免空指针问题
                                for (FriendRequest frq : allFriendRequests) {
                                    if (frq.getUsername().equals(SID))
                                        isHas = true;
                                    break;     //如果集合存在该好友请求就跳过，避免重复
                                }
                            }
                            if (isHas) continue;   //如果重复就跳过
                            FriendRequest friendRequest = new FriendRequest(HPUrl, SID, nickname, text, status);
                            new DownloadImgTask2("all").execute(friendRequest);
//                            Log.d("重点排查17", "判断图片是否存在本地 ");
                            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
                            Log.d("重点排查17", "判断图片是否存在本地 "+file.getPath());
                            while (!file.exists()) {
                                //如果文件不存在就不继续进行
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.d("重点排查17", "图片下载完成 "+file.getPath());
                        }
                        Log.d("重点排查17", "好友请求下载完毕 ");
                        ifGetAllFriendsRequest = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Handler
                }
            });
        }
    }

    //获取所有好友资料，用于好友列表
    public class GetAllFriend extends Thread implements Runnable {
        String RID = username;

        @Override
        public void run() {
            super.run();
            final String TAG = "GetAllFriends";
            OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000, TimeUnit.MILLISECONDS)
                    .writeTimeout(10000, TimeUnit.MILLISECONDS).build();


            final Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(AllFriends_URL + "?username=" + RID)
                    .get()
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("重点排查18", "获取消息数据失败 - - 网络错误");
                    ifGetAllFriends = true;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应--GetAllFriends");
                    String res = response.body().string();

                    if (res.equals("fail")) {
                        Log.d(TAG, "onResponse: " + res);
                        Log.d("重点排查18", "好友列表下载失败 ");
                        ifGetAllFriends = true;
                        return;
                    }else if(res.equals("null")){
                        Log.d("重点排查18", "服务器好友列表为空 ");
                        ifGetAllFriends = true;
                        return;
                    }
                    String json = Base64Decoder.decode(res);
                    Log.d(TAG, "result:" + json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        if (jsonArray == null || jsonArray.length() == 0) {
                            Log.d("重点排查18", "好友列表下载为空 ");
                            ifGetAllFriends = true;
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);
                            String username = jsonObject.getString("username");
                            String nickname = jsonObject.getString("nickname");
                            String sex = jsonObject.getString("sex");
                            String country = jsonObject.getString("country");
                            String province = jsonObject.getString("province");
                            String city = jsonObject.getString("city");
                            String HPUrl = jsonObject.getString("hPUrl");

                            while (isDownloading3) {     //因为要准备下载头像了，判断是否在正在下载头像
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
//                            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
//                            if (file.exists()) return; //如果头像已存在，说明下载过头像并保存过该好友请求
                            Boolean isHas = false;
                            if (allFriends != null && !allFriends.isEmpty()) {    //  避免空指针问题
                                for (Friend frd : allFriends) {
                                    if (frd.getUsername().equals(username))
                                        isHas = true;
                                    break;     //如果集合存在该好友就跳过，避免重复
                                }
                            }
                            if (isHas) continue;     //如果存在就跳过
                            Friend friend1 = new Friend(username, nickname, sex, country, province, city, HPUrl);
                            new DownloadImgTask3("all").execute(friend1);
                            Log.d("重点排查18", "判断本地是否存在该图片 ");
                            while (!new File(getFilesDir().getAbsolutePath(), username + ".jpg").exists()) {
                                //如果文件不存在就不继续进行
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Log.d("重点排查18", "好友列表下载完毕 ");
                        ifGetAllFriends = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Handler
                }
            });
        }
    }

    //初始化获取所有数据
    private Boolean GetAllData() {   //之所以设置为同步方法是因为保证初始化的时候主线程只执行进行数据请求，而数据请求完毕才开始渲染UI；
        new GetAllMessageTask().start();
        new GetAllFriendRequests().start();
        new GetAllFriend().start();
        while (true) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ifGetAllMessages && ifGetAllFriendsRequest && ifGetAllFriends)
            {
                Log.d("重点排查15", "GetAllData: 所有数据下载完成");
                return true;
            }
        }
    }

    //自动轮询
    public class AutoRequestForData extends Thread implements Runnable {
        @Override
        public void run() {
            super.run();
            try {   //延迟500ms，等待判断是否正在初始化数据
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (isIniting){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true) {
                try {
                    Thread.sleep(500);      // 0.4s轮询一次服务器
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new GetNewMessageTask().start();
                new GetNewFriendRequests().start();
                new GetYesFriendResponseAsync().start();
//                GetNewMessageTask getNewMessageTask = new GetNewMessageTask();
//                GetNewFriendRequests getNewFriendRequests = new GetNewFriendRequests();
//                GetYesFriendResponseAsync getYesFriendResponseAsync = new GetYesFriendResponseAsync();
//                getNewMessageTask.start();
//                getNewFriendRequests.start();
//                getYesFriendResponseAsync.start();
            }
        }
    }

    //下载图片消息图片（用于消息图片）
    private class DownloadImgTask extends AsyncTask<Message, Void, Void> {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        String type = null;

        public DownloadImgTask(String type) {
            this.type = type;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            isDownloading1 = true;  //防止同一张图片在同一时间点同时下载
            Message message = messages[0];
            String downloadpath = message.getM_Content();
            String path = null;
            URL url = null;
            long M_Time = message.getM_Time();
            File file = new File(getFilesDir().getAbsolutePath(), M_Time + ".jpg");
            if (!file.exists()) {
                //如果图片不存在才开始下载
                try {
                    url = new URL(downloadpath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();
                        fileOutputStream = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                        path = file.getPath();
                        Log.d("service的图片下载任务结束", "headPicPath=" + path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Log.d("重点排查48", "path:"+path);
                while (path == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //延时操作，等待图片下载完成
                }
            }
            //封装消息
            Message message1 = null;
            Log.d("重点排查48", "message:"+message);
            if(message.getSID().equals(username)){  //如果是自己发的，直接标记已读
                message1 = new Message(message.getM_Time(), message.getRID(), message.getSID(),
                        message.getM_Type(), file.getPath(), true, true);
            }else {
                message1 = new Message(message.getM_Time(), message.getRID(), message.getSID(),
                        message.getM_Type(), file.getPath(), true, message.getIF_Readed());
            }

            if (type.equals("new")) {
                newMessages.add(message1);
            } else if (type.equals("all")) {
                allMessages.add(message1);
            }
            isDownloading1 = false;
            return null;
        }
    }

    //下载头像(用于好友请求头像）
    private class DownloadImgTask2 extends AsyncTask<FriendRequest, Void, Void> {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        private String headpicPath = null;
        String type = null;

        public DownloadImgTask2(String type) {
            this.type = type;
        }

        @Override
        protected Void doInBackground(FriendRequest... friendRequests) {
            isDownloading2 = true;     //防止同一时间下载同一张头像图片
            FriendRequest friendRequest = friendRequests[0];
            String downloadpath = friendRequest.getHeadPicPath();
            URL url = null;
            String SID = friendRequest.getUsername();
            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
            if (!file.exists()) {
                //如果图片不存在才开始下载
                try {
                    url = new URL(downloadpath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();

                        fileOutputStream = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                        headpicPath = file.getPath();
                        Log.d("service的头像下载任务结束", "headPicPath=" + headpicPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                while (headpicPath == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //延时操作，等待图片下载完成
                }
            }
            //封装好友请求
            FriendRequest friendRequest1 = new FriendRequest(file.getPath(), friendRequest.getUsername(),
                    friendRequest.getName(), friendRequest.getText(), friendRequest.getStatus());
            Log.d("重点排查36", "headPicPath："+friendRequest.getHeadPicPath());
            if (type.equals("new")) {
                newFriendRequsts.add(friendRequest1);
            } else if (type.equals("all")) {
                allFriendRequests.add(friendRequest1);
            }
            isDownloading2 = false;
            return null;
        }
    }

    //下载好友头像（初始化好友列表）
    public class DownloadImgTask3 extends AsyncTask<Friend, Void, Void> {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        private String headpicPath = null;
        String type = null;

        public DownloadImgTask3(String type) {
            this.type = type;
        }

        @Override
        protected Void doInBackground(Friend... friends) {
            isDownloading3 = true;     //防止同一时间下载同一张头像图片
            Friend friend = friends[0];
            String downloadpath = friend.getHeadPicPath();
            URL url = null;
            String SID = friend.getUsername();
            File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
            if (!file.exists()) {
                //如果图片不存在才开始下载
                try {
                    url = new URL(downloadpath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();

                        fileOutputStream = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                        headpicPath = file.getPath();
                        Log.d("service的头像下载任务结束", "headPicPath=" + headpicPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                while (headpicPath == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //延时操作，等待图片下载完成
                }
            }
            //封装好友请求
            Friend friend1 = new Friend(friend.getUsername(), friend.getNickname(), friend.getSex(),
                    friend.getCountry(), friend.getProvince(), friend.getCity(), file.getPath());
            if (type.equals("all")) {
                allFriends.add(friend1);
            }
            isDownloading3 = false;
            return null;
        }
    }

    //8.确认收到消息(测试通过）
    public class ReceiveOkAsync extends Thread implements Runnable {
        List<Long> mTimes;

        public ReceiveOkAsync(List<Long> mTimes) {
            this.mTimes = mTimes;
        }

        @Override
        public void run() {
            super.run();
            JSONArray jsonArray = new JSONArray(mTimes);
            String json = jsonArray.toString();
            String base64 = Base64Encoder.encode(json);
            String path = MyConstants.ClearMsg_URL + "?data=" + base64;
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("ReceiveOk", "服务器更改消息状态：" + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    //确认收到好友请求
    public class ReceiveOk2Async extends Thread implements Runnable {
        List<String> fUsernames;

        public ReceiveOk2Async(List<String> fUsernames) {
            this.fUsernames = new ArrayList<>(fUsernames);
        }

        @Override
        public void run() {
            super.run();
            Log.d("重点排查11", "fUsernames" + fUsernames);
            JSONArray jsonArray = new JSONArray(fUsernames);
            String json = jsonArray.toString();

            String base64 = Base64Encoder.encode(json);
            String path = MyConstants.ClearRqst_URL + "?RID=" + username + "&data=" + base64;
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("ReceiveOk", "服务器更改消息状态：" + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //回应好友请求
    public class ResponseFriendRequestAsync extends Thread implements Runnable {
        private String SID;
        private String RID;
        private String result;
        private String TAG = "Service-Response";

        public ResponseFriendRequestAsync(String SID, String RID, String result) {
            this.SID = SID;
            this.RID = RID;
            this.result = result;
        }

        @Override
        public void run() {
            super.run();
            String path = FriendResponse_URL + "?SID=" + SID + "&RID=" + RID + "&result=" + result;
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    if (result.toString().equals("success")) {
                        Log.d(TAG, "发送好友成功 ");
                    } else {
                        Log.d(TAG, "发送好友请求失败");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //接收好友请求的回复(轮询）
    public class GetYesFriendResponseAsync extends Thread implements Runnable {
        private String TAG = "Service-getResponse";

        @Override
        public void run() {
            super.run();
            String path = FriendResponse_URL + "?SID=" + username;
            InputStream inputStream = null;
            BufferedReader reader = null;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
//                Log.d(TAG, "URLPath: "+path);
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    String res = result.toString();
//                    Log.d(TAG, "接到回应--GetNewFriend");
                    if (res.equals("fail")) {
//                        Log.d(TAG, "onResponse:"+res);
                        return;
                    }else if (res.equals("null")) {
//                        Log.d(TAG, "onResponse:"+res);
                        return;
                    }
                    String json = Base64Decoder.decode(res);
                    JSONArray jsonArray = new JSONArray(json);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Friend friend = new Friend(jsonObject.getString("username"),
                                jsonObject.getString("nickname"),
                                jsonObject.getString("sex"),
                                jsonObject.getString("country"),
                                jsonObject.getString("province"),
                                jsonObject.getString("city"),
                                jsonObject.getString("hPUrl"));
                        newFriends.add(friend);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    //删除服务端好友请求，有始有终
    public class DeleteFriendRequestAsync extends AsyncTask<String, Void, Void> {
        String RID = null;
        String SID = null;
        InputStream inputStream;
        BufferedReader reader;
        HttpURLConnection conn;

        @Override
        protected Void doInBackground(String... strings) {
            RID = strings[0];
            SID = strings[1];
            try {
                URL url = new URL(DeleteFriendRequest_URL + "?RID=" + RID + "&SID=" + SID);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                if (conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("删除服务端好友请求", "结果： " + result.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
