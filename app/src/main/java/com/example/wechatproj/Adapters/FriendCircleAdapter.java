package com.example.wechatproj.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.DataUtil;
import com.example.wechatproj.Utils.ImageShader;

import java.util.ArrayList;
import java.util.List;

public class FriendCircleAdapter extends RecyclerView.Adapter<FriendCircleAdapter.FriendCircleViewHolder> {
    Activity activity;
    List<FriendCircle> friendCircles = new ArrayList<>();
    String myUsername;

    public FriendCircleAdapter(Activity activity, String myUsername) {
        this.activity = activity;
        this.myUsername = myUsername;
    }

    public void setFriendCircles(List<FriendCircle> friendCircles) {
        this.friendCircles = friendCircles;
    }




    @NonNull
    @Override
    public FriendCircleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        itemView = layoutInflater.inflate(R.layout.cell_friend_circle,parent,false);
        return new FriendCircleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendCircleViewHolder holder, int position) {
        FriendCircle friendCircle = friendCircles.get(position);

        Bitmap bitmap = BitmapFactory.decodeFile(friendCircle.getHeadPicPath());
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,120,120,20);
        holder.headPic.setImageBitmap(renderBitmap);

        holder.nickname.setText(friendCircle.getNickname());
        holder.text.setText(friendCircle.getText());
        String imagePath = friendCircle.getImagePath();

        if(imagePath!=null && !imagePath.isEmpty()){
            Bitmap imaBitmap = BitmapFactory.decodeFile(imagePath);
            holder.image.setImageBitmap(imaBitmap);
            holder.image.setImageBitmap(imaBitmap);
        }

        DataUtil dataUtil = new DataUtil(friendCircle.getM_Time());
        int days = dataUtil.getDays();
        int hours = dataUtil.getHours();
        int minutes = dataUtil.getMinutes();
        if(System.currentTimeMillis()-friendCircle.getM_Time()>=86400*1000){
            holder.timeText.setText(days+"天前");
        }else if(System.currentTimeMillis()-friendCircle.getM_Time()>=3600*1000){
            holder.timeText.setText(hours+"小时前");
        }else if(System.currentTimeMillis()-friendCircle.getM_Time()>=60*1000){
            holder.timeText.setText(minutes+"分钟前");
        }else {
            holder.timeText.setText("刚刚");
        }

        //下面过于复杂，暂时不开发
//        List<Comment> comments = friendCircle.getComments();
//        holder.commentView.setLayoutManager();

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.moreLayout.setVisibility(View.VISIBLE);
            }
        });

        holder.moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.moreLayout.setVisibility(View.INVISIBLE);
                Toast.makeText(activity,"暂时没有开放评论功能",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return friendCircles.size();
    }

    static class FriendCircleViewHolder extends RecyclerView.ViewHolder{
        ImageButton headPic,moreButton;
        ImageView image;
        TextView nickname,text,timeText;
        RecyclerView commentView;

        View moreLayout;

        public FriendCircleViewHolder(@NonNull View itemView) {
            super(itemView);
            headPic = itemView.findViewById(R.id.headPic);
            image = itemView.findViewById(R.id.image);
            nickname = itemView.findViewById(R.id.nicknameView);
            text = itemView.findViewById(R.id.text);
            timeText = itemView.findViewById(R.id.timeText);
            moreButton = itemView.findViewById(R.id.moreButton);
            moreLayout = itemView.findViewById(R.id.moreLayout);
            commentView = itemView.findViewById(R.id.commentView);
        }

    }
}
