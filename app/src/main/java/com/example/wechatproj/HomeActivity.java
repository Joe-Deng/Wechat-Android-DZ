package com.example.wechatproj;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Entity.FriendRequest;
import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Database.ViewModel.FriendRequestsViewModel;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.Database.ViewModel.MessagesViewModel;
import com.example.wechatproj.Services.UpdateService;
import com.example.wechatproj.ViewModel.HomeBottomViewModel;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    View navLayout;
    Button button1,button2,button3,button4;
    ImageView imageView1,imageView2,imageView3,imageView4;
    TextView textView1,textView2,textView3,textView4;
    TextView redPot1,redPot2,redPot3;
    TextView titleText;
    ImageButton navButton1,navButton2,cameraButton;
    String username;
    int MESSAGE_NUM = 0;
    int FRIEND_REQUEST_NUM = 0 ;
    View hostFragment;
    private GestureDetector detector; //????????????
    final int Distance = 50;    //????????????
    public int CurrentFragment = 1; //???????????????fragment??????
    //???????????????????????????
    String title1 = "??????";
    String title2 = "?????????";
    String title3 = "??????";
    private int DEFAULT_COLOR = R.color.myDarkGray;
    private int ACTIVE_COLOR = R.color.myGreen;
    MyServiceConn myserviceconn = new MyServiceConn();
    //ViewModel ????????????????????????????????????
    MessagesViewModel messagesViewModel;
    FriendRequestsViewModel friendRequestsViewModel;
    FriendsViewModel friendsViewModel;
    SharedPreferences sharedPreferences = null;
    MyHandler handler;
    Boolean isIniting = false;
    //??????????????????????????????????????????????????????
