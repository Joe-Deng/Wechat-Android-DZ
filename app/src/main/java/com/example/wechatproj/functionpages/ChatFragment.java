package com.example.wechatproj.functionpages;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatproj.Adapters.MessageAdapter;
import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Database.Repository.FriendRepository;
import com.example.wechatproj.Database.ViewModel.MessagesViewModel;
import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MainActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.Base64Encoder;
import com.example.wechatproj.Utils.UploadUtil;
import com.example.wechatproj.Utils.UriPathEncoder;
import com.example.wechatproj.startpages.Login_Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    String TAG = "????????????ChatFragment";
    TextView titleText;
    ImageButton backButton,moreButton,voiceButton,emojiButton,addButton;
    Button sendButton,mask;
    EditText editText;
    RecyclerView recyclerView;
    Bundle bundle;
    String username,nickname,headPicPath;
    SharedPreferences sharedPreferences;
    String myUsername,myHeadPicPath;
    MessagesViewModel messagesViewModel;
    List<Message> messages = null;
    MessageAdapter adapter = null;
    String path =null;
    public static final int chose_puhoto=2;
    File file2;
    Long nowTime = 0l;
    private String uploadResult = null;
    Boolean isFromHome = false;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        messagesViewModel = ViewModelProviders.of(this).get(MessagesViewModel.class);
        getMyData();        //??????????????????
        getDataFromArgument();      //??????????????????
        messagesViewModel.setMessageRepository(username);   //???????????????????????????DAO???????????????????????????????????????
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.isFromHome = ((ChatActivity)activity).getIfFromHome();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();     //???????????????


        getMessages();      //??????????????????adapter

        messagesViewModel.updateReadMessage(username);   //??????ViewModel????????????????????????????????????????????????????????????????????????IF_Readed??????

       editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
           @Override
           public void onFocusChange(View v, boolean hasFocus) {
               if(hasFocus){
                   addButton.setVisibility(View.INVISIBLE);
                   emojiButton.setVisibility(View.INVISIBLE);
                   sendButton.setVisibility(View.VISIBLE);
                   mask.setVisibility(View.VISIBLE);
               }else {
                   addButton.setVisibility(View.VISIBLE);
                   emojiButton.setVisibility(View.VISIBLE);
                   sendButton.setVisibility(View.INVISIBLE);
                   mask.setVisibility(View.INVISIBLE);
               }
           }
       });

       mask.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG, "RecycleView CLike!");
               //????????????????????????
               editText.clearFocus();
           }
       });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1.??????????????????????????????
                long time = System.currentTimeMillis();
                Log.d(TAG, "??????time???"+time);
                String text = editText.getText().toString();
                if(text.isEmpty() || text.trim().isEmpty()){
                    return;
                }
                Message message = new Message(System.currentTimeMillis(),username,myUsername,0,text,false,true);
                messagesViewModel.insertMessages(message);
                Log.d(TAG, "????????????????????????-oldTime="+message.getM_Time()+"  content:"+message.getM_Content());
                //2.???????????????????????????
                editText.setText("");
                //3.??????????????????
                RecyclerView recyclerView=(RecyclerView)getView().findViewById(R.id.recyclerView);
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                //4.????????????????????????
                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????bug?????????4????????????
                Message message1 = messagesViewModel.getMessage(message.getM_Time());
                Log.d(TAG, "?????????????????????????????????message_Time"+message.getM_Content());
                new SendMessageTask().execute(message);
            }
        });

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                if (android.os.Build.VERSION.SDK_INT >= 28){
                    intent.setAction(Intent.ACTION_PICK);
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                }
                Log.d("????????????40", "SDK:"+android.os.Build.VERSION.SDK_INT);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                Log.d("????????????40", "intent:"+intent);
                file2 = new File(getActivity().getFilesDir().getAbsolutePath(),System.currentTimeMillis()+".jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));
                startActivityForResult(intent,chose_puhoto);
            }
        });

