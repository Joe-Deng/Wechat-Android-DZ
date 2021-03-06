package com.example.wechatproj.mainpages.ui.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Adapters.FriendAdapter;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.functionpages.AddFriendActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FriendsFragment extends Fragment {
    RecyclerView recyclerView;
    FriendAdapter friendAdapter;
    FriendsViewModel friendsViewModel;
    Button testButton,addFriendButton;
//    String[] friends = {"datou","xiaobai","Jsiij","jsnnsn","jsjbsbbs"};
    String[] friends = {"xiaobai"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendsViewModel =
                ViewModelProviders.of(this).get(FriendsViewModel.class);

        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        testButton = getView().findViewById(R.id.testButton);
        recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Activity activity = getActivity();
        friendAdapter = new FriendAdapter(activity);
        recyclerView.setAdapter(friendAdapter);
        addFriendButton = getView().findViewById(R.id.addFriendButton);
        //??????????????? = =||
//        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
        friendsViewModel.getAllFriendLive().observe(this, new Observer<List<Friend>>() {
            @Override
            public void onChanged(List<Friend> friends) {
                Log.d("????????????20", "friends "+friends);
                friendAdapter.setAllFriends(friends);   //?????????
                friendAdapter.notifyDataSetChanged();   //????????????
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OkHttpLoginTask().execute(friends);
//                Friend friend = new Friend("laopike","?????????","???","??????","???????????????","?????????",getContext().getSharedPreferences("my_data", Context.MODE_PRIVATE).getString("headPicPath",""));
//                friendsViewModel.insertFriends(friend);
                testButton.setVisibility(View.INVISIBLE);
            }
        });
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            if(msg.what ==2){
                //????????????
                Bundle data = msg.getData();
                new DownloadHeadPic().execute(data);
            }else if(msg.what == 1){
                // ?????? ViewModel ???????????????
                Bundle data = msg.getData();
                Friend friend = new Friend(data.getString("username"),
                        data.getString("nickname"),
                        data.getString("sex"),
                        data.getString("country"),
                        data.getString("province"),
                        data.getString("city"),
                        data.getString("headPicPath"));
                friendsViewModel.insertFriends(friend);
            }else if(msg.what == -2) {
                Toast.makeText(getContext(),"???????????????",Toast.LENGTH_LONG).show();

            }else if(msg.what == -1){
                Toast.makeText(getContext(),"???????????????:404",Toast.LENGTH_LONG).show();
            }else if(msg.what == -3){
                Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_LONG).show();
            }
        }
    }

    //??????OkHttp?????????????????????
    private class OkHttpLoginTask extends AsyncTask<String, Void, Void> {
                    MyHandler handler = new MyHandler();
        String TAG ="OKHTTP";
        String url = MyConstants.Friends_URL;
        String res = null;
        Boolean end = false;
        Message msg = null;

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//????????????GET?????????????????????
                .build();

        @Override
        protected Void doInBackground(String... strings) {
            for(int i = 0;i<strings.length;i++){
                postAsynHttp(strings[i]);
                while (!end){
                    // ???????????????????????????????????????   ???????????????HPUrl ?????????????????????????????????
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                end = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            Log.d(TAG, "??????????????????");
            super.onPostExecute(s);

        }

        public void postAsynHttp(String username) {
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username" ,username)
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
                        handler.sendEmptyMessage(-1);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "????????????");
                    res = response.body().string();
                    Log.d(TAG, "response.body:  "+res);
                    if(res.equals("fail")){
                            handler.sendEmptyMessage(-2);
                        return;
                    }
//                    String jsonstr = new String(Base64.decode(res.getBytes(), Base64.URL_SAFE),"UTF-8");
                    String jsonstr = Base64Decoder.decode(res);
                    Log.d(TAG, "JsonStr: "+jsonstr);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonstr);
//                            JSONArray jsonArray = new JSONArray(jsonstr);
//                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                        Log.d(TAG, "?????????"+jsonObject.getString("username")+"  ?????????"+jsonObject.getString("nickname")+"  ?????????"+jsonObject.getString("sex")+"  ?????????"+jsonObject.getString("country")
                                +jsonObject.getString("province")+jsonObject.getString("city")+"  ?????????"+jsonObject.getString("hPUrl"));
                        //?????????????????????json???
//                        String username = jsonObject.getString("username");
//                        String nickname = jsonObject.getString("nickname");
//                        String sex = jsonObject.getString("sex");
//                        String country = jsonObject.getString("country");
//                        String province = jsonObject.getString("province");
//                        String city = jsonObject.getString("city");
//                        String HPUrl = jsonObject.getString("hPUrl");
                        Iterator<String> keys = jsonObject.keys();
                        Bundle bundle = new Bundle();
                        // ???Json ?????? Bundle
                        while (keys.hasNext()){
                            String key = keys.next();
                            Log.d(TAG, "????????????key="+key);
                            bundle.putString(key,jsonObject.getString(key));
                        }
                        Log.d(TAG, "bundle:"+bundle);
                        Message msg = new Message();
                        msg.setData(bundle);
                        Log.d(TAG, "keys:"+keys);
                        Log.d(TAG, "?????????????????????????????????");
                        msg.what = 2;
                        handler.sendMessage(msg);
                        end = true;
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

    //??????????????????
    private class DownloadHeadPic extends AsyncTask<Bundle,Void,String>{
        String TAG ="DownloadTask";
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        MyHandler handler = new MyHandler();
        File file = null;
        String path = null;
        Bundle bundle;
        @Override
        protected String doInBackground(Bundle... bundles) {

            try {
                bundle = bundles[0];
                String username = bundle.getString("username");
                String HPUrl = bundle.getString("hPUrl");
                Log.d(TAG, "HPUrl:"+HPUrl);
                URL url = new URL(HPUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    file = new File(getContext().getFilesDir().getAbsolutePath(),username+".jpg");
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
                }else {
                    handler.sendEmptyMessage(-3);
                }
            } catch (Exception e) {
                handler.sendEmptyMessage(-3);
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
            while (path == null) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //???????????????????????????????????????
            }
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            bundle.putString("headPicPath",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            //??????????????????
            handler.sendMessage(msg);
            bundle = null;
        }
    }
}