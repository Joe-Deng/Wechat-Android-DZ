package com.example.wechatproj.mainpages.ui.me;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Encoder;
import com.example.wechatproj.Utils.ImageUtils;
import com.example.wechatproj.Utils.UploadUtil;
import com.example.wechatproj.Utils.UriPathEncoder;
import com.example.wechatproj.mainpages.ui.found.FriendCircleActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlterInfoActivity extends AppCompatActivity {
    Button headPicButton,nicknameButton,locationButton,moreButton;
    ImageButton backButton;
    ImageView headPic;
    TextView nicknameView;
    TextView location;
    String username,nickname,sex,country,province,city,headPicPath;
    EditText nicknameEdit,countryEdit,provinceEdit,cityEdit;
    ImageView nicknameOk,locationOk;
    SharedPreferences sharedPreferences;
    public static final int TAKE_PHOTP=1;
    public static final int chose_puhoto=2;
    private static final int ZOOM_OK = 3;
    private File currentImageFile = null;
    private File file2 = null;
    private Uri cramuri;
    String TAG = "AlterInfo";
    private static String AlterInfo_URL = MyConstants.AlterInfo_URL;
    private String uploadResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alter_info);

        Init();

        final String[] items = new String[]{"????????????","??????"};
        headPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(AlterInfoActivity.this)
                        .setTitle("????????????")
                        .setIcon(R.drawable.ic_camera_alt_black_24dp)
                        .setItems(items, new DialogInterface.OnClickListener() {//????????????
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                                    //Pick ?????????????????????????????????????????????????????????
                                    if (android.os.Build.VERSION.SDK_INT >= 28){
                                        intent.setAction(Intent.ACTION_PICK);
                                    } else {
                                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                    }
                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    file2 = new File(getApplicationContext().getFilesDir().getAbsolutePath(),System.currentTimeMillis()+".jpg");
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));
                                    startActivityForResult(intent,chose_puhoto);
