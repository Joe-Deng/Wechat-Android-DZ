package com.example.wechatproj;



import com.example.wechatproj.Utils.Base64Decoder;
import com.example.wechatproj.Utils.Base64Encoder;

import org.junit.Test;

public class Base64Test {

    @Test
    public static void main(String[] args) {
        String TAG = "Test";
        String s = "http://192.168.2.102:8080/WechatServer/Info?Username=张家辉&nickname=西双版纳";
        String encodedStr = Base64Encoder.encode(s);
        System.out.println("转码后："+encodedStr);
        Base64Decoder base64Decoder = new Base64Decoder();
        String decodedStr = base64Decoder.decode(encodedStr);
        System.out.println("解码后："+decodedStr);

    }
}
