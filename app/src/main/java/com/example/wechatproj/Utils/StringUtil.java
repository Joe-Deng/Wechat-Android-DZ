package com.example.wechatproj.Utils;

import android.view.View;
import android.widget.EditText;

public class StringUtil {
    public static void deleteSpace(EditText editText, int start){
        if (editText.getText().toString().contains(" ")) {
            String[] str = editText.getText().toString().split(" ");
            String str1 = "";
            for (int i = 0; i < str.length; i++) {
                str1 += str[i];
            }
            editText.setText(str1);
            editText.setSelection(start);
        }
    }
}
