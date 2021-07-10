package com.example.wechatproj.startpages;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class Register_Fragment extends Fragment {
    EditText username_edit,password_edit,resume_edit,phone_edit,email_edit;
    Button registerButton;
    RadioButton allowButton;
    TextView protocol;
    TextView username_label,password_label,resume_label,phone_label,email_label;
    ImageView username_ok,password_ok,resume_ok,phone_ok,email_ok;
    Boolean usernameOK,passwordOK,resumeOK,phoneOK,emailOK,protocolOK;
    ProgressBar loadingBar;
    String url = "";
    String TAG ="register TAG";
    private FragmentManager fragmentManager;

    public Register_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        buttonObserver();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开始发送网络异步请求任务
                url = MyConstants.Register_URL+ "?username="+username_edit.getText().toString()+"&password="+password_edit.getText().toString()+"&phone="+phone_edit.getText().toString()+"&email="+email_edit.getText().toString();
                try {
                    loadingBar.setVisibility(View.VISIBLE);
                    new RegistTask().execute(new URL(url));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }finally {
                    url = null;
                }
            }
        });



    }

    //输入检查器（核心）——通过就让注册按钮高亮
    private void buttonObserver(){
        username_edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(username_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!usernameCheak(username_edit.getText().toString())){
                    username_ok.setVisibility(View.INVISIBLE);
                    username_label.setVisibility(View.VISIBLE);
                    usernameOK=false;
                    registDisable();
                }else {
                    username_label.setVisibility(View.INVISIBLE);
                    username_ok.setVisibility(View.VISIBLE);
                    usernameOK=true;
                    registAble();
                }
            }
        });

        password_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                StringUtil.deleteSpace(password_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!passwordCheak(password_edit.getText().toString())){
                    password_ok.setVisibility(View.INVISIBLE);
                    password_label.setVisibility(View.VISIBLE);
                    passwordOK=false;
                    registDisable();
                }else {
                    password_label.setVisibility(View.INVISIBLE);
                    password_ok.setVisibility(View.VISIBLE);
                    passwordOK=true;
                    registAble();
                }

                if(!password_edit.getText().toString().equals(resume_edit.getText().toString())){
                    resume_ok.setVisibility(View.INVISIBLE);
                    resume_label.setVisibility(View.VISIBLE);
                    resumeOK = false;
                }else {
                    resume_ok.setVisibility(View.VISIBLE);
                    resume_label.setVisibility(View.INVISIBLE);
                    resumeOK = true;
                }
            }
        });

        resume_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                StringUtil.deleteSpace(resume_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(password_edit.getText().toString().equals(resume_edit.getText().toString())){
                    resume_label.setVisibility(View.INVISIBLE);
                    resume_ok.setVisibility(View.VISIBLE);
                    resumeOK = true;
                    registAble();
                }else {
                    resume_label.setVisibility(View.VISIBLE);
                    resume_ok.setVisibility(View.INVISIBLE);
                    resumeOK = false;
                    registDisable();
                }
            }
        });

        phone_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(phone_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() ==11){
                    phone_ok.setVisibility(View.VISIBLE);
                    phone_label.setVisibility(View.INVISIBLE);
                    phoneOK = true;
                    registAble();
                }else {
                    phone_ok.setVisibility(View.INVISIBLE);
                    phone_label.setVisibility(View.VISIBLE);
                    phoneOK = false;
                    registDisable();
                }
            }
        });

        email_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(email_edit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(emailCheak(s.toString())){
                    email_label.setVisibility(View.INVISIBLE);
                    email_ok.setVisibility(View.VISIBLE);
                    emailOK = true;
                    registAble();
                }else {
                    email_label.setVisibility(View.VISIBLE);
                    email_ok.setVisibility(View.INVISIBLE);
                    emailOK = false;
                    registDisable();
                }
            }
        });

        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(protocolOK){
                    allowButton.setChecked(false);
                    protocolOK = false;
                    registDisable();
                }else {
                    allowButton.setChecked(true);
                    protocolOK = true;
                    registAble();
                }
            }
        });

    }

    //界面初始化
    private void init(){
        registerButton = getView().findViewById(R.id.regist_button);
        username_edit = getView().findViewById(R.id.username_edit);
        password_edit = getView().findViewById(R.id.password_edit);
        resume_edit = getView().findViewById(R.id.resume_edit);
        phone_edit = getView().findViewById(R.id.phone_edit);
        email_edit = getView().findViewById(R.id.email_edit);
        username_label = getView().findViewById(R.id.username_label);
        password_label = getView().findViewById(R.id.password_label);
        resume_label = getView().findViewById(R.id.resume_label);
        phone_label = getView().findViewById(R.id.phone_label);
        email_label = getView().findViewById(R.id.email_label);
        username_ok = getView().findViewById(R.id.username_ok);
        password_ok = getView().findViewById(R.id.password_ok);
        resume_ok = getView().findViewById(R.id.resume_ok);
        phone_ok = getView().findViewById(R.id.phone_ok);
        email_ok = getView().findViewById(R.id.email_ok);
        usernameOK = false;
        passwordOK = false;
        resumeOK = false;
        phoneOK = false;
        emailOK = false;
        protocolOK = false;
        allowButton = getView().findViewById(R.id.allow_button);
        protocol = getView().findViewById(R.id.protocal);
        loadingBar = getView().findViewById(R.id.loadingBar);
    }

    //三个输入检查规则
    private Boolean usernameCheak(String s){
        String pattern = "^[a-zA-Z0-9]{4,24}$";
        return Pattern.matches(pattern,s);
    }

    private Boolean passwordCheak(String s){
        String pattern = "^[a-za-z0-9]{6,24}$";
        return Pattern.matches(pattern,s);
    }

    private Boolean emailCheak(String s){
        //邮箱正则
        String pattern = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return Pattern.matches(pattern,s);
    }

    //两个控制注册按钮是否能被点击
    private void registAble(){
        if(usernameOK && protocolOK && resumeOK && phoneOK && emailOK && protocolOK){
            registerButton.setClickable(true);
            registerButton.setBackgroundResource(R.color.myGreen);
        }
    }

    private void registDisable(){
        registerButton.setClickable(false);
        registerButton.setBackgroundResource(R.color.myGray);
    }

    //只负责发送URL和接收结果
    private class RegistTask extends AsyncTask<URL, Void, String> {
        Handler handler = new MyHandler();

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            String res = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                //设置连接超时时间（毫秒）
                conn.setConnectTimeout(5000);
                //设置读取超时时间（毫秒）
                conn.setReadTimeout(5000);
                //设置请求方法
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ser-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");

                if (conn.getResponseCode() == 200) {
                    Log.d(TAG, "发送成功 ");
                    //返回输入流
                    InputStream in = conn.getInputStream();

                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                    Log.d(TAG, "接收到结果: "+res);
                }else {
                    handler.sendEmptyMessage(-1);
                    return null;
                }
            } catch (IOException e) {
                handler.sendEmptyMessage(-2);
                e.printStackTrace();
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {//关闭连接
                    conn.disconnect();
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "数据准备发送。。。");
            super.onPostExecute(s);
            if(s == null || s.isEmpty()){
                Log.d(TAG, "发送失败，请检测网络");
            }else {
                Bundle bundle = new Bundle();
                bundle.putString("result",s);
                Message msg = new Message();
                msg.setData(bundle);
                msg.what = 1;
                Log.d(TAG, "准备完成，正在处理结果");
                handler.sendMessage(msg);
            }
        }
    }

    //处理网络返回结果（报错、页面跳转）
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "转圈圈。。。");
            loadingBar.setVisibility(View.INVISIBLE);
            Log.d(TAG, "圈圈转完了");
            if(msg.what == 1){
                String result = msg.getData().getString("result");

                if(result.equals("success")){
                    Log.d("Register_Fragement", "注册结果: "+result);
                    Toast.makeText(getContext(),"注册成功",Toast.LENGTH_LONG).show();
                    //__________________跳转至填写资料界面——————————
                    Bundle argBundle = new Bundle();
                    argBundle.putString("username",username_edit.getText().toString());
                    NavController controller = Navigation.findNavController(getView());
                    controller.navigate(R.id.action_register_Fragment_to_info_Fragment,argBundle);
                }else {
                    Toast.makeText(getContext(),"用户名已存在，请重新注册",Toast.LENGTH_LONG).show();
                }
            }else if(msg.what == -2) {
                Toast.makeText(getContext(),"注册失败,请检查网络",Toast.LENGTH_LONG).show();
            }else if(msg.what == -1){
                Toast.makeText(getContext(),"404 NO FOUND...",Toast.LENGTH_LONG).show();
            }
        }
    }


}



