package com.example.wechatproj;

import android.content.Intent;
import android.util.Log;

import com.example.wechatproj.Utils.Base64Encoder;

import org.json.JSONArray;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class serviceTest {
    List<Long> testMsg = new ArrayList<>();

    //8.确认收到消息
    public class ReceiveOk extends Thread implements Runnable{
        List<Long> mTimes;

        public ReceiveOk(List<Long> mTimes) {
            this.mTimes = mTimes;
        }

        @Override
        public void run() {
            JSONArray jsonArray = new JSONArray(mTimes);
            String json = jsonArray.toString();
            String base64 = Base64Encoder.encode(json);
            String path = "http://192.168.2.102:8080/WechatServer/ClearMsg?data="+base64;
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode() == 200){
                    inputStream = conn.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("ReceiveOk", "服务器更改消息状态："+result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            super.run();
        }
    }

    @Test
    public void test(){
        testMsg.add(1608644226522l);
        testMsg.add(1608644230406l);
        new ReceiveOk(testMsg).run();
    }
}
