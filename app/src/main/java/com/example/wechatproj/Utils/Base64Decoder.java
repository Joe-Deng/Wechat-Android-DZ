package com.example.wechatproj.Utils;


import java.io.UnsupportedEncodingException;

import it.sauronsoftware.base64.Base64;


public class Base64Decoder {
    public static String decode(String s){
        String str = null;
        try {
             str = new String(Base64.decode(s.getBytes()),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
