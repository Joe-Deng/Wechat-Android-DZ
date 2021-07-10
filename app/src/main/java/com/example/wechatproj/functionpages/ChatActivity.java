package com.example.wechatproj.functionpages;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.R;

public class ChatActivity extends AppCompatActivity {
    FriendsViewModel friendsViewModel;
    Friend friend;
    String TAG = "ChatActivity";
    Boolean ifFromHome = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        setContentView(R.layout.activity_chat);
//        FriendInfoFragment friendInfoFragment = new FriendInfoFragment();
        friendsViewModel =
                ViewModelProviders.of(this).get(FriendsViewModel.class);
        String username = this.getIntent().getExtras().getString("username");

        Log.d(TAG, "得到username："+username);

        friend = friendsViewModel.findFriend(username);
        Log.d(TAG, "得到friend："+friend.getNickname());
        Bundle bundle1 = new Bundle();
        bundle1.putString("username",friend.getUsername());
        bundle1.putString("nickname",friend.getNickname());
        bundle1.putString("sex",friend.getSex());
        bundle1.putString("country",friend.getCountry());
        bundle1.putString("province",friend.getProvince());
        bundle1.putString("city",friend.getCity());
        bundle1.putString("headPicPath",friend.getHeadPicPath());
        Log.d(TAG, "重点排查29："+this.getIntent().getStringExtra("from"));
        if(this.getIntent().getStringExtra("from")!=null && this.getIntent().getStringExtra("from").equals("home")){
            ifFromHome = true;
            NavController controller = Navigation.findNavController(findViewById(R.id.navHost));
            controller.navigate(R.id.chatFragment,bundle1);
        }else {
            NavController controller = Navigation.findNavController(findViewById(R.id.navHost));
            controller.navigate(R.id.friendInfoFragment,bundle1);
        }
//        friendInfoFragment.setArguments(bundle1);

    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle bundle = new Bundle();
        if(ifFromHome){
            bundle.putString("ToFragment","home");
        }else {
            bundle.putString("ToFragment","friends");
        }
        intent.putExtras(bundle);
        startActivity(intent);
        Log.i(TAG, "返回");
    }

    public Boolean getIfFromHome() {
        return ifFromHome;
    }
}
