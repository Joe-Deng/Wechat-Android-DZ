package com.example.wechatproj.functionpages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.wechatproj.Adapters.FriendRequestAdapter;
import com.example.wechatproj.Database.DAO.FriendDao;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Entity.FriendRequest;
import com.example.wechatproj.Database.ViewModel.FriendRequestsViewModel;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Services.UpdateService;
import com.example.wechatproj.Utils.Base64Decoder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Button searchButton;
    String path = null;
    EditText editText;
    ProgressBar searchProgressBar;
    String myUsername;
    FriendsViewModel friendsViewModel;
    FriendRequestsViewModel friendRequestsViewModel;
    FriendRequestAdapter friendRequestAdapter;
    Activity activity;
    MyHandler handler;
    MyServiceConn myserviceconn = new MyServiceConn();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        searchButton = findViewById(R.id.searchButton);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myUsername = getSharedPreferences("my_data",MODE_PRIVATE).getString("username","");
        friendRequestAdapter = new FriendRequestAdapter(AddFriendActivity.this,myUsername);
        recyclerView.setAdapter(friendRequestAdapter);
        friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);
        friendRequestsViewModel = ViewModelProviders.of(this).get(FriendRequestsViewModel.class);
        friendRequestsViewModel.setFriendRequestRepository(myUsername);
        handler = new MyHandler();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fUsername = editText.getText().toString();
                Friend myFriend = null;
                if(fUsername.isEmpty()){
                    Toast.makeText(getApplicationContext(),"请输入查询内容",Toast.LENGTH_SHORT).show();
                } else {
                    if((myFriend = friendsViewModel.findFriend(fUsername))!= null){
                        Log.d("AddFriendActivity","该用户是好友："+myFriend.getNickname()+": "+myFriend.getNickname());
                        Bundle bundle = new Bundle();
                        bundle.putString("username",myFriend.getUsername());
                        Intent intent = new Intent(AddFriendActivity.this,ChatActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else {
                        searchButton.setVisibility(View.INVISIBLE);
                        searchProgressBar.setVisibility(View.VISIBLE);
                        new findUserinfo().execute(fUsername);
                    }
                }
            }
        });



