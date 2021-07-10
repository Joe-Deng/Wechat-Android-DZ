package com.example.wechatproj.mainpages.ui.found;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.Database.ViewModel.FriendCirclesViewModel;
import com.example.wechatproj.R;

public class PublishFriendCiecleActivity extends AppCompatActivity {
    private ImageButton backButton;
    private Button publishButton;
    private EditText editText;
    private ImageView imageView;
    private String SID,nickname,headPicPath,imagePath,text;
    FriendCirclesViewModel friendCirclesViewModel;
    private FriendCircle friendCircle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_friend_ciecle);

        backButton = findViewById(R.id.backButton);
        publishButton = findViewById(R.id.publishButton);
        editText = findViewById(R.id.editText);
        imageView = findViewById(R.id.imageView);

        getDates();

        imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));

        //ViewModel
        friendCirclesViewModel = ViewModelProviders.of(this).get(FriendCirclesViewModel.class);
        friendCirclesViewModel.setFriendCircleRepository(SID);


        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"功能正常",Toast.LENGTH_SHORT).show();
                String text = editText.getText().toString();
                if(text.isEmpty()){
                    Toast.makeText(getApplicationContext(),"输入内容不能为空",Toast.LENGTH_SHORT).show();
                }else {
                    friendCircle = new FriendCircle(System.currentTimeMillis(),SID,nickname,headPicPath,text,imagePath,null);
                    friendCirclesViewModel.insertFriendCircle(friendCircle);
                    Intent myIntent = new Intent();
                    myIntent = new Intent(PublishFriendCiecleActivity.this, FriendCircleActivity.class);
                    startActivity(myIntent);
                    PublishFriendCiecleActivity.this.finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent();
                myIntent = new Intent(PublishFriendCiecleActivity.this, FriendCircleActivity.class);
                startActivity(myIntent);
                PublishFriendCiecleActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent myIntent = new Intent();
            myIntent = new Intent(PublishFriendCiecleActivity.this, FriendCircleActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getDates() {
        Bundle bundle = getIntent().getExtras();
        SID = bundle.getString("SID");
        nickname = bundle.getString("nickname");
        headPicPath = bundle.getString("headPicPath");
        imagePath = bundle.getString("imagePath");
    }
}
