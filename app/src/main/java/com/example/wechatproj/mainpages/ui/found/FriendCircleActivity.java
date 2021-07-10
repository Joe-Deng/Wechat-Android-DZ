package com.example.wechatproj.mainpages.ui.found;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wechatproj.Adapters.FriendCircleAdapter;
import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.Database.ViewModel.FriendCirclesViewModel;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;
import com.example.wechatproj.Utils.ImageUtils;
import com.example.wechatproj.Utils.UriPathEncoder;

import java.io.File;
import java.util.List;

public class FriendCircleActivity extends AppCompatActivity {

    ImageButton cameraButton;
    ImageView myBackground,myHeadPic;
    TextView NicknameView;
    View scrollView;
    RecyclerView recyclerView;
    FriendCircleAdapter friendCircleAdapter;
    FriendCirclesViewModel friendCirclesViewModel;
    SharedPreferences sharedPreferences;
    public static final int TAKE_PHOTP=1;
    public static final int chose_puhoto=2;
    private static final int ZOOM_OK = 3;
    public static final int TAKE_PHOTP_PUBLISH=4;
    public static final int chose_puhoto_publish=5;
    private static final int ZOOM_OK_PUBLISH = 6;
    private File currentImageFile = null;
    private File file2 = null;
    private Uri cramuri;
    String myNickname,myHeadPicPath,backgroundPath,username = null;
    String TAG = "FriendCircleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_circle);
        Log.d(TAG, "onCreate: ");

        cameraButton = findViewById(R.id.cameraButton);
        myBackground = findViewById(R.id.myBackground);
        myHeadPic = findViewById(R.id.myHeadPic);
        scrollView = findViewById(R.id.scrollView2);
        recyclerView = findViewById(R.id.recyclerView);
        NicknameView = findViewById(R.id.myNickname);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        sharedPreferences = getSharedPreferences("my_data", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username","");
        friendCircleAdapter = new FriendCircleAdapter(FriendCircleActivity.this,username);
        recyclerView.setAdapter(friendCircleAdapter);

        friendCirclesViewModel = ViewModelProviders.of(this).get(FriendCirclesViewModel.class);
        friendCirclesViewModel.setFriendCircleRepository(username);
        friendCirclesViewModel.getAllFriendCircleLive().observe(this, new Observer<List<FriendCircle>>() {
            @Override
            public void onChanged(List<FriendCircle> friendCircles) {
                friendCircleAdapter.setFriendCircles(friendCircles);
                friendCircleAdapter.notifyDataSetChanged();
            }
        });





        //获取sharedPreferences里的数据
        myNickname = sharedPreferences.getString("nickname","");
        myHeadPicPath  = sharedPreferences.getString("headPicPath","");
        backgroundPath  = sharedPreferences.getString("backgroundPath","");

        //设置朋友圈头像
        Bitmap bitmap = BitmapFactory.decodeFile(myHeadPicPath);
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,200,200,20);
        myHeadPic.setImageBitmap(renderBitmap);

        //设置朋友圈背景（先查询有没有图片）
        if(backgroundPath!=null && !backgroundPath.isEmpty()){
            Bitmap backgroundBitmap = BitmapFactory.decodeFile(backgroundPath);
            myBackground.setImageBitmap(backgroundBitmap);
        }

        //设置昵称
        NicknameView.setText(myNickname);

//        //测试
//
//        final FriendCircle friendCircle = new FriendCircle(System.currentTimeMillis(),username,myNickname,myHeadPicPath,"今天刚换的头像，好不好看？",myHeadPicPath,"ok");
//        cameraButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                friendCirclesViewModel.insertFriendCircle(friendCircle);
//            }
//        });

