package com.example.wechatproj.Utils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.wechatproj.startpages.Login_Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpGetTools {
    String TAG = "HttpGetTools";
    private URL url = null;

    public HttpGetTools(URL url) {
        this.url = url;
        Log.d(TAG, "url = "+this.url);
    }

    //登录操作
    public void excuteLogin(){

    }

    //注册操作
    public void excuteRegist(){}

    //登录任务


}