//        new ObserveFriendRequests(this).start();
        friendRequestsViewModel.getAllFriendRequestLive().observe(this, new Observer<List<FriendRequest>>() {
            @Override
            public void onChanged(List<FriendRequest> friendRequests) {
                friendRequestAdapter.setAllFriendRequests(friendRequests);
                friendRequestAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启后台服务，定时轮询服务器数据并更新数据库，再通过ViewModel进行展示
        startServerForData();
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                Bundle bundle = msg.getData();
                String HPUrl = bundle.getString("HPUrl");
                Log.d("重点排查32", "handleMessage: ");
                path = null;
                String FID = bundle.getString("username");
                Log.d("重点排查32", "FID: "+FID+" URL:"+HPUrl);
                new DownloadHeadPic().execute(bundle);
            }else if(msg.what == 3){
                Log.d("重点排查33", "ddd");
                Bundle bundle = msg.getData();
                Intent intent = new Intent(AddFriendActivity.this,SearchInfoActivity.class);
                intent.putExtras(bundle);
                searchButton.setVisibility(View.VISIBLE);
                searchProgressBar.setVisibility(View.INVISIBLE);
                startActivity(intent);
//                friend = new Friend(bundle.getString("username"),
//                        bundle.getString("nickname"),
//                        bundle.getString("sex"),
//                        bundle.getString("country"),
//                        bundle.getString("province"),
//                        bundle.getString("city"),
//                        path);
            }else if(msg.what == 2){
                Bundle bundle = msg.getData();
                Friend friend = new Friend(bundle.getString("username"),
                        bundle.getString("nickname"),
                        bundle.getString("sex"),
                        bundle.getString("country"),
                        bundle.getString("province"),
                        bundle.getString("city"),
                        getFilesDir().getAbsolutePath()+"/"+bundle.getString("username")+".jpg");
                Log.d("重点排查13", "friend:"+friend+"  headPicPath："+friend.getHeadPicPath());
                friendsViewModel.insertFriends(friend);
            } else if(msg.what == -2){
                searchButton.setVisibility(View.VISIBLE);
                searchProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"连接失败，请检查网络",Toast.LENGTH_LONG).show();
            }else if(msg.what == -1){
                searchButton.setVisibility(View.VISIBLE);
                searchProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"抱歉，该用户不存在",Toast.LENGTH_LONG).show();
            }
        }
    }

    public class DownloadHeadPic extends AsyncTask<Bundle,Void,Void> {
        String TAG ="DownloadTask";
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;
        @Override
        protected Void doInBackground(Bundle...bundles) {

           Bundle bundle = bundles[0];
           String apath = bundle.getString("HPUrl");
           String FID = bundle.getString("username");

            //1.图片路径
            try {
                Log.d("重点排查31", "开始下载图片");
                URL url = new URL(apath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    file = new File(getFilesDir().getAbsolutePath(),FID+".jpg");
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
            bundle.putString("headPicPath",path);
            Message message = new Message();
            message.setData(bundle);
            message.what = 3;
            handler.sendMessage(message);
            return null;
        }
    }

    //搜索用户信息（根据账号）
    public class findUserinfo extends AsyncTask<String,Void,Void>{
        InputStream inputStream = null;
        BufferedReader reader = null;
        String res;
        @Override
        protected Void doInBackground(String... strings) {
            String content = strings[0];
            String path = MyConstants.Friends_URL+ "?username="+content;
            try{
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                    if(res.equals("fail")){
                        handler.sendEmptyMessage(-1);
                    }
                }else {
                    handler.sendEmptyMessage(-2);
                }
                String json = Base64Decoder.decode(res);
                Log.d("重点排查30", "json "+json);
                JSONObject jsonObject = new JSONObject(json);
                Bundle bundle = new Bundle();
                bundle.putString("username",jsonObject.getString("username"));
                bundle.putString("nickname",jsonObject.getString("nickname"));
                bundle.putString("sex",jsonObject.getString("sex"));
                bundle.putString("country",jsonObject.getString("country"));
                bundle.putString("province",jsonObject.getString("province"));
                bundle.putString("city",jsonObject.getString("city"));
                bundle.putString("HPUrl",jsonObject.getString("hPUrl"));
                Message msg = new Message();
                msg.what = 1;
                msg.setData(bundle);
                handler.handleMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
                Log.d("findUserinfo", "查询失败，检查网络");
            }
            return null;
        }
    }

    public class ObserveFriendRequests extends Thread implements Runnable{
        LifecycleOwner lifecycleOwner;

        public ObserveFriendRequests(LifecycleOwner lifecycleOwner) {
            this.lifecycleOwner = lifecycleOwner;
        }

        @Override
        public void run() {
            super.run();
            while (true){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    //开启service
    private void  startServerForData(){
        Intent intent = new Intent(AddFriendActivity.this, UpdateService.class);
        intent.putExtra("username",myUsername);
//        startService(intent);//启动式
        bindService(intent,myserviceconn, Context.BIND_AUTO_CREATE);  //绑定式启动
    }

    //服务器连接实例，通过这个获取接口，从而获取数据
    public class MyServiceConn implements ServiceConnection {

        UpdateService.UpdateBinder binder = null;
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder= (UpdateService.UpdateBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    }

    //通过service回复好友请求
    public void responseFriendRequest(String SID,String RID,String result){
        myserviceconn.binder.responseFriendRequest(SID,RID,result);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(myserviceconn);
    }


    //如果同意好友请求，那么就添加该好友
    public class AddFriend extends AsyncTask<String,Void,Void>{
        InputStream inputStream = null;
        BufferedReader reader = null;
        String res;
        @Override
        protected Void doInBackground(String... strings) {
            String username = strings[0];
            String path = MyConstants.Friends_URL+ "?username="+username;
            try{
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                    if(res.equals("fail")){
                        handler.sendEmptyMessage(-1);
                    }
                }else {
                    handler.sendEmptyMessage(-2);
                }
                String json = Base64Decoder.decode(res);
                JSONObject jsonObject = new JSONObject(json);
                Bundle bundle = new Bundle();
                bundle.putString("username",jsonObject.getString("username"));
                bundle.putString("nickname",jsonObject.getString("nickname"));
                bundle.putString("sex",jsonObject.getString("sex"));
                bundle.putString("country",jsonObject.getString("country"));
                bundle.putString("province",jsonObject.getString("province"));
                bundle.putString("city",jsonObject.getString("city"));
                bundle.putString("HPURl",jsonObject.getString("hPUrl"));
                Message msg = new Message();
                msg.what = 2;
                msg.setData(bundle);
                handler.handleMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
                Log.d("findUserinfo", "查询失败，检查网络");
            }
            return null;
        }
    }
}
