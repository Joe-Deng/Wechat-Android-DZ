package com.example.wechatproj.startpages;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.ImageUtils;
import com.example.wechatproj.Utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * A simple {@link Fragment} subclass.
 */
public class Login_Fragment extends Fragment {
    EditText username_edit,password_edit;
    Button loginButton,registerButton,forgetButton;
    String url = null;
    String TAG = "LoginTag";
    String username = null;
    String nickname = null;
    String sex = null;
    String country = null;
    String province = null;
    String city = null;
    String HPUrl = null;
    String myHeadPicPath = null;
    Boolean ifTrue = false;
    Boolean ifOver = false;
    private static final int RC_EXTERNAL_STORAGE_PERM = 100;

    public Login_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_login_, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        username_edit = getView().findViewById(R.id.username_edit);
        password_edit = getView().findViewById(R.id.password_edit);
        loginButton = getView().findViewById(R.id.login_button);
        registerButton = getView().findViewById(R.id.toRegist_button);
        forgetButton = getView().findViewById(R.id.foget_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username_edit.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"?????????????????????",Toast.LENGTH_LONG).show();
                }else if(password_edit.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_LONG).show();
                }else {
                    try {
//                        LoginTask loginTask = new LoginTask();
//                        loginTask.execute(new URL(url));
                        new OkHttpLoginTask().execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller = Navigation.findNavController(v);
                controller.navigate(R.id.action_login_Fragment_to_register_Fragment);
            }
        });

        forgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller = Navigation.findNavController(v);
                controller.navigate(R.id.action_login_Fragment_to_forget_Fragment);
            }
        });

        //??????????????????
        username_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //????????????
                StringUtil.deleteSpace(username_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //????????????
                StringUtil.deleteSpace(password_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //????????????SDK????????????????????????(android 7.0 --- SDK 24 )???????????????StrictMode ??????Android??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    //??????****
    private class LoginTask extends AsyncTask<URL, Void, String> {
        Handler handler = new MyHandler();

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            Message msg = null;
            String res = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                //????????????????????????????????????
                conn.setConnectTimeout(5000);
                //????????????????????????????????????
                conn.setReadTimeout(5000);
                //??????????????????
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ser-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");

                if (conn.getResponseCode() == 200){
                    //???????????????
                    InputStream in = conn.getInputStream();

                    //???????????????
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                }else {
                    handler.sendEmptyMessage(-1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(-2);
            }finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {//????????????
                    conn.disconnect();
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Bundle bundle = new Bundle();
            bundle.putString("result",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            handler.sendMessage(msg);
        }
    }

    //??????OkHttp?????????????????????
    private class OkHttpLoginTask extends AsyncTask<Void,Void,String>{
            MyHandler handler = new MyHandler();
            String TAG ="OKHTTP";
            String url = MyConstants.Login_URL;
            String res = null;


            @Override
            protected String doInBackground(Void... voids) {
                ifTrue = false;
                ifOver = false;
                postAsynHttp();
                while (!ifOver && HPUrl == null){
                    try {
                        Thread.sleep(1);
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
                Log.d(TAG, "?????????????????????????????????");
                handler.sendEmptyMessage(2);
            }

            public void postAsynHttp() {
                OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000,TimeUnit.MILLISECONDS)
                        .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

                RequestBody formBody = new FormBody.Builder()
                        .add("username" ,username_edit.getText().toString())
                        .add("password",password_edit.getText().toString())
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
                            ifTrue = false;
                            ifOver = true;
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
                            username = jsonObject.getString("username");
                            nickname = jsonObject.getString("nickname");
                            sex = jsonObject.getString("sex");
                            country = jsonObject.getString("country");
                            province = jsonObject.getString("province");
                            city = jsonObject.getString("city");
                            HPUrl = jsonObject.getString("hPUrl");
                            ifTrue = true;
                            ifOver = true;
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
    private class DownloadHeadPic extends AsyncTask<Void,Void,String>{
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            MyHandler handler = new MyHandler();
            File file = null;
            String path = null;
            @Override
            protected String doInBackground(Void... voids) {

                try {
                        URL url = new URL(HPUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestMethod("GET");
                        if(conn.getResponseCode() == 200){
                            inputStream = conn.getInputStream();
                            file = new File(getContext().getFilesDir().getAbsolutePath(),"headPic.jpg");
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
                myHeadPicPath = s;
                //??????????????????
                handler.sendEmptyMessage(1);
            }
        }



    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            if(msg.what ==2){
                //????????????
                if(ifTrue){
                    new DownloadHeadPic().execute();
                }
            }else if(msg.what == 1){
                        Toast.makeText(getContext(),"????????????????????????"+username,Toast.LENGTH_LONG).show();
//                        new GetInfoTask
                    //??????????????????
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("my_data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("username",username);
                    edit.putString("nickname",nickname);
                    edit.putString("sex",sex);
                    edit.putString("country",country);
                    edit.putString("province",province);
                    edit.putString("city",city);
                    edit.putString("headPicPath",myHeadPicPath);
                    if(edit.commit()){
                        Log.d(TAG, "???????????????????????????");
                    }else {
                        Log.d(TAG, "????????????????????????");
                    }
                    //????????????
                    Intent intent = new Intent(getActivity(),HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);   //??????Activity ???????????????????????????
                    startActivity(intent);
            }else if(msg.what == -2) {
            Toast.makeText(getContext(),"????????????????????????,???????????????",Toast.LENGTH_LONG).show();

            }else if(msg.what == -1){
            Toast.makeText(getContext(),"????????????,???????????????:404",Toast.LENGTH_LONG).show();
            }else if(msg.what == -3){
                Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_LONG).show();
            }
        }
    }

}
