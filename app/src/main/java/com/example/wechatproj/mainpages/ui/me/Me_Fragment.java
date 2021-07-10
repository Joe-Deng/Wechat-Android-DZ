package com.example.wechatproj.mainpages.ui.me;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class Me_Fragment extends Fragment {
    private MeViewModel meViewModel;
    private Button InfoButton,PayButton,CollectionButton,GalleryButton,CardbagButton,EmojiButton,SettingsButton;
    ImageButton headPic;
    TextView nickname,username;
    SharedPreferences sharedPreferences;

    public Me_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        meViewModel = ViewModelProviders.of(this).get(MeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_me_, container, false);
        return root;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();

        File file = new File("/data/data/"+getContext().getPackageName()+"/shared_prefs/my_data.xml");
        if (file.exists()){
            //加载本地数据
            sharedPreferences = getContext().getSharedPreferences("my_data", Context.MODE_PRIVATE);
            String mUsername = sharedPreferences.getString("username","");
            if(mUsername.length()>13){
                username.setText("微信号:" + mUsername.substring(0,13)+"...");
            }else {
                username.setText("微信号:" + mUsername);
            }
            String mNickname = sharedPreferences.getString("nickname","");
            if(mNickname.length()>10){
                nickname.setText(mNickname.substring(0,10)+"...");
            }else {
                nickname.setText(mNickname);
            }
            String path = sharedPreferences.getString("headPicPath","");
            if(!path.isEmpty()){    //存在图片路径则访问图片文件并加载头像
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ImageShader imageShader = new ImageShader();
                Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,200,200,20);
                headPic.setImageBitmap(renderBitmap);
            }else {
                Toast.makeText(getContext(),"初始化错误_头像",Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(getContext(),"本地无数据，请登录",Toast.LENGTH_LONG).show();
            //返回登录界面等操作
        }

        changeInfo();

        InfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AlterInfoActivity.class);
                startActivity(intent);
            }
        });

        SettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init(){
        InfoButton = getView().findViewById(R.id.InfoButton);
        PayButton = getView().findViewById(R.id.PayButton);
        CollectionButton = getView().findViewById(R.id.CollectionButton);
        GalleryButton = getView().findViewById(R.id.GalleryButton);
        CardbagButton = getView().findViewById(R.id.CardbagButton);
        EmojiButton = getView().findViewById(R.id.EmojiButton);
        SettingsButton = getView().findViewById(R.id.SettingsButton);
        headPic = getView().findViewById(R.id.myHeadPic);
        nickname = getView().findViewById(R.id.myNickname);
        username = getView().findViewById(R.id.myUsername);
    }

    public void changeInfo(){
        Bundle bundle = getArguments();
        if(bundle == null)
            return;
        String isChangeInfo = bundle.getString("ifChangeInfo");
        if(isChangeInfo!=null && !isChangeInfo.isEmpty() && isChangeInfo.equals("true")){
            String mUsername = sharedPreferences.getString("username","");
            if(mUsername.length()>13){
                username.setText("微信号:" + mUsername.substring(0,13)+"...");
            }else {
                username.setText("微信号:" + mUsername);
            }
            String mNickname = sharedPreferences.getString("nickname","");
            if(mNickname.length()>10){
                nickname.setText(mNickname.substring(0,10)+"...");
            }else {
                nickname.setText(mNickname);
            }
            String path = sharedPreferences.getString("headPicPath","");
            if(!path.isEmpty()){    //存在图片路径则访问图片文件并加载头像
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                ImageShader imageShader = new ImageShader();
                Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,200,200,20);
                headPic.setImageBitmap(renderBitmap);
            }else {
                Toast.makeText(getContext(),"初始化错误_头像",Toast.LENGTH_LONG).show();
            }
        }
    }
}
