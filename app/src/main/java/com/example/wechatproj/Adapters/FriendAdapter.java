package com.example.wechatproj.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;
import com.example.wechatproj.functionpages.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    Activity activity;

    public void setAllFriends(List<Friend> allFriends) {
        this.allFriends = allFriends;
    }

    private List<Friend>allFriends = new ArrayList<>();

    public FriendAdapter(Activity activity){
        this.activity = activity;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        itemView = layoutInflater.inflate(R.layout.cell_friend,parent,false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        final Friend friend = allFriends.get(position);
        // 查询对象的头像String格式的Path ，再通过path找到文件转成bitmap，最后渲染ImageView
        /** 裁剪图片-圆角 **/

        Bitmap bitmap = BitmapFactory.decodeFile(friend.getHeadPicPath());
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,200,200,20);
        holder.headPicture.setImageBitmap(renderBitmap);
        holder.nickname.setText(friend.getNickname());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出 FriendInfoFragment
                Bundle argBundle = new Bundle();
                argBundle.putString("username",friend.getUsername());
                Log.d("FriendAdapter", "验证:activity:"+activity);
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.putExtras(argBundle);

                activity.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return allFriends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder{
        ImageView headPicture;
        TextView nickname;
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            headPicture = itemView.findViewById(R.id.headPic);
            nickname = itemView.findViewById(R.id.nicknameView);
        }
    }
}