//        moreButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String apath = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3718511018,2983387049&fm=26&gp=0.jpg";
//                new DownloadHeadPic().execute(apath);
//                while (true){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//                messagesViewModel.insertMessages(new Message(System.currentTimeMillis(),myUsername,username,2,path,true,true));
////                messagesViewModel.insertMessages(new Message(System.currentTimeMillis(),myUsername,username,2,path,true,true));
////                Message msg = new Message(System.currentTimeMillis(),myUsername,username,0,"?????????",true,true);
////                messagesViewModel.insertMessages(msg);
////                RecyclerView recyclerView=(RecyclerView)getView().findViewById(R.id.recyclerView);
////                recyclerView.scrollToPosition(adapter.getItemCount()-1);
////
////                Log.d(TAG, "getScrollY:"+getView().getScrollY()+","+getView().getHeight()+","+getView().getScaleY());
//            }
//        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFromHome){
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }else {
                    getFragmentManager().popBackStack();
                }

            }
        });
    }

    private void init(){
        titleText = getView().findViewById(R.id.titleText);
        backButton = getView().findViewById(R.id.backButton);
        moreButton = getView().findViewById(R.id.moreButton);
        voiceButton = getView().findViewById(R.id.voiceButton);
        emojiButton = getView().findViewById(R.id.emojiButton);
        addButton = getView().findViewById(R.id.addButton);
        sendButton = getView().findViewById(R.id.sendButton);
        editText = getView().findViewById(R.id.editText);
        recyclerView = getView().findViewById(R.id.recyclerView);
        mask = getView().findViewById(R.id.mask);

        titleText.setText(nickname);

        //????????????SDK????????????????????????(android 7.0 --- SDK 24 )???????????????StrictMode ??????Android??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    private void getDataFromArgument(){
        bundle = getArguments();
        username = bundle.getString("username");
        nickname = bundle.getString("nickname");
//        sex = bundle.getString("sex");
//        country = bundle.getString("country");
//        province = bundle.getString("province");
//        city = bundle.getString("city");
        headPicPath = bundle.getString("headPicPath");
    }

    private void getMyData(){
        sharedPreferences = getContext().getSharedPreferences("my_data", Context.MODE_PRIVATE);
        myUsername = sharedPreferences.getString("username","");
        myHeadPicPath = sharedPreferences.getString("headPicPath","");
    }

    private void getMessages(){

        if(messagesViewModel.getAllMessageLive()!=null){
            messages = messagesViewModel.getAllMessageLive().getValue();
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(myUsername,username,myHeadPicPath,headPicPath);   //?????????Adapter???????????????????????????????????????
        recyclerView.setAdapter(adapter);
        messagesViewModel.getAllMessageLive().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                adapter.setAllMessages(messages);
                adapter.notifyDataSetChanged();
            }
        });
        messagesViewModel.getFriendNewMessages(username).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case chose_puhoto:
                if (resultCode == RESULT_OK) {
                    //????????????
                        Uri uri = data.getData();
                        String path = UriPathEncoder.getPath(getContext(), uri);
                        Log.d("????????????40", "uri"+uri);
                        Log.d("????????????40", "path"+path);
                        nowTime = System.currentTimeMillis();
                        Message imgMessage = new Message(nowTime,username,myUsername,2,path,false,true);
                        messagesViewModel.insertMessages(imgMessage);
                        new UploadTask().execute(path);
                }
                break;

            default:
                break;
        }
    }



    //2.????????????????????????
    //??????OkHttp???????????????????????????SendMsg
    private class SendMessageTask extends AsyncTask<Message,Void,String> {
        MyHandler handler = new MyHandler();
        String TAG ="OKHTTP";
        String url = MyConstants.SendMsg_URL;
        String res = null;
        Long oldTime = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//????????????GET?????????????????????
                .build();

        @Override
        protected String doInBackground(Message... messages) {
            oldTime = messages[0].getM_Time();
            postAsynHttp(messages[0]);
            while (res == null){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // ???????????????????????????????????????   ???????????????HPUrl ?????????????????????????????????
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "??????????????????");
            super.onPostExecute(s);
        }

        public void postAsynHttp(final Message message) {
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            String base64Content = Base64Encoder.encode(message.getM_Content());

            RequestBody formBody = new FormBody.Builder()
                    .add("M_Time" ,String.valueOf(message.getM_Time())) //long
                    .add("RID",message.getRID())
                    .add("SID",message.getSID())
                    .add("M_Type",String.valueOf(message.getM_Type()))  //int
                    .add("M_Content",base64Content)
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            Log.d(TAG, "???????????????????????????");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "Failure");
                    Log.d(TAG, "??????????????????--????????????");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "????????????");
                    res = response.body().string();
                    Log.d(TAG, "response.body:  "+res);
                    if(res.equals("fail")){
                        Log.d(TAG, "??????????????????--???????????????");
                        return;
                    }else if(res.equals("success")){
                        Log.d(TAG, "????????????????????????");
                        return;
                    }
