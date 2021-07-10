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
    private GestureDetector detector; //滑动手势
    final int Distance = 50;    //滑动距离
    public int CurrentFragment = 1; //当前显示的fragment序号
    //底部导航栏的按钮数
    String title1 = "微信";
    String title2 = "通讯录";
    String title3 = "发现";
    private int DEFAULT_COLOR = R.color.myDarkGray;
    private int ACTIVE_COLOR = R.color.myGreen;
    MyServiceConn myserviceconn = new MyServiceConn();
    //ViewModel 负责更新底部导航栏的提示
    MessagesViewModel messagesViewModel;
    FriendRequestsViewModel friendRequestsViewModel;
    FriendsViewModel friendsViewModel;
    SharedPreferences sharedPreferences = null;
    MyHandler handler;
    Boolean isIniting = false;
    //数字用于计数，用于更新底部导航栏提示
//    HomeBottomViewModel numViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //注意执行步骤！开启service ——>  开启界面监听 ——>  开启Update轮询Service

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //检测登录状态，如果已登录，初始化username
        File file = new File("/data/data/"+getPackageName()+"/shared_prefs/my_data.xml");
        if(!file.exists()){
                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                startActivity(intent);
        }
        sharedPreferences = getSharedPreferences("my_data", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");

        //界面初始化
        init();

        //检测是否从其他的Activity跳转回来
        keepAlive();

        //滑动手势
        detector = new GestureDetector(this,listener);

        //获取所有ViewModel
        //这两步初始化messagesViewModel
        messagesViewModel = ViewModelProviders.of(this).get(MessagesViewModel.class);
        messagesViewModel.setMessageRepository(username);
        //这两步初始化FriendRequestsViewModel
        friendRequestsViewModel = ViewModelProviders.of(this).get(FriendRequestsViewModel.class);
        friendRequestsViewModel.setFriendRequestRepository(username);
        //这步初始化friendsViewModel
        friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);


    }

    @Override
    protected void onStart() {
        super.onStart();

        //开启后台服务，定时轮询服务器数据并更新数据库，再通过ViewModel进行展示
        startServerForData();
        //        界面监听ViewModel
        ObserveAllViewModel();

        //获取所有服务器数据，如果是第一次登录则肯定不包含这条数据，那么进行初始化请求服务器所有数据
        if(sharedPreferences.getString("IF_First_Login","").isEmpty()){
            //请求所有数据
            new InitDataFromServer().start();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IF_First_Login","false");
            editor.commit();
        }


        //开启更新轮询任务
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


    //四个Fragment跳转加载事件
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
        //必须是true
        return true;
    }

    //滑动手势
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
            //左滑
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
            }else if (x>50){  //右滑
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

    // 检测是否从其他Activity 跳转回来
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


    //开启service
    private void  startServerForData(){
        Intent intent = new Intent(HomeActivity.this, UpdateService.class);
        intent.putExtra("username",username);
//        startService(intent);//启动式
        bindService(intent,myserviceconn, Context.BIND_AUTO_CREATE);  //绑定式启动
    }

    //服务器连接实例，通过这个获取接口，从而获取数据
    public class MyServiceConn implements ServiceConnection {

        UpdateService.UpdateBinder binder = null;
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MyServiceConn", "建立了连接");
            binder= (UpdateService.UpdateBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    }

    //第一次登录本机，将服务器的所有数据请求过来———————异步—————————————————
    public class InitDataFromServer extends Thread implements Runnable{
        String TAG = "InitDataFromServer";
        @Override
        public void run() {
            super.run();
            isIniting = true;
            while (myserviceconn.binder == null){
                try {    //延迟1s，等待activity 与 Service 连接（这个数值很重要，也可变）-- 重点排查S ——
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("重点排查19", "循环结束就不再回来 ");
            }
//            myserviceconn.binder.InitGetAllData();
            if(myserviceconn.binder.InitGetAllData()){// 会阻塞主线程来进行网络请求，当获取到所有消息才开始渲染UI
                List<Message> allMessages = myserviceconn.binder.getAllMessages();
                List<FriendRequest> allFriendRequests = myserviceconn.binder.getAllFriendRequests();
                List<Friend> allFriends = myserviceconn.binder.getAllFriends();
                Log.d("重点排查19", "阻塞结束 ");
                //存入本地数据库
                List<Long> mTimes = new ArrayList<>();       //用于向服务器发送“我已收到这些消息，可以修改IF_Receive了”的请求
                List<String> friendUsernames = new ArrayList<>();    //用于向服务器发送“我已收到这些好友请求，可以修改IF_Receive了”
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
                //再给服务器发送我已收到的消息，让他修改IF_Receive(通过service)
                if(mTimes!=null && !mTimes.isEmpty()){
                    Log.d(TAG, "准备发送确认请求m_Times");
                    myserviceconn.binder.ReceiveOk(mTimes);
//                    mTimes.clear();
                }
                if(friendUsernames!=null && !friendUsernames.isEmpty()){
                    Log.d(TAG, "准备发送确认请求friendUsernames");
                    myserviceconn.binder.ReceiveOk2(friendUsernames);
//                    friendUsernames.clear();
                }
                Log.d("重点排查19", "数据全部插入数据库，线程结束 ");
                isIniting = false;
            }
        }

    }

    //数据库更新任务，仍然是通过轮询获取数据 ————————核心————————————————
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
            List<Long> mTimes = new ArrayList<>();       //用于向服务器发送“我已收到这些消息，可以修改IF_Receive了”的请求
            List<String> friendUsernames = new ArrayList<>();    //用于向服务器发送“我已收到这些好友请求，可以修改IF_Receive了”
            // 取到数据立刻删除;
            myserviceconn.binder.deleteMessages();
//            Log.d("重点排查36", "newMessages="+newMessages);
            //遍历新收到的消息结果集，判断数据库是否已存在
//            Log.d(TAG, "重点排查6");
           if(newMessages!=null && !newMessages.isEmpty()){
               for(Message message : newMessages){
//                   Log.d(TAG, "重点排查7，获取到了新消息："+message.getM_Content());
                   Message foundMessage = messagesViewModel.getMessage(message.getM_Time());
//                   Log.d(TAG, "重点排查8，数据库查重结果："+foundMessage);
//                   Log.d("重点排查37", "newMessages="+newMessages);
                   if(foundMessage!=null && foundMessage.getM_Time() == message.getM_Time()){
                       //第二个判断是为了避免拿到的是前一次查询的消息，因为dao查询预判10ms时间，
                       //10ms就返回，如果已经返回了，那么查询结果就可能会在下一次查询的时候返回
                       //如果数据库已存在该数据，略过
                       continue;
                   }
//                   Log.d("重点排查37", "准备插入"+message.getM_Content());
                   messagesViewModel.insertMessages(message);
//                Log.d(TAG, "重点排查9");
//                   Log.d("重点排查37", "获取到"+message.getM_Time());
                   mTimes.add(message.getM_Time());
//                   Log.d(TAG, "插入一条新消息： "+message.getM_Type()+message.getM_Content()
//                   +"  mtimes="+mTimes);
               }
               //再给服务器发送我已收到的消息，让他修改IF_Receive(通过service)
               if(mTimes!=null && !mTimes.isEmpty()){
                   Log.d(TAG, "准备发送确认请求m_Times"+mTimes);
                   myserviceconn.binder.ReceiveOk(mTimes);
                   mTimes.clear();
               }
           }
            //遍历新收到的好友请求
            if(newFriendRequests!=null && !newFriendRequests.isEmpty()){
                for(FriendRequest friendRequest : newFriendRequests){
                    //根据用户名去判断是否收到过
//                Log.d(TAG, "重点排查5:"+friendRequestsViewModel.findFriendRequest(friendRequest.getUsername()));
                    FriendRequest foundFriendRequest = friendRequestsViewModel.findFriendRequest(friendRequest.getUsername());
                    if(foundFriendRequest!=null && foundFriendRequest.getUsername().equals(friendRequest.getUsername())){
                        //第二个判断是为了避免拿到的是前一次查询的消息，因为dao查询预判10ms时间，
                        //10ms就返回，如果已经返回了，那么查询结果就可能会在下一次查询的时候返回
                        //如果数据库已存在该用户所发过的请求，略过
                        continue;
                    }
//                Log.d(TAG, "重点排查5，执行到这了");
                    friendRequestsViewModel.insetFriendRequest(friendRequest);
                    friendUsernames.add(friendRequest.getUsername());
                    Log.d(TAG, "插入一条新好友请求： "+friendRequest.getUsername());
                }
            }
            if(friendUsernames!=null && !friendUsernames.isEmpty()){
                Log.d(TAG, "准备发送确认请求friendUsernames");
                myserviceconn.binder.ReceiveOk2(friendUsernames);
                friendUsernames.clear();
            }
            //遍历新添加的朋友
            if(newFriends!=null && !newFriends.isEmpty()){
                for(Friend friend : newFriends){
                    if(friendsViewModel.findFriend(friend.getUsername())!=null){
                        //添加过该好友则删除对应的服务器好友请求并跳过插入本地数据库步骤
                        myserviceconn.binder.deleteFriendRequest(username,friend.getUsername());
                        continue;
                    }
                    //先下载头像,后续步骤在handler中
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



    //监听所有ViewModel，用来更新界面
    public void ObserveAllViewModel(){
//            messagesViewModel.getAllMessageLive().observe(this, new Observer<List<Message>>() {
//                @Override
//                public void onChanged(List<Message> messages) {
//
//                }
//            });

        //监听新消息修改 消息 红点的数据
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

        //监听新消息修改 好友请求 红点的数据
        friendRequestsViewModel.getNewFriendRequestLive().observe(this, new Observer<List<FriendRequest>>() {
            @Override
            public void onChanged(List<FriendRequest> friendRequests) {
                Log.d("重点排查", "getValue: " +friendRequestsViewModel.getNewFriendRequestLive()+"    "+friendRequestsViewModel.getNewFriendRequestLive().getValue());

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

        //监听朋友圈消息修改  发现 红点的显示效果

    }

    //下载头像
    private class DownloadHeadPicTask extends AsyncTask<Friend,Void,Void>{
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        private String headpicPath = null;
        @Override
        protected Void doInBackground(Friend... friends) {
            Friend friend = friends[0];
            String downloadpath = friend.getHeadPicPath();
            Log.d("重点排查35", "path= "+downloadpath);
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
                    Log.d("新好友的头像下载任务结束", "headPicPath="+headpicPath);
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
                //延时操作，等待图片下载完成
            }
            Bundle bundle = new Bundle();
            bundle.putString("username",friend.getUsername());
            bundle.putString("nickname",friend.getNickname());
            bundle.putString("headPicPath",headpicPath); //这里改成下载好的本地路径
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
                Log.d("HomeActivity", "添加了一名好友 ");
                myserviceconn.binder.deleteFriendRequest(username,bundle.getString("username"));
            } else if (msg.what == -1) {
                Toast.makeText(getApplicationContext(), "网络出现了一些问题", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(myserviceconn);
    }

}
