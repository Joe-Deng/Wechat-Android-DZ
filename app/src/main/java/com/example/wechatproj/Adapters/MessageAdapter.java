package com.example.wechatproj.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private String TAG = "MessageAdapter";
    private List<Message>allMessages = new ArrayList<>();
    private String myUsername,fUsername,myHeadPicPath,fHeadPicPath;

    public void setAllMessages(List<Message> allMessages) {
        this.allMessages = allMessages;
    }

    //构造方法
    public MessageAdapter(String myUsername, String fUsername, String myHeadPicPath, String fHeadPicPath) {
        this.myUsername = myUsername;
        this.fUsername = fUsername;
        this.myHeadPicPath = myHeadPicPath;
        this.fHeadPicPath = fHeadPicPath;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 0;
        if (allMessages.size() > 0) {
            Message message = allMessages.get(position);
            int mtype = message.getM_Type();
            boolean ifSend = message.getIF_Send();
            String SID = message.getSID();
            //类型判断 ，方便选择layout
            if(SID.equals(myUsername)){
//                Log.d(TAG, "发送者:"+myUsername);
                if(mtype==0 || mtype==1){
                    if(ifSend){
                        type = 1;  //"me_text"
                    }else {
                        type = 2; //"me_text_notsend"
                    }
                }else if(mtype == 2){
                    type = 3;  //"me_image"
                }
            }else {
//                Log.d(TAG, "发送者:"+fUsername);
                if(mtype==0 || mtype==1){
                    type = 4;  //"friend_text"
                }else if(mtype == 2){
                    type = 5; //"friend_image"
                }
            }
            return type;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = null;

        Log.d(TAG, "type: "+viewType);
        switch (viewType){
            case 4: itemView = layoutInflater.inflate(R.layout.cell_friend_message,parent,false);
            break;
            case 5: itemView = layoutInflater.inflate(R.layout.cell_friend_image,parent,false);
            break;
            case 1: itemView = layoutInflater.inflate(R.layout.cell_me_message,parent,false);
            break;
            case 2: itemView = layoutInflater.inflate(R.layout.cell_me_message_notsend,parent,false);
            break;
            case 3: itemView = layoutInflater.inflate(R.layout.cell_me_image,parent,false);
            break;
        }
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {
        final Message message = allMessages.get(position);
        int type = 0;
            int mtype = message.getM_Type();
            boolean ifSend = message.getIF_Send();
            String SID = message.getSID();
            //类型判断 ，方便选择layout
            if(SID.equals(myUsername)){

                if(mtype==0 || mtype==1){
                    if(ifSend){
                        type = 1;  //"me_text"
                    }else {
                        type = 2; //"me_text_notsend"
                    }
                }else if(mtype == 2){
                    type = 3;  //"me_image"
                }
            }else {

                if(mtype==0 || mtype==1){
                    type = 4;  //"friend_text"
                }else if(mtype == 2){
                    type = 5; //"friend_image"
                }
            }
        //重绑组件
        switch (type){
            case 4:{
                holder.headPicPath = fHeadPicPath;
                holder.textView = holder.itemView.findViewById(R.id.text);
                holder.text = message.getM_Content();
                holder.textView.setText(holder.text);
            }
            break;
            case 1:
            case 2:{
                holder.headPicPath = myHeadPicPath;
                holder.textView = holder.itemView.findViewById(R.id.text);
                holder.text = message.getM_Content();
                holder.textView.setText(holder.text);
            }
                break;
            case 5:{
                holder.headPicPath = fHeadPicPath;
                holder.imageView = holder.itemView.findViewById(R.id.image);
                holder.imagePath = message.getM_Content();
                Bitmap imgBitmap = BitmapFactory.decodeFile(holder.imagePath);
                holder.imageView.setImageBitmap(imgBitmap);
            }
            break;
            case 3: {
                holder.headPicPath = myHeadPicPath;
                holder.imageView = holder.itemView.findViewById(R.id.image);
                holder.imagePath = message.getM_Content();
                Bitmap imgBitmap = BitmapFactory.decodeFile(holder.imagePath);
                holder.imageView.setImageBitmap(imgBitmap);
            }
                break;
        }
//        Log.d(TAG, "绑定中：当前positon="+position);

        // 查询对象的头像String格式的Path ，再通过path找到文件转成bitmap，最后渲染ImageView
        /** 裁剪图片-圆角 **/
        Bitmap bitmap = BitmapFactory.decodeFile(holder.headPicPath);
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,120,120,20);
        holder.headPic.setImageBitmap(renderBitmap);
    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder{
        String headPicPath;
        ImageButton headPic;
        TextView textView;
        ImageView imageView;
        String text;
        String imagePath;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            headPic = itemView.findViewById(R.id.headPic);
        }
    }

    // 类型判断,重新定义组件
    private void redefine(){

    }
}