//                                    openAlbum();
                                }else if(i == 1){
                                    //????????????
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        currentImageFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
                                    }
                                    // ???????????????????????? Intent
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    // ???????????????????????? Android 7.0
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        cramuri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileProvider", currentImageFile);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );
                                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                                    } else {    // ??????7.0 ???
                                        cramuri = Uri.fromFile(currentImageFile);
                                    }
                                    // ??????????????????File ?????? |*Uri | ???????????? Uri ??????Extra ?????? intent??? ??????????????????????????????????????????????????????????????????Uri ??????????????? ?????????????????????
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cramuri);
                                    startActivityForResult(intent,TAKE_PHOTP);
                                }
                            }
                        }).create();
                alertDialog.show();
            }
        });

        nicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nicknameButton.setVisibility(View.INVISIBLE);
                nicknameView.setVisibility(View.INVISIBLE);
                nicknameEdit.setVisibility(View.VISIBLE);
                nicknameEdit.setText(nickname);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationButton.setVisibility(View.INVISIBLE);
                location.setVisibility(View.INVISIBLE);
                countryEdit.setVisibility(View.VISIBLE);
                provinceEdit.setVisibility(View.VISIBLE);
                cityEdit.setVisibility(View.VISIBLE);
                countryEdit.setText(country);
                provinceEdit.setText(province);
                cityEdit.setText(city);
            }
        });

        nicknameOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname =nicknameEdit.getText().toString();
                //??????
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nickname", nickname);
                editor.commit();
                //?????????
                new AlterInfoTask().execute("nickname",nickname);
                nicknameView.setText(nickname);
                nicknameEdit.setVisibility(View.INVISIBLE);
                nicknameView.setVisibility(View.VISIBLE);
                nicknameButton.setVisibility(View.VISIBLE);
            }
        });

        locationOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                country = countryEdit.getText().toString();
                province = provinceEdit.getText().toString();
                city = cityEdit.getText().toString();
                //??????
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("country", country);
                editor.putString("province", province);
                editor.putString("city", city);
                editor.commit();
                //?????????
                new AlterInfoTask().execute("country",country);
                new AlterInfoTask().execute("province",province);
                new AlterInfoTask().execute("city",city);
                location.setText(country+"  "+province+"  "+city);
                countryEdit.setVisibility(View.INVISIBLE);
                provinceEdit.setVisibility(View.INVISIBLE);
                cityEdit.setVisibility(View.INVISIBLE);
                location.setVisibility(View.VISIBLE);
                locationButton.setVisibility(View.VISIBLE);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMe();
            }
        });

    }

    private void Init(){
       headPicButton  = findViewById(R.id.headpicButton);
       nicknameButton  = findViewById(R.id.nicknameButton);
        locationButton = findViewById(R.id.locationButton);
        moreButton = findViewById(R.id.moreButton);
        backButton = findViewById(R.id.backButton);
        headPic = findViewById(R.id.headpic);
        nicknameView = findViewById(R.id.nicknameView);
        location = findViewById(R.id.location);
        nicknameEdit = findViewById(R.id.nicknameEdit);
        countryEdit = findViewById(R.id.countryEdit);
        provinceEdit = findViewById(R.id.provinceEdit);
        cityEdit = findViewById(R.id.cityEdit);
        nicknameOk = findViewById(R.id.nicknameOk);
        locationOk = findViewById(R.id.locationOk);

        sharedPreferences = getSharedPreferences("my_data",MODE_PRIVATE);
        username = sharedPreferences.getString("username","");
        nickname = sharedPreferences.getString("nickname","");
        sex = sharedPreferences.getString("sex","");
        country = sharedPreferences.getString("country","");
        province = sharedPreferences.getString("province","");
        city = sharedPreferences.getString("city","");
        headPicPath = sharedPreferences.getString("headPicPath","");

        headPic.setImageBitmap(BitmapFactory.decodeFile(headPicPath));
        nicknameView.setText(nickname);
        location.setText(country+"  "+province+"  "+city);
    }

    //?????????????????? ????????????????????????????????????protected?????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case TAKE_PHOTP:
                if (resultCode == RESULT_OK) {
                    //????????????
                    Log.d(TAG, "???????????????data:" + data);
                    // ????????????
                    startPhotoZoom(cramuri);
                }
                break;
            case chose_puhoto:
                if (resultCode == RESULT_OK) {
                    //????????????
                    Uri uri = data.getData();
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        startPhotoZoom(uri);
                    } else {    //????????????????????????Crop??????????????????????????????
                        headPic.setImageURI(uri);
                        String path = UriPathEncoder.getPath(getApplicationContext(), uri);
                        //???????????????sharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("headPicPath", path);
                        editor.commit();
                        headPicPath = path;
                        Log.d(TAG, "???????????????????????????????????????");
                        new UploadTask().execute(path);
                    }

                }
                break;

            case ZOOM_OK:
                Log.d(TAG, "????????????");
                if (resultCode == RESULT_OK) {
                    //????????????
//                    Uri uri = data.getData();
//                    Log.d(TAG, "Uri: "+uri.getPath());
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//                    Drawable drawable = new BitmapDrawable(bitmap);
//                    mHeadpicImgbutton.setImageDrawable(drawable);
                    Uri uri = Uri.fromFile(file2);
                    Log.d(TAG, "flie2 --- uri :" + uri);
                    headPic.setImageURI(uri);
                    //??????????????????????????????????????????????????????
                    String path = ImageUtils.getRealPathFromUri(getApplicationContext(), uri);
                    //???????????????sharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("headPicPath", path);
                    editor.commit();
                    headPicPath = path;
                    Log.d(TAG, "???????????????????????????????????????");
                    new UploadTask().execute(path);
                }
                break;

            default:
                break;
        }
    }

    /**
     * ????????????????????????
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // ???Environment.DIRECTORY_PICTURES ???????????? ???????????? ??????????????? ??????????????? ??? ????????????????????????
            file2 = new File(AlterInfoActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
        }
        Log.d(TAG, "????????????");

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // ????????????Uri ????????????????????????Uri ???????????????
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // ????????????
        intent.putExtra("crop", "true");
        // aspectX aspectY ??????????????????
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY ?????????????????????
//        intent.putExtra("outputX", 500);
//        intent.putExtra("outputY", 500);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));  //???????????????????????????????????????
        intent.putExtra("return-data", false); //?????????Intent???????????????
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// ????????????
        intent.putExtra("noFaceDetection", true);// ?????????????????? --- ??????????????????????????????????????????????????????????????????????????? --- ????????????
//        intent.putExtra("return-data", true);
        Log.d(TAG, "?????????????????????");
        startActivityForResult(intent, ZOOM_OK);
    }

    public class AlterInfoTask extends AsyncTask<String,Void,Void>{
        String key = null;
        String value = null;

        @Override
        protected Void doInBackground(String... strings) {
            key = strings[0];
            value = strings[1];
            InputStream inputStream = null;
            BufferedReader reader = null;
            String base64 = Base64Encoder.encode(value.trim());
            try {
                URL url = new URL(AlterInfo_URL+"?key="+key+"&value="+base64+"&username="+username);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                if (conn.getResponseCode() == 200) {
                        inputStream = conn.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        Log.d("AlterInfoTask", "????????????????????????????????????" + result.toString());
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    //????????????
    private class UploadTask extends AsyncTask<String,Void,String>{
        MyHandler handler = new MyHandler();
        String url = MyConstants.ImageUpload_URL;
        @Override
        protected String doInBackground(String... strings) {
            //??????????????????????????????
            String filePath = strings[0];
            String res = UploadUtil.uploadFile(new File(filePath), url);
            Log.d(TAG, "UploadTask-doInbackground-res:"+res);
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uploadResult = s;
            handler.sendEmptyMessage(1);
        }
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(android.os.Message msg){
            if(msg.what ==1) {
                if(uploadResult!= null && !uploadResult.isEmpty()){
                    new AlterInfoTask().execute("HPUrl",uploadResult);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMe();
    }

    private void backToMe(){
        Intent intent = new Intent(AlterInfoActivity.this, HomeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("ToFragment","alterInfo");
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
