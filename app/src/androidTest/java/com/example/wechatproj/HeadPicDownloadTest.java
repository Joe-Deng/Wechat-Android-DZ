package com.example.wechatproj;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HeadPicDownloadTest {
    String headPicPath = "D:\\WeChatProj\\app\\src\\androidTest\\java\\com\\example\\wechatproj"+"/files/"+"jjpsa"+".jpg";
    String Purl="http://192.168.2.102:8080/WechatServer/resources/HeadPic/1608397923982.jpg";
    InputStream inputStream = null;
    FileOutputStream fileOutputStream;
    @Test
    public void test(){
        File file = new File(headPicPath);
        try{
            URL url = new URL(Purl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if(conn.getResponseCode() == 200) {
                inputStream = conn.getInputStream();
                fileOutputStream = new FileOutputStream(file);
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer) )!= -1){
                    fileOutputStream.write(buffer,0,len);
                }
                fileOutputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
                try {
                    if (inputStream!=null){
                        inputStream.close();
                    }
                    inputStream.close();
                    if (fileOutputStream!=null){
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
