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
    String savedHeadPicFilePath;    //?????????????????????????????????
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
        //?????????
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
        //???????????????????????????
        requiresPermission();

        //????????????SDK????????????????????????(android 7.0 --- SDK 24 )???????????????StrictMode ??????Android??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        final String[] items = new String[]{"????????????","??????"};
        mHeadpicImgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("??????????????????")
                        .setIcon(R.drawable.ic_camera_alt_black_24dp)
                        .setItems(items, new DialogInterface.OnClickListener() {//????????????
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    //??????????????????
//                                    if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
////                                    {
////                                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
////                                    }else {
////                                        openAlbum();
////                                    }
                                    //Pick ?????????????????????????????????????????????????????????
                                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    startActivityForResult(intent,chose_puhoto);
//                                    openAlbum();
                                }else if(i == 1){
                                    //????????????
                                    // ??????????????????SD??????????????????????????????????????????????????? |*File | ???????????????jpg?????????????????????Camera????????????
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        currentImageFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
                                    }
                                    // ???????????????????????? Intent
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                     // ???????????????????????? Android 7.0
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Log.d(TAG, "??????Android7.0");
                                        cramuri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileProvider", currentImageFile);
                                        Log.d(TAG, "cramuri??? "+cramuri);
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

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                new UploadTask().execute(savedHeadPicFilePath);
            }
        });
    }

    //??????EasyPermissions???????????????
    @AfterPermissionGranted(RC_EXTERNAL_STORAGE_PERM)
    private void requiresPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
        } else {
            EasyPermissions.requestPermissions(this,  getResources().getString(R.string.tips_crema), RC_EXTERNAL_STORAGE_PERM, perms);
        }
    }

    //???????????????
    private void buttonObserver(){
        mNicknameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //????????????
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
                    sex = "???";
                    sexOK = true;
                }else if(checkedId == mSexButton2.getId()) {
                    sex = "???";
                    sexOK = true;
                }
                submitAble();
            }
        });

        //?????????????????????????????????????????????????????????
        mEditCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //????????????
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
                //????????????
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
                //????????????
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


    // ????????????????????????
    private void submitAble(){
        if(headPicOK && nicknameOK && sexOK && countryOK && provinceOK && cityOK){
            mSubmitButton.setClickable(true);
            mSubmitButton.setBackgroundResource(R.color.myGreen);
        }else {
            mSubmitButton.setClickable(false);
            mSubmitButton.setBackgroundResource(R.color.myGray);
        }
    }

    //???????????????????????? //??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }
                else {
                    Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_LONG).show();
                }
                break;
                //???????????????1 ??????????????????????????????
            default:
        }

    }

    /*??????????????????*/  //??????
    private void openAlbum() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,chose_puhoto);//????????????
    }

    //?????????????????? ????????????????????????????????????protected?????????
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){

            case TAKE_PHOTP:
                if(resultCode == RESULT_OK){
                    //????????????
                    Log.d(TAG, "???????????????data:"+data);
                        // ????????????
                        startPhotoZoom(cramuri);
                }
                    break;
            case chose_puhoto:
                if(resultCode==RESULT_OK){
                    //????????????
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
                Log.d(TAG, "????????????");
                if(resultCode == RESULT_OK){
                    //????????????
//                    Uri uri = data.getData();
//                    Log.d(TAG, "Uri: "+uri.getPath());
//                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//                    Drawable drawable = new BitmapDrawable(bitmap);
//                    mHeadpicImgbutton.setImageDrawable(drawable);
                    Uri uri = Uri.fromFile(file2);
                    Log.d(TAG, "flie2 --- uri :"+uri);
                    mHeadpicImgbutton.setImageURI(uri);
                    //??????????????????????????????????????????????????????
                    savedHeadPicFilePath = ImageUtils.getRealPathFromUri(getContext(), Uri.fromFile(file2));
                    Log.d(TAG, "file_path_by_zoomed:"+savedHeadPicFilePath);
                    //???base64,????????????????????????????????????????????? ??? ????????????????????????
                    bitmappath = ImageUtils.getImgStr(savedHeadPicFilePath);
                    //????????????????????????
                    headPicOK = true;
                    submitAble();
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
        Log.d(TAG, "????????????");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // ???Environment.DIRECTORY_PICTURES ???????????? ???????????? ??????????????? ??????????????? ??? ????????????????????????
            file2 = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
        }

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
        intent.putExtra("outputX", 360);
        intent.putExtra("outputY", 360);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));  //???????????????????????????????????????
        intent.putExtra("return-data", false); //?????????Intent???????????????
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// ????????????
        intent.putExtra("noFaceDetection", true);// ?????????????????? --- ??????????????????????????????????????????????????????????????????????????? --- ????????????