//                    String jsonstr = new String(Base64.decode(res.getBytes(), Base64.URL_SAFE),"UTF-8");
                    String jsonstr = Base64Decoder.decode(res);
                    Log.d(TAG, "JsonStr: "+jsonstr);
                    //?????????:????????????????????????????????????
                    try {
                        JSONObject jsonObject = new JSONObject(jsonstr);
//                            JSONArray jsonArray = new JSONArray(jsonstr);
//                            JSONObject jsonObject = jsonArray.getJSONObject(0);
//                        Log.d(TAG, "???????????????"+jsonObject.getString("oldTime")+"  ???????????????"+jsonObject.getString("newTime"));
                        //?????????????????????json???
//                        String oldTime = jsonObject.getString("oldTime");
//                        String newTime = jsonObject.getString("newTime");
                        Long M_Time = jsonObject.getLong("m_Time");
                        String RID = jsonObject.getString("rID");
                        String SID = jsonObject.getString("sID");
                        int M_Type = jsonObject.getInt("m_Type");
                        String M_Content = jsonObject.getString("m_Content");
                        Bundle bundle = new Bundle();
                        bundle.putLong("Old_Time",oldTime);
                        bundle.putLong("New_Time",M_Time);
                        bundle.putString("RID",RID);
                        bundle.putString("SID",SID);
                        bundle.putInt("M_Type",M_Type);
                        bundle.putString("M_Content",M_Content);
                        bundle.putBoolean("IF_Readed",message.getIF_Readed());
                        android.os.Message msg = new android.os.Message();
                        msg.what = 2;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                JsonObject data = new JsonParser().parse(jsonstr).getAsJsonObject();
//                Gson gson = new Gson();
//                ADImage adImage =  gson.fromJson(jsonstr,ADImage.class);
//                ADUrl = adImage.getRes().getPic();
//                Log.i(TAG, "ADUrl???" + ADUrl);
                }

            });
        }

    }

    //6.???????????????????????????CleanMsg
    private class CleanMessageTask extends AsyncTask<List<Message>,Void,Void>{
        long M_Time;
        String TAG ="OKHTTP";
        String url = MyConstants.CleanMsg_URL;
        String res = null;
        @Override
        protected Void doInBackground(List<Message>... lists) {
            List<Message> messages = lists[0];
            JSONArray jsonArray = new JSONArray(messages);
            String json = jsonArray.toString();
            String data = Base64Encoder.encode(json);
            PostAsync(data);
            return null;
        }

        private void PostAsync(String data){
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("data" ,data) //base64
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "?????????????????????????????????--????????????");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "????????????");
                    res = response.body().string();
                    Log.d(TAG, "onResponse: "+res);
                }
            });
        }
    }

    //6.?????????????????????????????????ClearMsg
    private class SendMessageOKTask extends AsyncTask<Long,Void,Void>{
        long M_Time;
        String TAG ="OKHTTP";
        String url = MyConstants.ClearMsg_URL;
        String res = null;
        @Override
        protected Void doInBackground(Long... longs) {
            Long M_Time = longs[0];
            PostAsync(M_Time);
            return null;
        }

        private void PostAsync(Long M_Time){
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("M_Time" ,String.valueOf(M_Time)) //base64
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "?????????????????????????????????--????????????");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "????????????");
                    res = response.body().string();
                    Log.d(TAG, "onResponse: "+res);
                }
            });
        }
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg){
            if(msg.what ==2){
                Log.d(TAG, "???????????????????????????");
                //?????????????????????
                Bundle bundle = msg.getData();
                long oldTime = Long.valueOf(bundle.getLong("Old_Time",0));
                Log.d(TAG, "handleMessage: ");
                long newTime = Long.valueOf(bundle.getLong("New_Time",0));
                //5.?????????????????????????????????
                Message oldMessage = messagesViewModel.getMessage(oldTime);
                Log.d("????????????45", "oldTime= "+oldTime+", newTime="+newTime+", oldMessage:"+oldMessage);
                Log.d(TAG, "oldTime:"+oldTime+" " +oldMessage.getM_Content()+"   newTime="+newTime);
                messagesViewModel.deleteMessages(oldMessage);
                Log.d(TAG, "oldMessage:"+oldMessage.getM_Content()+oldMessage.getM_Time());
                Message newMessage = new Message(newTime,bundle.getString("RID"),bundle.getString("SID"),bundle.getInt("M_Type"),oldMessage.getM_Content(),true,true);
                messagesViewModel.insertMessages(newMessage);
                Log.d(TAG, "newMessage???" +newMessage.getM_Content());
                Log.d(TAG, "??????????????????????????????");
                new SendMessageOKTask().execute(newTime);        //???????????????
            }else if(msg.what == 3){
                Log.d(TAG, "?????????????????????path???"+uploadResult);
                //??????????????????????????????????????????
                String[] splits = uploadResult.split("/");
                Log.d("????????????47", "splits: "+splits[splits.length - 1]);
                String[] filenameSplits = splits[splits.length - 1].split("\\.");
                Log.d("????????????47", "filenameSplits"+filenameSplits[0]);
                Long realTime = Long.valueOf(filenameSplits[0]);
                Log.d("????????????47", "realTime"+realTime+"  oldTime:"+nowTime);
                Long oldTime = nowTime;
                Message oldMessage = messagesViewModel.getMessage(oldTime);
                Log.d("????????????45", "oldMessage:"+oldMessage);
                //???????????????????????????
                Message newMessage = new Message(realTime,username,myUsername,2,oldMessage.getM_Content(),true,true);
                messagesViewModel.deleteMessages(oldMessage);
                messagesViewModel.insertMessages(newMessage);
                Log.d(TAG, "newMessage???" +newMessage.getM_Content());
                //?????????????????????
                Message message = new Message(realTime,username,myUsername,2,uploadResult,true,true);
                new SendMessageTask().execute(message);
                Log.d(TAG, "??????????????????????????????");
            }
        }
    }

    public class DownloadHeadPic extends AsyncTask<String,Void,Void>{
        String TAG ="DownloadTask";
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;
        @Override
        protected Void doInBackground(String...strings) {
            //1.???????????????2.filePath , 3.username
            try {
                String apath = strings[0];
                URL url = new URL(apath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    file = new File(getContext().getFilesDir().getAbsolutePath(),System.currentTimeMillis()+".jpg");
                    fileOutputStream = new FileOutputStream(file);
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer) )!= -1){
                        fileOutputStream.write(buffer,0,len);
                    }
                    fileOutputStream.flush();
                    //??????????????????????????????
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
            return null;
        }
    }

    //????????????
    private class UploadTask extends AsyncTask<String,Void,String>{
        MyHandler handler = new MyHandler();
        String url = MyConstants.ImageUpload_URL;
        @Override
        protected String doInBackground(String... strings) {
            //??????????????????????????????
            String filePath = strings[0];
            String res = UploadUtil.uploadFile(new File(filePath), url);
            Log.d(TAG, "UploadTask-doInbackground-res:"+res);
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uploadResult = s;
            handler.sendEmptyMessage(3);
        }
    }
}
