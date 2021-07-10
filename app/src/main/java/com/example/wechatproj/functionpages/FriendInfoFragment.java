package com.example.wechatproj.functionpages;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.ImageShader;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendInfoFragment extends Fragment {
    ImageButton backButton,moreButton,headPicButton;
    Button remarkButton,permissonButton,chatButton,videoButton;
    ImageView sexImg;
    TextView bigNickname,smallNickname,usernameText,location;
    String TAG = "FriendInfoFragment";
    String username,nickname,sex,country,province,city,headPicPath;
    Bundle bundle;
    FriendsViewModel friendsViewModel;
    MyHandler handler;
    String path = null;

    public FriendInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_info, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "好友资料 "+getArguments().getString("username"));
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
        String bigNicknamestr = nickname;
        if(nickname.length()>13){
            bigNicknamestr = nickname.substring(0,12)+"...";
        }
        bigNickname.setText(bigNicknamestr);
        smallNickname.setText("昵称: "+nickname);
        usernameText.setText("微信号: "+username);
        location.setText("地区: "+country+" "+province+" "+city);

        //实时更新
        friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);
        friendsViewModel.findFriendLive(username).observe(this, new Observer<Friend>() {
            @Override
            public void onChanged(Friend friend) {
                Log.d("重点排查50", "friend:"+friend+"  "+friend.getHeadPicPath());
                final Bitmap fbitmap = BitmapFactory.decodeFile(friend.getHeadPicPath());
                ImageShader fimageShader = new ImageShader();
                Bitmap frenderBitmap = fimageShader.roundBitmapByShader(fbitmap,200,200,20);
                headPicButton.setImageBitmap(frenderBitmap);

                if(friend.getSex() == "女"){
                    sexImg.setImageResource(R.drawable.female);
                }else{
                    sexImg.setImageResource(R.drawable.male);
                }
                //暂时为nickname，之后再加备注功能
                nickname = friend.getNickname();
                String bigNicknamestr = nickname;
                if(nickname.length()>13){
                    bigNicknamestr = nickname.substring(0,12)+"...";
                }
                bigNickname.setText(bigNicknamestr);
                smallNickname.setText("昵称: "+nickname);
                username = friend.getUsername();
                usernameText.setText("微信号: "+username);
                country = friend.getCountry();
                province = friend.getProvince();
                city = friend.getCity();
                location.setText("地区: "+country+" "+province+" "+city);
            }
        });


        new findUserinfo().execute(username);


        //跳转到聊天界面
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller = Navigation.findNavController(v);
                controller.navigate(R.id.chatFragment,bundle);
            }
        });

        //返回按钮
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ToFragment","friends");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }



    private void init() {
        backButton = getView().findViewById(R.id.backButton);
        moreButton = getView().findViewById(R.id.moreButton);
        headPicButton = getView().findViewById(R.id.headPic);
        remarkButton = getView().findViewById(R.id.remark_button);
        permissonButton = getView().findViewById(R.id.permission_button);
        sexImg = getView().findViewById(R.id.sexImage);
        chatButton = getView().findViewById(R.id.chat_button);
        videoButton = getView().findViewById(R.id.video_button);
        bigNickname = getView().findViewById(R.id.bigNickname);
        smallNickname = getView().findViewById(R.id.smallNickname);
        usernameText = getView().findViewById(R.id.username);
        location = getView().findViewById(R.id.location);
        handler = new MyHandler();
    }

    private void getDataFromArgument(){
        bundle = getArguments();
        username = bundle.getString("username");
        nickname = bundle.getString("nickname");
        sex = bundle.getString("sex");
        country = bundle.getString("country");
        province = bundle.getString("province");
        city = bundle.getString("city");
        headPicPath = bundle.getString("headPicPath");
    }


    //搜索用户信息（根据账号）
    public class findUserinfo extends AsyncTask<String,Void,Void> {
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

    private class MyHandler extends Handler {
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
                Bundle bundle = msg.getData();
                Friend friend = new Friend(bundle.getString("username"),
                        bundle.getString("nickname"),
                        bundle.getString("sex"),
                        bundle.getString("country"),
                        bundle.getString("province"),
                        bundle.getString("city"),
                        bundle.getString("headPicPath"));
                friendsViewModel.updateFriends(friend);
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
                    file = new File(getContext().getFilesDir().getAbsolutePath(),FID+".jpg");
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
}
