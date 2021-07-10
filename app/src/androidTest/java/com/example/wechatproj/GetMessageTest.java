package com.example.wechatproj;

import android.util.Log;

import com.example.wechatproj.Utils.Base64Decoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetMessageTest {
    private static String SERVE_URL = "http://192.168.2.102:8080/WechatServer/GetMessage";

    public class GetAllMessageTask extends Thread implements Runnable{
        @Override
        public void run() {
            super.run();

            //通过OkHttp建立联系

            final String TAG ="OKHTTP";
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username" ,"laowang")
                    .add("type","all")
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(SERVE_URL)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "获取消息数据失败 - - 网络错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应--GetMessage");
                    String res = response.body().string();
                    Log.d(TAG, "res:"+res);
                    String json = Base64Decoder.decode(res);
                    Log.d(TAG, "result:"+json);
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(json);
                        for(int i = 0; i<jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            System.out.println(jsonObject);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
    @Test
    public void test(){
        new GetAllMessageTask().start();
    }
}
