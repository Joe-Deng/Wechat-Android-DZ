package com.example.wechatproj.startpages;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechatproj.HomeActivity;
import com.example.wechatproj.MainActivity;
import com.example.wechatproj.MyConstants;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.Base64Encoder;
import com.example.wechatproj.Utils.ImageUtils;
import com.example.wechatproj.Utils.StringUtil;
import com.example.wechatproj.Utils.UploadUtil;
import com.example.wechatproj.Utils.UriPathEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Info_Fragment extends Fragment {

    private RadioGroup mSexGroup;
    private RadioButton mSexButton1;
    private RadioButton mSexButton2;
    private TextView mTextView;
    private Guideline mGuideline;
    private ImageButton mHeadpicImgbutton;
    private EditText mNicknameEdit;
    private EditText mEditCountry;
    private EditText mEditProvince;
    private EditText mEditCity;
    private Guideline mGuideline12;
    private Button mSubmitButton;
    private ProgressBar mProgressBar;
    private ImageView nickname_ok;
    private TextView nickname_label;
    private Boolean headPicOK,nicknameOK,sexOK,countryOK,provinceOK,cityOK;
    public static final int TAKE_PHOTP=1;
    public static final int chose_puhoto=2;
    private static final int ZOOM_OK = 3;
    private static final int RC_EXTERNAL_STORAGE_PERM = 100;
    private String TAG = "Info_TAG";
    private File currentImageFile = null;
    private Uri cramuri;
    private File file2;
    private String bitmappath;
    String savedHeadPicFilePath;    //个人头像缩略图本地路径
    String uploadResult = "";
    private String username;
    private String sex;
    SharedPreferences sharedPreferences;
    String username64;
    String nickname64;
    String sex64;
    String country64;
    String province64;
    String city64;
    String headpic;

    public Info_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        username = getArguments().getString("username");
        Log.d(TAG, "username= "+username);
        //初始化
        initView();
        buttonObserver();
        sharedPreferences = getContext().getSharedPreferences("my_data", Context.MODE_PRIVATE);
    }

    public void initView() {
        mNicknameEdit = getView().findViewById(R.id.nickname_edit);
        mHeadpicImgbutton = getView().findViewById(R.id.headPic_imgButton);
        mSexGroup = getView().findViewById(R.id.sex_group);
        mSexButton1 = getView().findViewById(R.id.sex_button1);
        mSexButton2 = getView().findViewById(R.id.sex_button2);
        mEditCountry = getView().findViewById(R.id.edit_country);
        mEditProvince = getView().findViewById(R.id.edit_province);
        mEditCity = getView().findViewById(R.id.edit_city);
        mSubmitButton = getView().findViewById(R.id.submit_button);
        mProgressBar = getView().findViewById(R.id.progressBar);
        nickname_ok = getView().findViewById(R.id.nickname_ok);
        nickname_label = getView().findViewById(R.id.nickname_label);
        headPicOK = false;
        nicknameOK = false;
        sexOK = false;
        countryOK = true;
        provinceOK = true;
        cityOK = true;
        //权限管理，下面声明
        requiresPermission();

        //如果当前SDK版本大于最低要求(android 7.0 --- SDK 24 )，那么使用StrictMode ——Android性能调优工具
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        final String[] items = new String[]{"本地相册","拍照"};
        mHeadpicImgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("选择上传方式")
                        .setIcon(R.drawable.ic_camera_alt_black_24dp)
                        .setItems(items, new DialogInterface.OnClickListener() {//添加列表
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    //本地相册实现
//                                    if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
////                                    {
////                                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
////                                    }else {
////                                        openAlbum();
////                                    }
                                    //Pick 的方式可以在真机上运行，但在模拟器不行
                                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    startActivityForResult(intent,chose_puhoto);
//                                    openAlbum();
                                }else if(i == 1){
                                    //拍照实现
                                    // 判断是否获取SD卡权限，如果有权限，设置拍照的图片 |*File | 保存格式（jpg）和路径（系统Camera文件夹）
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        currentImageFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
                                    }
                                    // 配置系统相机跳转 Intent
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                     // 如果系统版本大于 Android 7.0
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Log.d(TAG, "大于Android7.0");
                                        cramuri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileProvider", currentImageFile);
                                        Log.d(TAG, "cramuri： "+cramuri);
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

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                new UploadTask().execute(savedHeadPicFilePath);
            }
        });
    }

    //使用EasyPermissions来管理权限
    @AfterPermissionGranted(RC_EXTERNAL_STORAGE_PERM)
    private void requiresPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
        } else {
            EasyPermissions.requestPermissions(this,  getResources().getString(R.string.tips_crema), RC_EXTERNAL_STORAGE_PERM, perms);
        }
    }

    //核心检查器
    private void buttonObserver(){
        mNicknameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(mNicknameEdit,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty() && s.toString().length()<=15){
                    nickname_ok.setVisibility(View.VISIBLE);
                    nickname_label.setVisibility(View.INVISIBLE);
                    nicknameOK = true;
                }else {
                    nickname_ok.setVisibility(View.INVISIBLE);
                    nickname_label.setVisibility(View.VISIBLE);
                    nicknameOK = false;
                }
                Log.d(TAG, "HeadPic_Nickname_sex_country_province_ciry: result: "+headPicOK+nicknameOK+sexOK+countryOK+provinceOK+cityOK);
                submitAble();
            }
        });

        mSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId == mSexButton1.getId()){
                    sex = "男";
                    sexOK = true;
                }else if(checkedId == mSexButton2.getId()) {
                    sex = "女";
                    sexOK = true;
                }
                submitAble();
            }
        });

        //以下三个是地区内容检查，暂时为必填内容
        mEditCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(mEditCountry,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    countryOK = false;
                }else {
                    countryOK = true;
                }
                submitAble();
            }
        });

        mEditProvince.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(mEditProvince,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    provinceOK = false;
                }else {
                    provinceOK = true;
                }
                submitAble();
            }
        });

        mEditCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //检测空格
                StringUtil.deleteSpace(mEditCity,start);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    cityOK = false;
                }else {
                    cityOK = true;
                }
                submitAble();
            }
        });
    }


    // 检查提交高亮条件
    private void submitAble(){
        if(headPicOK && nicknameOK && sexOK && countryOK && provinceOK && cityOK){
            mSubmitButton.setClickable(true);
            mSubmitButton.setBackgroundResource(R.color.myGreen);
        }else {
            mSubmitButton.setClickable(false);
            mSubmitButton.setBackgroundResource(R.color.myGray);
        }
    }

    //重写权限请求回调 //未用
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }
                else {
                    Toast.makeText(getContext(),"你拒绝了允许",Toast.LENGTH_LONG).show();
                }
                break;
                //请求码不为1 ，则按照系统默认执行
            default:
        }

    }

    /*打开相册方法*/  //未用
    private void openAlbum() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,chose_puhoto);//打开相册
    }

    //重写跳转周期 处理跳转请求（！！原本是protected方法）
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){

            case TAKE_PHOTP:
                if(resultCode == RESULT_OK){
                    //相机回调
                    Log.d(TAG, "相机回调，data:"+data);
                        // 剪切图片
                        startPhotoZoom(cramuri);
                }
                    break;
            case chose_puhoto:
                if(resultCode==RESULT_OK){
                    //相册回调
//                    String path = UriPathEncoder.getPath(getActivity(), data.getData());
//                    Log.d(TAG, "uri="+data.getData());
//                    Log.d(TAG, "path:" +path);
//                    Uri uri = Uri.parse(path);
////                    startPhotoZoom(uri);
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//                    Drawable drawable = new BitmapDrawable(bitmap);
//                    mHeadpicImgbutton.setImageDrawable(drawable);
                    Uri uri = data.getData();
                    startPhotoZoom(uri);
                }
                break;

            case ZOOM_OK:
                Log.d(TAG, "剪裁完成");
                if(resultCode == RESULT_OK){
                    //裁剪回调
//                    Uri uri = data.getData();
//                    Log.d(TAG, "Uri: "+uri.getPath());
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//                    Drawable drawable = new BitmapDrawable(bitmap);
//                    mHeadpicImgbutton.setImageDrawable(drawable);
                    Uri uri = Uri.fromFile(file2);
                    Log.d(TAG, "flie2 --- uri :"+uri);
                    mHeadpicImgbutton.setImageURI(uri);
                    //这个路径很重要，每次打开应用自动访问
                    savedHeadPicFilePath = ImageUtils.getRealPathFromUri(getContext(), Uri.fromFile(file2));
                    Log.d(TAG, "file_path_by_zoomed:"+savedHeadPicFilePath);
                    //转base64,将图片文件转化为字节数组字符串 ， 可以上传到服务器
                    bitmappath = ImageUtils.getImgStr(savedHeadPicFilePath);
                    //提示界面头像完成
                    headPicOK = true;
                    submitAble();
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
        Log.d(TAG, "开始剪裁");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 在Environment.DIRECTORY_PICTURES 文件夹下 创建一个 根据时间戳 命名的图片 ， 用于保存裁剪图片
            file2 = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
        }

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
        intent.putExtra("outputX", 360);
        intent.putExtra("outputY", 360);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));  //把剪裁好的图片作为文件保存
        intent.putExtra("return-data", false); //是否在Intent中返回数据
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别 --- 部分系统自带裁剪自动检测到人脸，并以人脸作中心裁剪 --- 个人猜测
//        intent.putExtra("return-data", true);
        startActivityForResult(intent, ZOOM_OK);
    }



    //网络请求模块
    //处理网络返回结果（报错、页面跳转）
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "转圈圈。。。");
            mProgressBar.setVisibility(View.INVISIBLE);
            Log.d(TAG, "圈圈转完了");
            if(msg.what == 1){
                // 提交完成后
                String result = msg.getData().getString("result");

                if(result.equals("success")){
                    Log.d("Info_Fragement", "提交结果: "+result);
                    Toast.makeText(getContext(),"提交成功",Toast.LENGTH_LONG).show();

//                    将资料存储到SharePreferences
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("username", username);
                    edit.putString("nickname",mNicknameEdit.getText().toString());
                    edit.putString("sex",sex);
                    edit.putString("country",mEditCountry.getText().toString());
                    edit.putString("province",mEditProvince.getText().toString());
                    edit.putString("city",mEditCity.getText().toString());
                    edit.putString("headPicPath",savedHeadPicFilePath); // 头像是本地头像而非服务器的
                    if(edit.commit()){
                        Log.d(TAG, "数据成功保存至本地");
                    }else {
                        Log.d(TAG, "数据保存本地失败");
                    }

                    //进行Activity 跳转
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(),"资料提交失败",Toast.LENGTH_LONG).show();
                }
            }else if(msg.what == -2) {
                Toast.makeText(getContext(),"提交失败,请检查网络",Toast.LENGTH_LONG).show();
            }else if(msg.what == -1){
                Toast.makeText(getContext(),"404 NO FOUND...",Toast.LENGTH_LONG).show();
            }else if(msg.what == 2){
                //上传文件完成后执行
                Log.d(TAG, "handleMessage-uploadResult:"+uploadResult);
                if(!uploadResult.isEmpty()) {
                    username64 = Base64Encoder.encode(username);
                    nickname64 = Base64Encoder.encode(mNicknameEdit.getText().toString());
                    sex64 = Base64Encoder.encode(sex);
                    country64 = Base64Encoder.encode(mEditCountry.getText().toString());
                    province64 = Base64Encoder.encode(mEditProvince.getText().toString());
                    city64 = Base64Encoder.encode( mEditCity.getText().toString());
                    headpic = uploadResult;
                    Log.d(TAG, "提交内容:"+username+"  "+mNicknameEdit.getText().toString()+"  "+sex+"  "+mEditCountry.getText().toString()+"  "+mEditProvince.getText().toString()+"  "+mEditCity.getText().toString()+"  "+ headpic);
                    // OKHTTP 提交表单
                    OkHttpSubmitTask okHttpSubmitTask = new OkHttpSubmitTask();
                    okHttpSubmitTask.execute();
                }else {
                    Toast.makeText(getContext(),"没有上传图片",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "没有上传图片");
                }
            }
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
            handler.sendEmptyMessage(2);
        }
    }

    //只负责发送URL和接收结果(原始HTTP)  淘汰***
    private class SubmitInfoTask extends AsyncTask<URL, Void, String> {
        Handler handler = new Info_Fragment.MyHandler();

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            String res = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                //设置连接超时时间（毫秒）
                conn.setConnectTimeout(5000);
                //设置读取超时时间（毫秒）
                conn.setReadTimeout(5000);
                //设置请求方法
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ser-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");

                if (conn.getResponseCode() == 200) {
                    Log.d(TAG, "发送成功 ");

                    //返回输入流
                    InputStream in = conn.getInputStream();

                    //读取输入流
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                    Log.d(TAG, "接收到结果: "+res);
                }else {
                    handler.sendEmptyMessage(-1);
                }
            } catch (IOException e) {
                handler.sendEmptyMessage(-2);
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {//关闭连接
                    conn.disconnect();
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "数据准备发送。。。");
            super.onPostExecute(s);
            Bundle bundle = new Bundle();
            bundle.putString("result",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            Log.d(TAG, "准备完成，正在处理结果");
            handler.sendMessage(msg);
        }
    }
    // OKHTTP
    public class OkHttpSubmitTask extends AsyncTask<Void,Void,String> {
        MyHandler handler = new MyHandler();
        String TAG ="OKHTTP";
        String url = MyConstants.InfoSubmit_URL;
        String res = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();

        @Override
        protected String doInBackground(Void... voids) {
            postAsynHttp();
            while (res == null){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 延时，等待服务器返回结果
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "数据准备发送。。。");
            super.onPostExecute(s);
            Bundle bundle = new Bundle();
            Log.d(TAG, "onPostExecute: ");
            bundle.putString("result",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            Log.d(TAG, "准备完成，正在处理结果");
            handler.sendMessage(msg);
        }

        public void postAsynHttp() {
            OkHttpClient mOkHttpClient=new OkHttpClient().newBuilder()
                    .connectTimeout(10000, TimeUnit.MILLISECONDS)
                    .readTimeout(10000,TimeUnit.MILLISECONDS)
                    .writeTimeout(10000,TimeUnit.MILLISECONDS).build();

            RequestBody formBody = new FormBody.Builder()
                    .add("username" ,username64)
                    .add("nickname", nickname64)
                    .add("sex",sex64)
                    .add("country",country64)
                    .add("province",province64)
                    .add("city",city64)
                    .add("HPUrl",headpic)
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Call call = mOkHttpClient.newCall(request);
            Log.d(TAG, "发送成功，等待回应");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "Failure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "接到回应");
                    res = response.body().string();
                    Log.d(TAG, "response.body:  "+res);
//                    String jsonstr = new String(Base64.decode(str.getBytes(), Base64.URL_SAFE),"UTF-8");
//                    Log.d(TAG, "JsonStr: "+jsonstr);
//                JsonObject data = new JsonParser().parse(jsonstr).getAsJsonObject();
//                Gson gson = new Gson();
//                ADImage adImage =  gson.fromJson(jsonstr,ADImage.class);
//                ADUrl = adImage.getRes().getPic();
//                Log.i(TAG, "ADUrl：" + ADUrl);
                }

            });
        }
    }
}
