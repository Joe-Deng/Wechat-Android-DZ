package com.example.wechatproj.mainpages.ui.me;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MainActivity;
import com.example.wechatproj.R;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingActivity extends AppCompatActivity {
    private Button loginoutBtn;
    private ImageButton backBtn;

    public SettingActivity() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //初始化
        init();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMe();
            }
        });
        loginoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除sharedPreferences下的数据
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("my_data", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.remove("username");
                edit.commit();

                //关闭所有页面并跳转
                Intent intent = new Intent(SettingActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }



    private void init() {
        loginoutBtn = findViewById(R.id.loginoutBtn);
        backBtn = findViewById(R.id.backButton);
    }

    private void backToMe(){
        Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("ToFragment","me");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMe();
    }
}