        //设置背景图片
        final String[] items = new String[]{"本地相册","拍照"};
        myBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(FriendCircleActivity.this)
                        .setTitle("选择图片")
                        .setIcon(R.drawable.ic_camera_alt_black_24dp)
                        .setItems(items, new DialogInterface.OnClickListener() {//添加列表
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i == 0){
                                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, null);
//                                    Pick 的方式可以在真机上运行，但在模拟器不行
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


        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(FriendCircleActivity.this)
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
                                    startActivityForResult(intent,chose_puhoto_publish);
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
                                    startActivityForResult(intent,TAKE_PHOTP_PUBLISH);
                                }
                            }
                        }).create();
                alertDialog.show();
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scrollView.onTouchEvent(event);
        return true;
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
                    if (android.os.Build.VERSION.SDK_INT >= 28){
                        startPhotoZoom(uri);
                    } else {    //部分机型适配不了Crop，只能直接设置为图片
                        myBackground.setImageURI(uri);
                        String path = UriPathEncoder.getPath(getApplicationContext(), uri);
                        //保存到本地sharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("backgroundPath", path);
                        editor.commit();
                        Log.d(TAG, "背景图片设置并保存成功！！！！");
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
                    myBackground.setImageURI(uri);
                    //这个路径很重要，每次打开应用自动访问
                    String backgroundPath = ImageUtils.getRealPathFromUri(getApplicationContext(), uri);
                    //保存到本地sharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("backgroundPath", backgroundPath);
                    editor.commit();
                    Log.d(TAG, "背景图片设置并保存成功！！！！");
                }
                break;

            case TAKE_PHOTP_PUBLISH:
                Log.d(TAG, "拍照完成准备编辑");
                if (resultCode == RESULT_OK) {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        // 在Environment.DIRECTORY_PICTURES 文件夹下 创建一个 根据时间戳 命名的图片 ， 用于保存裁剪图片
                        file2 = new File(FriendCircleActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
                    }
                    Log.d(TAG, "开始剪裁");

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(cramuri, "image/*");
                    // 临时获取Uri 的读写能力（通过Uri 控制文件）
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // 设置裁剪
                    intent.putExtra("crop", "true");
                    // aspectX aspectY 是宽高的比例
                    intent.putExtra("aspectX", 3);
                    intent.putExtra("aspectY", 2);
                    // outputX outputY 是裁剪图片宽高
//        intent.putExtra("outputX", 500);
//        intent.putExtra("outputY", 500);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file2));  //把剪裁好的图片作为文件保存
                    intent.putExtra("return-data", false); //是否在Intent中返回数据
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
                    intent.putExtra("noFaceDetection", true);// 取消人脸识别 --- 部分系统自带裁剪自动检测到人脸，并以人脸作中心裁剪 --- 个人猜测
//        intent.putExtra("return-data", true);
                    Log.d(TAG, "图片裁剪完成！");
                    startActivityForResult(intent, ZOOM_OK_PUBLISH);



                }
                break;

            case chose_puhoto_publish:
                Log.d(TAG, "选择图片完成，准备编辑");
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Intent intent = new Intent(this,PublishFriendCiecleActivity.class);
                    String path = UriPathEncoder.getPath(getApplicationContext(), uri);
                    Bundle bundle = new Bundle();
                    bundle.putString("imagePath",path);
                    Log.d(TAG, "path:"+path);
                    bundle.putString("SID",username);
                    bundle.putString("nickname",myNickname);
                    bundle.putString("headPicPath",myHeadPicPath);
                    //剩下两个参数在那边设置
                    intent.putExtras(bundle);
                    startActivity(intent);
                    this.finish();
                }
                break;

            case ZOOM_OK_PUBLISH:{
                Uri uri = Uri.fromFile(file2);
                Log.d(TAG, "flie2 --- uri :" + uri);
                myBackground.setImageURI(uri);
                //这个路径很重要，每次打开应用自动访问
                Intent intent = new Intent(this,PublishFriendCiecleActivity.class);
                String path = UriPathEncoder.getPath(getApplicationContext(), uri);
                Bundle bundle = new Bundle();
                bundle.putString("imagePath",path);
                Log.d(TAG, "path:"+path);
                bundle.putString("SID",username);
                bundle.putString("nickname",myNickname);
                bundle.putString("headPicPath",myHeadPicPath);
                //只传4个参数，剩下两个参数在那边设置
                intent.putExtras(bundle);
                startActivity(intent);
                this.finish();
            }

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
            file2 = new File(FriendCircleActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");
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
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 2);
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

}
