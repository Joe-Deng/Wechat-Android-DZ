package com.example.wechatproj.Utils;

import org.apache.commons.codec.Encoder;

import java.io.UnsupportedEncodingException;

import it.sauronsoftware.base64.Base64;

// String 转 Base64
public class Base64Encoder {

    public static String encode(String s) {
        byte[] bytes = null;
        try {
            bytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = Base64.encode(s);
        System.out.println("转码: "+result);
        result = result.replace("\n", "").replace(" ","")
                .replace("\t","").replace("\r","");
        return result;
    }
}