//    HomeBottomViewModel numViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //???????????????????????????service ??????>  ?????????????????? ??????>  ??????Update??????Service

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //????????????????????????????????????????????????username
        File file = new File("/data/data/"+getPackageName()+"/shared_prefs/my_data.xml");
        if(!file.exists()){
                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                startActivity(intent);
        }
        sharedPreferences = getSharedPreferences("my_data", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        //???????????????
        init();

        //????????????????????????Activity????????????
        keepAlive();

        //????????????
        detector = new GestureDetector(this,listener);

        //????????????ViewModel
        //??????????????????messagesViewModel
        messagesViewModel = ViewModelProviders.of(this).get(MessagesViewModel.class);
        messagesViewModel.setMessageRepository(username);
        //??????????????????FriendRequestsViewModel
        friendRequestsViewModel = ViewModelProviders.of(this).get(FriendRequestsViewModel.class);
        friendRequestsViewModel.setFriendRequestRepository(username);
        //???????????????friendsViewModel
        friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);


    }

    @Override
    protected void onStart() {
        super.onStart();

        //??????????????????????????????????????????????????????????????????????????????ViewModel????????????
        startServerForData();
        //        ????????????ViewModel
        ObserveAllViewModel();

        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if(sharedPreferences.getString("IF_First_Login","").isEmpty()){
            //??????????????????
            new InitDataFromServer().start();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IF_First_Login","false");
            editor.commit();
        }


        //????????????????????????
        new startUpdateAsync().start();
//        new UpdateTask().start();
    }

    private void init() {
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.toRegist_button);
        button3 = findViewById(R.id.foget_button);
        button4 = findViewById(R.id.button4);
        navButton1 = findViewById(R.id.navButton1);
        navButton2 = findViewById(R.id.navButton2);
        cameraButton = findViewById(R.id.navButton2_camera);
        imageView1 = findViewById(R.id.image1);
        imageView2 = findViewById(R.id.image2);
        imageView3 = findViewById(R.id.image3);
        imageView4 = findViewById(R.id.image4);
        redPot1 = findViewById(R.id.redPot1);
        redPot2 = findViewById(R.id.redPot2);
        redPot3 = findViewById(R.id.redPot3);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        titleText = findViewById(R.id.title_text);
        navLayout = findViewById(R.id.navLayout);
        hostFragment = findViewById(R.id.fragmentHost);
        handler = new MyHandler();

        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadFragment1();
                NavController navController = Navigation.findNavController(hostFragment);
                navController.navigate(R.id.homeFragment);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment2();
                NavController navController = Navigation.findNavController(hostFragment);
                navController.navigate(R.id.friendsFragment);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment3();
                NavController navController = Navigation.findNavController(hostFragment);
                navController.navigate(R.id.foundFragment);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment4();
                NavController navController = Navigation.findNavController(hostFragment);
                navController.navigate(R.id.me_Fragment);
            }
        });
    }


    //??????Fragment??????????????????
        @SuppressLint("ResourceAsColor")
        private void loadFragment1(){
            navLayout.setBackgroundColor(getResources().getColor(R.color.white_gray));
            navButton1.setVisibility(View.VISIBLE);
            navButton2.setVisibility(View.VISIBLE);
            titleText.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.INVISIBLE);

            imageView1.setImageResource(R.drawable.home_active);
            textView1.setTextColor(getResources().getColor(ACTIVE_COLOR));
            String title = title1;
            if(MESSAGE_NUM != 0){
                title += "(" + MESSAGE_NUM + ")";
            }
            titleText.setText(title);

            imageView2.setImageResource(R.drawable.friends);
            textView2.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView3.setImageResource(R.drawable.found);
            textView3.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView4.setImageResource(R.drawable.me);
            textView4.setTextColor(getResources().getColor(DEFAULT_COLOR));

            CurrentFragment = 1;
        }

        @SuppressLint("ResourceAsColor")
        private void loadFragment2(){
            navLayout.setBackgroundColor(getResources().getColor(R.color.white_gray));
            navButton1.setVisibility(View.VISIBLE);
            navButton2.setVisibility(View.VISIBLE);
            titleText.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.INVISIBLE);


            imageView2.setImageResource(R.drawable.friends_active);
            textView2.setTextColor(getResources().getColor(ACTIVE_COLOR));
            String title = title2;
            if(FRIEND_REQUEST_NUM != 0){
                title += "(" + FRIEND_REQUEST_NUM + ")";
            }
            titleText.setText(title);

            imageView1.setImageResource(R.drawable.home);
            textView1.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView3.setImageResource(R.drawable.found);
            textView3.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView4.setImageResource(R.drawable.me);
            textView4.setTextColor(getResources().getColor(DEFAULT_COLOR));

            CurrentFragment = 2;
        }

        @SuppressLint("ResourceAsColor")
        private void loadFragment3(){
            navLayout.setBackgroundColor(getResources().getColor(R.color.white_gray));
            navButton1.setVisibility(View.VISIBLE);
            navButton2.setVisibility(View.VISIBLE);
            titleText.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.INVISIBLE);

            imageView3.setImageResource(R.drawable.found_active);
            textView3.setTextColor(getResources().getColor(ACTIVE_COLOR));
            String title = title3;
            titleText.setText(title);

            imageView1.setImageResource(R.drawable.home);
            textView1.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView2.setImageResource(R.drawable.friends);
            textView2.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView4.setImageResource(R.drawable.me);
            textView4.setTextColor(getResources().getColor(DEFAULT_COLOR));

            CurrentFragment = 3;
        }

        @SuppressLint("ResourceAsColor")
        private void loadFragment4(){
            navLayout.setBackgroundColor(getResources().getColor(R.color.white));
            navButton1.setVisibility(View.INVISIBLE);
            navButton2.setVisibility(View.INVISIBLE);
            titleText.setVisibility(View.INVISIBLE);
            cameraButton.setVisibility(View.VISIBLE);

            imageView4.setImageResource(R.drawable.me_active);
            textView4.setTextColor(getResources().getColor(ACTIVE_COLOR));
            String title = title3;
            titleText.setText(title);

            imageView1.setImageResource(R.drawable.home);
            textView1.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView2.setImageResource(R.drawable.friends);
            textView2.setTextColor(getResources().getColor(DEFAULT_COLOR));
            imageView3.setImageResource(R.drawable.found);
            textView3.setTextColor(getResources().getColor(DEFAULT_COLOR));

            CurrentFragment = 4;
        }


    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        //?????????true
        return true;
    }

    //????????????
    private GestureDetector.OnGestureListener listener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = e2.getX()-e1.getX();
            //??????
            if(x< -50){
                switch (CurrentFragment){
                    case 1:
                    {
                        loadFragment2();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_homeFragment_to_friendsFragment);
                        break;
                    }
                    case 2:{
                        loadFragment3();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_friendsFragment_to_foundFragment);
                        break;
                    }
                    case 3:{
                        loadFragment4();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_foundFragment_to_me_Fragment);
                        break;
                    }
                }
            }else if (x>50){  //??????
                switch (CurrentFragment){
                    case 2:
                    {
                        loadFragment1();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_friendsFragment_to_homeFragment);
                        break;
                    }
                    case 3:{
                        loadFragment2();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_foundFragment_to_friendsFragment);
                        break;
                    }
                    case 4:{
                        loadFragment3();
                        NavController controller = Navigation.findNavController(hostFragment);
                        controller.navigate(R.id.action_me_Fragment_to_foundFragment);
                        break;
                    }
                }
            }
            return true;
        }
    };

    // ?????????????????????Activity ????????????
    private void keepAlive(){
        if(this.getIntent()!=null){
            Intent intent = this.getIntent();
            if(intent.getExtras()!=null){
                Bundle bundle = intent.getExtras();
                if(bundle.getString("ToFragment")!= null){
                    String toFragment = bundle.getString("ToFragment");
                    if(toFragment != null && !toFragment.isEmpty()){
                        switch (toFragment){
                            case "home":{
                                loadFragment1();
                                NavController controller = Navigation.findNavController(findViewById(R.id.fragmentHost));
                                controller.navigate(R.id.homeFragment);
                            }
                            break;
                            case "friends":{
                                loadFragment2();
                                NavController controller = Navigation.findNavController(findViewById(R.id.fragmentHost));
                                controller.navigate(R.id.friendsFragment);
                            }
                            break;
                            case "found":{
                                loadFragment3();
                                NavController controller = Navigation.findNavController(findViewById(R.id.fragmentHost));
                                controller.navigate(R.id.foundFragment);
                            }
                            break;
                            case "me":{
                                loadFragment4();
                                NavController controller = Navigation.findNavController(findViewById(R.id.fragmentHost));
                                controller.navigate(R.id.me_Fragment);
                            }
                            break;
                            case "alterInfo":{
                                loadFragment4();
                                Bundle bundle1 = new Bundle();
                                bundle1.putString("ifChangeInfo","true");
                                NavController controller = Navigation.findNavController(findViewById(R.id.fragmentHost));
                                controller.navigate(R.id.me_Fragment,bundle1);
                            }
                        }
                    }
                }
            }
        }
    }


    //??????service
    private void  startServerForData(){
        Intent intent = new Intent(HomeActivity.this, UpdateService.class);
        intent.putExtra("username",username);
//        startService(intent);//?????????
        bindService(intent,myserviceconn, Context.BIND_AUTO_CREATE);  //???????????????
    }

    //?????????????????????????????????????????????????????????????????????
    public class MyServiceConn implements ServiceConnection {

        UpdateService.UpdateBinder binder = null;
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MyServiceConn", "???????????????");
            binder= (UpdateService.UpdateBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    }

    //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    public class InitDataFromServer extends Thread implements Runnable{
        String TAG = "InitDataFromServer";
        @Override
        public void run() {
            super.run();
            isIniting = true;
            while (myserviceconn.binder == null){
                try {    //??????1s?????????activity ??? Service ?????????????????????????????????????????????-- ????????????S ??????
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("????????????19", "??????????????????????????? ");
            }
//            myserviceconn.binder.InitGetAllData();
            if(myserviceconn.binder.InitGetAllData()){// ?????????????????????????????????????????????????????????????????????????????????UI
                List<Message> allMessages = myserviceconn.binder.getAllMessages();
                List<FriendRequest> allFriendRequests = myserviceconn.binder.getAllFriendRequests();
                List<Friend> allFriends = myserviceconn.binder.getAllFriends();
                Log.d("????????????19", "???????????? ");
                //?????????????????????
                List<Long> mTimes = new ArrayList<>();       //??????????????????????????????????????????????????????????????????IF_Receive???????????????
                List<String> friendUsernames = new ArrayList<>();    //????????????????????????????????????????????????????????????????????????IF_Receive??????
                if(allMessages!=null && !allMessages.isEmpty()){
                    for (Message message: allMessages){
                        messagesViewModel.insertMessages(message);
                        if(message.getRID().equals(username)){
                            mTimes.add(message.getM_Time());
                        }
                    }
                }
                if(allFriendRequests!=null && !allFriendRequests.isEmpty()){
                    for(FriendRequest friendRequest:allFriendRequests){
                        friendRequestsViewModel.insetFriendRequest(friendRequest);
                        friendUsernames.add(friendRequest.getUsername());
                    }
                }
                if(allFriends!=null && !allFriends.isEmpty()){
                    for(Friend friend:allFriends){
                        friendsViewModel.insertFriends(friend);
                    }
                }
                //?????????????????????????????????????????????????????????IF_Receive(??????service)
                if(mTimes!=null && !mTimes.isEmpty()){
                    Log.d(TAG, "????????????????????????m_Times");
                    myserviceconn.binder.ReceiveOk(mTimes);
//                    mTimes.clear();
                }
                if(friendUsernames!=null && !friendUsernames.isEmpty()){
                    Log.d(TAG, "????????????????????????friendUsernames");
                    myserviceconn.binder.ReceiveOk2(friendUsernames);
//                    friendUsernames.clear();
                }
                Log.d("????????????19", "?????????????????????????????????????????? ");
                isIniting = false;
            }
        }

    }

    //????????????????????????????????????????????????????????? ??????????????????????????????????????????????????????????????????????????????
    public class UpdateTask extends Thread implements Runnable{
        String TAG = "UpdateTask";
        @Override
        public void run() {
            super.run();
            while (isIniting){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (myserviceconn.binder == null){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<Message> newMessages = new ArrayList<>();
            newMessages.addAll(myserviceconn.binder.getNewMessages());
            List<FriendRequest> newFriendRequests = new ArrayList<>();
            newFriendRequests.addAll(myserviceconn.binder.getNewFriendRequests());
            List<Friend> newFriends = new ArrayList<>();
            newFriends.addAll(myserviceconn.binder.getNewFriends());
            List<Long> mTimes = new ArrayList<>();       //??????????????????????????????????????????????????????????????????IF_Receive???????????????
            List<String> friendUsernames = new ArrayList<>();    //????????????????????????????????????????????????????????????????????????IF_Receive??????
            // ????????????????????????;
            myserviceconn.binder.deleteMessages();
//            Log.d("????????????36", "newMessages="+newMessages);
            //??????????????????????????????????????????????????????????????????
//            Log.d(TAG, "????????????6");
           if(newMessages!=null && !newMessages.isEmpty()){
               for(Message message : newMessages){
//                   Log.d(TAG, "????????????7???????????????????????????"+message.getM_Content());
                   Message foundMessage = messagesViewModel.getMessage(message.getM_Time());
//                   Log.d(TAG, "????????????8???????????????????????????"+foundMessage);
//                   Log.d("????????????37", "newMessages="+newMessages);
                   if(foundMessage!=null && foundMessage.getM_Time() == message.getM_Time()){
                       //???????????????????????????????????????????????????????????????????????????dao????????????10ms?????????
                       //10ms???????????????????????????????????????????????????????????????????????????????????????????????????
                       //??????????????????????????????????????????
                       continue;
                   }
//                   Log.d("????????????37", "????????????"+message.getM_Content());
                   messagesViewModel.insertMessages(message);
//                Log.d(TAG, "????????????9");
//                   Log.d("????????????37", "?????????"+message.getM_Time());
                   mTimes.add(message.getM_Time());
//                   Log.d(TAG, "???????????????????????? "+message.getM_Type()+message.getM_Content()
//                   +"  mtimes="+mTimes);
               }
               //?????????????????????????????????????????????????????????IF_Receive(??????service)
               if(mTimes!=null && !mTimes.isEmpty()){
                   Log.d(TAG, "????????????????????????m_Times"+mTimes);
                   myserviceconn.binder.ReceiveOk(mTimes);
                   mTimes.clear();
               }
           }
            //??????????????????????????????
            if(newFriendRequests!=null && !newFriendRequests.isEmpty()){
                for(FriendRequest friendRequest : newFriendRequests){
                    //???????????????????????????????????????
//                Log.d(TAG, "????????????5:"+friendRequestsViewModel.findFriendRequest(friendRequest.getUsername()));
                    FriendRequest foundFriendRequest = friendRequestsViewModel.findFriendRequest(friendRequest.getUsername());
                    if(foundFriendRequest!=null && foundFriendRequest.getUsername().equals(friendRequest.getUsername())){
                        //???????????????????????????????????????????????????????????????????????????dao????????????10ms?????????
                        //10ms???????????????????????????????????????????????????????????????????????????????????????????????????
                        //????????????????????????????????????????????????????????????
                        continue;
                    }
//                Log.d(TAG, "????????????5??????????????????");
                    friendRequestsViewModel.insetFriendRequest(friendRequest);
                    friendUsernames.add(friendRequest.getUsername());
                    Log.d(TAG, "?????????????????????????????? "+friendRequest.getUsername());
                }
            }
            if(friendUsernames!=null && !friendUsernames.isEmpty()){
                Log.d(TAG, "????????????????????????friendUsernames");
                myserviceconn.binder.ReceiveOk2(friendUsernames);
                friendUsernames.clear();
            }
            //????????????????????????
            if(newFriends!=null && !newFriends.isEmpty()){
                for(Friend friend : newFriends){
                    if(friendsViewModel.findFriend(friend.getUsername())!=null){
                        //?????????????????????????????????????????????????????????????????????????????????????????????
                        myserviceconn.binder.deleteFriendRequest(username,friend.getUsername());
                        continue;
                    }
                    //???????????????,???????????????handler???
                    new DownloadHeadPicTask().execute(friend);
                }
            }


        }
    }

    private class startUpdateAsync extends Thread implements Runnable{
        @Override
        public void run() {
            super.run();
            while (true){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new UpdateTask().start();
            }
        }
    }



    //????????????ViewModel?????????????????????
    public void ObserveAllViewModel(){
//            messagesViewModel.getAllMessageLive().observe(this, new Observer<List<Message>>() {
//                @Override
//                public void onChanged(List<Message> messages) {
//
//                }
//            });

        //????????????????????? ?????? ???????????????
        messagesViewModel.getAllNewMessageLive().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                int newMessageCount = messages.size();
                if(newMessageCount == 0 ){
                    redPot1.setVisibility(View.INVISIBLE);
                }else if(newMessageCount<100){
                    redPot1.setVisibility(View.VISIBLE);
                    redPot1.setText(String.valueOf(newMessageCount));
                }else {
                    redPot1.setVisibility(View.VISIBLE);
                    redPot1.setText("99+");
                }
            }
        });

        //????????????????????? ???????????? ???????????????
        friendRequestsViewModel.getNewFriendRequestLive().observe(this, new Observer<List<FriendRequest>>() {
            @Override
            public void onChanged(List<FriendRequest> friendRequests) {
                Log.d("????????????", "getValue: " +friendRequestsViewModel.getNewFriendRequestLive()+"    "+friendRequestsViewModel.getNewFriendRequestLive().getValue());

                int newFriendRequestCount = friendRequests.size();
                if(newFriendRequestCount == 0){
                    redPot2.setVisibility(View.INVISIBLE);
                }else if(newFriendRequestCount < 100){
                    redPot2.setVisibility(View.VISIBLE);
                    redPot2.setText(String.valueOf(newFriendRequestCount));
                }else {
                    redPot2.setVisibility(View.VISIBLE);
                    redPot2.setText("99+");
                }
            }
        });

        //???????????????????????????  ?????? ?????????????????????

    }

    //????????????
    private class DownloadHeadPicTask extends AsyncTask<Friend,Void,Void>{
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        private String headpicPath = null;
        @Override
        protected Void doInBackground(Friend... friends) {
            Friend friend = friends[0];
            String downloadpath = friend.getHeadPicPath();
            Log.d("????????????35", "path= "+downloadpath);
            URL url = null;
            try {
                url = new URL(downloadpath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                String SID = friend.getUsername();
                if(conn.getResponseCode() == 200) {
                    inputStream = conn.getInputStream();
                    File file = new File(getFilesDir().getAbsolutePath(), SID + ".jpg");
                    fileOutputStream = new FileOutputStream(file);
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer) )!= -1){
                        fileOutputStream.write(buffer,0,len);
                    }
                    fileOutputStream.flush();
                    headpicPath = file.getPath();
                    Log.d("????????????????????????????????????", "headPicPath="+headpicPath);
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
                    if(fileOutputStream != null){
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
                //???????????????????????????????????????
            }
            Bundle bundle = new Bundle();
            bundle.putString("username",friend.getUsername());
            bundle.putString("nickname",friend.getNickname());
            bundle.putString("headPicPath",headpicPath); //????????????????????????????????????
            bundle.putString("sex",friend.getSex());
            bundle.putString("country",friend.getCountry());
            bundle.putString("province",friend.getProvince());
            bundle.putString("city",friend.getCity());
            android.os.Message msg = new android.os.Message();
            msg.setData(bundle);
            msg.what = 1;
            handler.sendMessage(msg);
            return null;
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Bundle bundle = msg.getData();
                Friend newFriend = new Friend(bundle.getString("username"),
                        bundle.getString("nickname"),
                        bundle.getString("sex"),
                        bundle.getString("country"),
                        bundle.getString("province"),
                        bundle.getString("city"),
                        bundle.getString("headPicPath"));
                friendsViewModel.insertFriends(newFriend);
                Log.d("HomeActivity", "????????????????????? ");
                myserviceconn.binder.deleteFriendRequest(username,bundle.getString("username"));
            } else if (msg.what == -1) {
                Toast.makeText(getApplicationContext(), "???????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(myserviceconn);
    }

}
