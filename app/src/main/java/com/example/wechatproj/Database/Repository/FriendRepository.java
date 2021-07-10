package com.example.wechatproj.Database.Repository;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.example.wechatproj.Database.DAO.FriendDao;
import com.example.wechatproj.Database.Database.FriendDatabase;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.mainpages.ui.friends.FriendsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


//异步执行工具
public class FriendRepository {
    public LiveData<List<Friend>> getAllFriendsLive() {
        return allFriendslive;
    }
    String TAG ="FriendRepo";
    LiveData<List<Friend>>allFriendslive;
    FriendDao friendDao;
    FriendDatabase friendDatabase;
    Friend friend = null;
    String filePath = null;
    String path = null;
    Context mContext;
    String username = null;
    Bundle bundle;
    LiveData<Friend> friendLive;

    //构造，保证所有实体静态唯一
    public FriendRepository(Context context) {
        friendDatabase = FriendDatabase.getDatabase(context.getApplicationContext());
        friendDao = friendDatabase.getFriendDao();
        allFriendslive = friendDao.getAllFriendLive();
        filePath = context.getFilesDir().getAbsolutePath();
        mContext = context;
    }
    
    public void insertFriends(Friend...friends){
        new InsertAsyncTask(friendDao).execute(friends);
    }

    public void updateFriends(Friend...friends){
        new UpdateAsyncTask(friendDao).execute(friends);
    }

    public void deleteFriends(Friend...friends){
        new DeleteAsyncTask(friendDao).execute(friends);
    }

