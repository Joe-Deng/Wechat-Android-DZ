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

        final String[] items = new String[]{"本地相册","拍照"};
        headPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(AlterInfoActivity.this)
                        .setTitle("选择图片")
                        .setIcon(R.drawable.ic_camera_alt_black_24dp)
                        .setItems(items, new DialogInterface.OnClickListener() {//添加列表
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                                    //Pick 的方式可以在真机上运行，但在模拟器不行
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
                                    //拍照实现
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        currentImageFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
                                    }
                                    // 配置系统相机跳转 Intent
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    // 如果系统版本大于 Android 7.0
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        cramuri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileProvider", currentImageFile);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );
                                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                                    } else {    // 低于7.0 则
                                        cramuri = Uri.fromFile(currentImageFile);
                                    }
                                    // 以上配置好了File 映射 |*Uri | ，下面把 Uri 作为Extra 存入 intent的 然后跳转至相机，会自动执行拍照，并将文件存入Uri 映射的文件 ，然后跳转回来
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
                //本地
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nickname", nickname);
                editor.commit();
                //服务器
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
                //本地
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("country", country);
                editor.putString("province", province);
                editor.putString("city", city);
                editor.commit();
                //服务器
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

    //重写跳转周期 处理跳转请求（！！原本是protected方法）
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case TAKE_PHOTP:
                if (resultCode == RESULT_OK) {
                    //相机回调
                    Log.d(TAG, "相机回调，data:" + data);
                    // 剪切图片
                    startPhotoZoom(cramuri);
                }
                break;
            case chose_puhoto:
                if (resultCode == RESULT_OK) {
                    //相册回调
                    Uri uri = data.getData();
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        startPhotoZoom(uri);
                    } else {    //部分机型适配不了Crop，只能直接设置为图片
                        headPic.setImageURI(uri);
                        String path = UriPathEncoder.getPath(getApplicationContext(), uri);
                        //保存到本地sharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("headPicPath", path);
                        editor.commit();
                        headPicPath = path;
                        Log.d(TAG, "头像修改并保存成功！！！！");
                        new UploadTask().execute(path);
                    }

                }
                break;

            case ZOOM_OK:
                Log.d(TAG, "剪裁完成");
                if (resultCode == RESULT_OK) {
                    //裁剪回调
//                    Uri uri = data.getData();
//                    Log.d(TAG, "Uri: "+uri.getPath());
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//                    Drawable drawable = new BitmapDrawable(bitmap);
//                    mHeadpicImgbutton.setImageDrawable(drawable);
                    Uri uri = Uri.fromFile(file2);
                    Log.d(TAG, "flie2 --- uri :" + uri);
                    headPic.setImageURI(uri);
                    //这个路径很重要，每次打开应用自动访问
                    String path = ImageUtils.getRealPathFromUri(getApplicationContext(), uri);
                    //保存到本地sharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("headPicPath", path);
                    editor.commit();
                    headPicPath = path;
                    Log.d(TAG, "头像修改并保存成功！！！！");
                    new UploadTask().execute(path);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 在Environment.DIRECTORY_PICTURES 文件夹下 创建一个 根据时间戳 命名的图片 ， 用于保存裁剪图片
            file2 = new File(AlterInfoActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
        }
        Log.d(TAG, "开始剪裁");

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 临时获取Uri 的读写能力（通过Uri 控制文件）
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 500);
//        intent.putExtra("outputY", 500);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));  //把剪裁好的图片作为文件保存
        intent.putExtra("return-data", false); //是否在Intent中返回数据
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别 --- 部分系统自带裁剪自动检测到人脸，并以人脸作中心裁剪 --- 个人猜测
//        intent.putExtra("return-data", true);
        Log.d(TAG, "图片裁剪完成！");
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
                        Log.d("AlterInfoTask", "服务器更改个人信息状态：" + result.toString());
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

    //上传图片
    private class UploadTask extends AsyncTask<String,Void,String>{
        MyHandler handler = new MyHandler();
        String url = MyConstants.ImageUpload_URL;
        @Override
        protected String doInBackground(String... strings) {
            //使用上传工具上传文件
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
