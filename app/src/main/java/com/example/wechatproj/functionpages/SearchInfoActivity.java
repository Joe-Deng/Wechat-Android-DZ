package com.example.wechatproj.functionpages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SearchInfoActivity extends AppCompatActivity {
    ImageButton backButton,moreButton,headPicButton;
    Button remarkButton,addButton;
    ImageView sexImg;
    TextView bigNickname,smallNickname,usernameText,location;
    String TAG = "FriendInfoFragment";
    String username,nickname,sex,country,province,city,headPicPath,SID;
    Bundle bundle;
    private static String FriendRequest_URL = MyConstants.FriendRequest_URL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_info);
        init();
        getDataFromArgument();
        /** 裁剪图片-圆角 **/
        final Bitmap bitmap = BitmapFactory.decodeFile(headPicPath);
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,200,200,20);
        headPicButton.setImageBitmap(renderBitmap);
        if(sex == "女"){
            sexImg.setImageResource(R.drawable.female);
        }
        //暂时为nickname，之后再加备注功能
        bigNickname.setText(nickname);
        smallNickname.setText("昵称: "+nickname);
        usernameText.setText("微信号: "+username);
        location.setText("地区: "+country+" "+province+" "+city);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new sendFriendRequestAsync().execute();
            }
        });
    }

    private void init() {
        backButton = findViewById(R.id.backButton);
        moreButton = findViewById(R.id.moreButton);
        headPicButton = findViewById(R.id.headPic);
        remarkButton = findViewById(R.id.remark_button);
        sexImg = findViewById(R.id.sexImage);
        addButton = findViewById(R.id.addFriend_button);
        bigNickname = findViewById(R.id.bigNickname);
        smallNickname = findViewById(R.id.smallNickname);
        usernameText = findViewById(R.id.username);
        location = findViewById(R.id.location);
    }

    private void getDataFromArgument(){
        bundle = getIntent().getExtras();
        username = bundle.getString("username");
        nickname = bundle.getString("nickname");
        sex = bundle.getString("sex");
        country = bundle.getString("country");
        province = bundle.getString("province");
        city = bundle.getString("city");
        headPicPath = bundle.getString("headPicPath");
        SID = getSharedPreferences("my_data", MODE_PRIVATE).getString("username","");
    }

    public class sendFriendRequestAsync extends AsyncTask<Void,Void,Void>{
        MyHandler handler = new MyHandler();

        @Override
        protected Void doInBackground(Void... voids) {
            final String TAG ="GetNewFriendRequests";
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("RID" ,username)
                    .add("SID",SID)
                    .build();

            final Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(FriendRequest_URL)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "获取消息数据失败 - - 网络错误");
                    handler.sendEmptyMessage(-2);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应--GetMessage");
                    String res = response.body().string();
                    if(res.equals("fail")){
                        Log.d(TAG, "onResponse: "+res);
                        handler.sendEmptyMessage(-1);
                    }else if(res.equals("wait")){
                        Log.d(TAG, "onResponse: "+res);
                        handler.sendEmptyMessage(-3);
                    } else {
                        Log.d(TAG, "onResponse: "+res);
                        handler.sendEmptyMessage(1);
                    }
                    //Handler
                }
            });
            return null;
        }

        public class MyHandler extends Handler{
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == -1){
                    Toast.makeText(getApplicationContext(),"发送好友请求失败",Toast.LENGTH_LONG).show();
                }else if(msg.what == 1){
                    Toast.makeText(getApplicationContext(),"发送成功",Toast.LENGTH_LONG).show();
                }else if(msg.what == -2){
                    Toast.makeText(getApplicationContext(),"请求超时，请检查网络",Toast.LENGTH_LONG).show();
                }else if(msg.what == -3){
                    Toast.makeText(getApplicationContext(),"你已提交过好友请求，请等待回应",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