    public Friend findFriend(String...strings){
        Log.d(TAG, "处理开始,friend:"+friend);
//        new FindAsyncTask(friendDao).execute(strings);
//        findFriendByThread(strings[0]);
//        while(friend == null){
//            //延迟操作，等待线程执行完毕，handler返回结果
//        }
//        Log.d("FriendRepo", "findFriend: "+friend.getNickname());
//        // 因为仓库实例是唯一的，所以friend作为属性也唯一，实例不消除，friend就会永远存在，会跳过阻塞循环，所以必须清除属性
//        Friend afriend = friend;
//        friend = null;
        final String username = strings[0];
        new Thread(new Runnable() {
            @Override
            public void run() {
                friend = friendDao.getFriend(username);
            }
        }).start();
        while (friend == null){
            try {   //50ms 不管有没有查到都返回
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            break;
        }
        return friend;
//        Log.d(TAG, "find aFriend: "+afriend.getUsername());

    }

    public LiveData<Friend> getFriendLive(String fUsername){
        final String theUsername = fUsername;

        new Thread(new Runnable() {
            @Override
            public void run() {
                friendLive = friendDao.getFriendLive(theUsername);
            }
        }).start();
            try {   //50ms 不管有没有查到都返回
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        return friendLive;
    }

    //只要给用户名就能插入好友
    public void InsertFriendByUsername(String musername){
        bundle = new Bundle();
        username = musername;
        new SearchInfoTask().execute(musername);
    }

    static class InsertAsyncTask extends AsyncTask<Friend,Void,Void> {
        private FriendDao friendDao;

        public InsertAsyncTask(FriendDao friendDao) {
            this.friendDao = friendDao;
        }

        @Override
        protected Void doInBackground(Friend... friends) {
            friendDao.insertFriend(friends);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Friend,Void,Void> {
        private FriendDao friendDao;

        public UpdateAsyncTask(FriendDao friendDao) {
            this.friendDao = friendDao;
        }

        @Override
        protected Void doInBackground(Friend... friends) {
            friendDao.updateFriend(friends);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Friend,Void,Void>{
        private FriendDao friendDao;

        public DeleteAsyncTask(FriendDao friendDao){
            this.friendDao = friendDao;
        }
        @Override
        protected Void doInBackground(Friend... friends) {
            friendDao.deleteFriend(friends);
            return null;
        }
    }


    public void findFriendByThread(final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                friend = friendDao.getFriend(username);
                Log.d("FriendRepo", "FindAsyncTask-doInBackground     friend:"+friend.getNickname());
            }
        }).start();
    }

    private class SearchInfoTask extends AsyncTask<String, Void, Void> {
        MyHandler handler = new MyHandler();
        String TAG ="OKHTTP";
        String url = MyConstants.Friends_URL;
        String res = null;

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();

        @Override
        protected Void doInBackground(String... strings) {
                postAsynHttp(strings[0]);
            return null;
        }

        public void postAsynHttp(String username) {
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username" ,username)
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            Log.d(TAG, "发送成功，等待回应");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "Failure");
                    handler.sendEmptyMessage(-1);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应");
                    res = response.body().string();
                    Log.d(TAG, "response.body:  "+res);
                    if(res.equals("fail")){
                        handler.sendEmptyMessage(-2);
                        return;
                    }
//                    String jsonstr = new String(Base64.decode(res.getBytes(), Base64.URL_SAFE),"UTF-8");
                    String jsonstr = Base64Decoder.decode(res);
                    Log.d(TAG, "JsonStr: "+jsonstr);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonstr);
//                            JSONArray jsonArray = new JSONArray(jsonstr);
//                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                        Log.d(TAG, "账号："+jsonObject.getString("username")+"  昵称："+jsonObject.getString("nickname")+"  性别："+jsonObject.getString("sex")+"  地区："+jsonObject.getString("country")
                                +jsonObject.getString("province")+jsonObject.getString("city")+"  头像："+jsonObject.getString("hPUrl"));

                        String username = jsonObject.getString("username");
                        String nickname = jsonObject.getString("nickname");
                        String sex = jsonObject.getString("sex");
                        String country = jsonObject.getString("country");
                        String province = jsonObject.getString("province");
                        String city = jsonObject.getString("city");
                        String HPUrl = jsonObject.getString("hPUrl");
                        Bundle bundle = new Bundle();
                        bundle.putString("username",username);
                        bundle.putString("nickname",nickname);
                        bundle.putString("sex",sex);
                        bundle.putString("country",country);
                        bundle.putString("province",province);
                        bundle.putString("city",city);
                        bundle.putString("HPUrl",HPUrl);
                        Message message = new Message();
                        message.what = 2;
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                JsonObject data = new JsonParser().parse(jsonstr).getAsJsonObject();
//                Gson gson = new Gson();
//                ADImage adImage =  gson.fromJson(jsonstr,ADImage.class);
//                ADUrl = adImage.getRes().getPic();
//                Log.i(TAG, "ADUrl：" + ADUrl);
                }

            });
        }

    }

    //下载头像线程
    public class DownloadHeadPic extends AsyncTask<String,Void,Void>{
            String TAG ="DownloadTask";
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            File file = null;
            MyHandler handler = new MyHandler();
            @Override
            protected Void doInBackground(String...strings) {
                //1.图片路径，2.filePath , 3.username
                try {
                    path = strings[0];
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if(conn.getResponseCode() == 200){
                        inputStream = conn.getInputStream();
                        file = new File(filePath,username+".jpg");
                        fileOutputStream = new FileOutputStream(file);
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = inputStream.read(buffer) )!= -1){
                            fileOutputStream.write(buffer,0,len);
                        }
                        fileOutputStream.flush();
                        //获取绝对路径用于保存
//                            path = ImageUtils.getRealPathFromUri(getContext(), Uri.fromFile(file));
                        path = file.getPath();
                        Log.d(TAG, "path:" +
                                ""+path);
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(inputStream != null){
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fileOutputStream != null){
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            return null;
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.what ==2){
                //下载头像
                bundle = msg.getData();
                username = bundle.getString("username");
                new DownloadHeadPic().execute(bundle.getString("HPUrl"));
            }else if(msg.what ==1){
                Friend friend = new Friend(bundle.getString("username"),bundle.getString("nickname"),bundle.getString("sex"),bundle.getString("country"),bundle.getString("province"),bundle.getString("city"),path);
                insertFriends(friend);
            }
        }
    }

}