//        intent.putExtra("return-data", true);
        startActivityForResult(intent, ZOOM_OK);
    }



    //??????????????????
    //???????????????????????????????????????????????????
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
            Log.d(TAG, "??????????????????");
            mProgressBar.setVisibility(View.INVISIBLE);
            Log.d(TAG, "???????????????");
            if(msg.what == 1){
                // ???????????????
                String result = msg.getData().getString("result");

                if(result.equals("success")){
                    Log.d("Info_Fragement", "????????????: "+result);
                    Toast.makeText(getContext(),"????????????",Toast.LENGTH_LONG).show();

//                    ??????????????????SharePreferences
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("username", username);
                    edit.putString("nickname",mNicknameEdit.getText().toString());
                    edit.putString("sex",sex);
                    edit.putString("country",mEditCountry.getText().toString());
                    edit.putString("province",mEditProvince.getText().toString());
                    edit.putString("city",mEditCity.getText().toString());
                    edit.putString("headPicPath",savedHeadPicFilePath); // ???????????????????????????????????????
                    if(edit.commit()){
                        Log.d(TAG, "???????????????????????????");
                    }else {
                        Log.d(TAG, "????????????????????????");
                    }

                    //??????Activity ??????
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_LONG).show();
                }
            }else if(msg.what == -2) {
                Toast.makeText(getContext(),"????????????,???????????????",Toast.LENGTH_LONG).show();
            }else if(msg.what == -1){
                Toast.makeText(getContext(),"404 NO FOUND...",Toast.LENGTH_LONG).show();
            }else if(msg.what == 2){
                //???????????????????????????
                Log.d(TAG, "handleMessage-uploadResult:"+uploadResult);
                if(!uploadResult.isEmpty()) {
                    username64 = Base64Encoder.encode(username);
                    nickname64 = Base64Encoder.encode(mNicknameEdit.getText().toString());
                    sex64 = Base64Encoder.encode(sex);
                    country64 = Base64Encoder.encode(mEditCountry.getText().toString());
                    province64 = Base64Encoder.encode(mEditProvince.getText().toString());
                    city64 = Base64Encoder.encode( mEditCity.getText().toString());
                    headpic = uploadResult;
                    Log.d(TAG, "????????????:"+username+"  "+mNicknameEdit.getText().toString()+"  "+sex+"  "+mEditCountry.getText().toString()+"  "+mEditProvince.getText().toString()+"  "+mEditCity.getText().toString()+"  "+ headpic);
                    // OKHTTP ????????????
                    OkHttpSubmitTask okHttpSubmitTask = new OkHttpSubmitTask();
                    okHttpSubmitTask.execute();
                }else {
                    Toast.makeText(getContext(),"??????????????????",Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "??????????????????");
                }
            }
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
            handler.sendEmptyMessage(2);
        }
    }

    //???????????????URL???????????????(??????HTTP)  ??????***
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
                //????????????????????????????????????
                conn.setConnectTimeout(5000);
                //????????????????????????????????????
                conn.setReadTimeout(5000);
                //??????????????????
                conn.setRequestMethod("GET");
                conn.setRequestProperty("ser-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");

                if (conn.getResponseCode() == 200) {
                    Log.d(TAG, "???????????? ");

                    //???????????????
                    InputStream in = conn.getInputStream();

                    //???????????????
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    res = result.toString();
                    Log.d(TAG, "???????????????: "+res);
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
                if (conn != null) {//????????????
                    conn.disconnect();
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "???????????????????????????");
            super.onPostExecute(s);
            Bundle bundle = new Bundle();
            bundle.putString("result",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            Log.d(TAG, "?????????????????????????????????");
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
                .get()//????????????GET?????????????????????
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
                // ????????????????????????????????????
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "???????????????????????????");
            super.onPostExecute(s);
            Bundle bundle = new Bundle();
            Log.d(TAG, "onPostExecute: ");
            bundle.putString("result",s);
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = 1;
            Log.d(TAG, "?????????????????????????????????");
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
            Log.d(TAG, "???????????????????????????");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "Failure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "????????????");
                    res = response.body().string();
                    Log.d(TAG, "response.body:  "+res);
//                    String jsonstr = new String(Base64.decode(str.getBytes(), Base64.URL_SAFE),"UTF-8");
//                    Log.d(TAG, "JsonStr: "+jsonstr);
//                JsonObject data = new JsonParser().parse(jsonstr).getAsJsonObject();
//                Gson gson = new Gson();
//                ADImage adImage =  gson.fromJson(jsonstr,ADImage.class);
//                ADUrl = adImage.getRes().getPic();
//                Log.i(TAG, "ADUrl???" + ADUrl);
                }

            });
        }
    }
}
