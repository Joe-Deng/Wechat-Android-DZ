package com.example.wechatproj.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Entity.Message;
import com.example.wechatproj.Database.ViewModel.MessagesViewModel;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.DataUtil;
import com.example.wechatproj.Utils.ImageShader;
import com.example.wechatproj.functionpages.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeMessagesAdapter extends RecyclerView.Adapter<HomeMessagesAdapter.HomeMessagesHolder> {
    private List<Friend> allFriends = new ArrayList<>();
    private String username;
    private Activity activity;
    private MessagesViewModel messagesViewModel;


    //两个方法初始化Adapter
    public void setAllFriends(List<Friend> allFriends) {
        this.allFriends = allFriends;
    }

    public HomeMessagesAdapter(Activity activity, String username){
        this.activity = activity;
        this.username = username;
        this.messagesViewModel = ViewModelProviders.of((FragmentActivity) activity).get(MessagesViewModel.class);
        messagesViewModel.setMessageRepository(username);
        Log.d("重点排查22", "1 ");
    }

    //重写三个方法
    @NonNull
    @Override
    public HomeMessagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        itemView = layoutInflater.inflate(R.layout.cell_home_message,parent,false);
        return new HomeMessagesHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeMessagesHolder holder, int position) {
        Log.d("重点排查22", "2 "+allFriends);
        final Friend friend = allFriends.get(position);
        Log.d("重点排查22", "3"+friend);
        String fUsername = friend.getUsername();
        Log.d("重点排查22", "4"+fUsername);
        Message friendLastMessage = messagesViewModel.getFriendLastMessage(fUsername);
//        Log.d("重点排查hms", "最后一条消息:"+friendLastMessage.getM_Content());
        String fNickname = friend.getNickname();
        if(fNickname.length()>13){
            fNickname = fNickname.substring(0,12)+"...";
        }
        holder.nickname.setText(fNickname);
//        Log.d("重点排查27", "FriendNewMessages"+messagesViewModel.getFriendNewMessages(fUsername).getValue());
        if(friendLastMessage!=null && !friendLastMessage.getM_Content().isEmpty()){
            if(friendLastMessage.getM_Type() == 2){
                holder.lastMessage.setText("[ 图片 ]");
            }else {
                holder.lastMessage.setText(friendLastMessage.getM_Content());
            }
            Log.d("重点排查29", holder.lastMessage.getText().toString());
            DataUtil dataUtil = new DataUtil(friendLastMessage.getM_Time());
            holder.timeText.setText(dataUtil.getTimeStr());
            Log.d("重点排查28", holder.timeText.getText().toString());

            //暂时不判断数量，之后实现了再替代
            if(!friendLastMessage.getIF_Readed() && friendLastMessage.getSID().equals(friend.getUsername())){
                holder.redpot.setVisibility(View.VISIBLE);
            }else {
                holder.redpot.setVisibility(View.INVISIBLE);
            }
//            if(friendNewMessagesCount == 0){
//                holder.redpot.setVisibility(View.INVISIBLE);
//            }else if(friendNewMessagesCount<100) {
//                holder.redpot.setVisibility(View.VISIBLE);
//                holder.redpot.setText(String.valueOf(friendNewMessagesCount));
//            }else {
//                holder.redpot.setVisibility(View.VISIBLE);
//                holder.redpot.setText("99+");
//            }
        }else {
            holder.lastMessage.setText("");
            holder.timeText.setText("从未联系");
            holder.redpot.setVisibility(View.INVISIBLE);
            Log.d("重点排查28", "空");
        }

        /** 裁剪图片-圆角 **/
        Bitmap bitmap = BitmapFactory.decodeFile(friend.getHeadPicPath());
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,120,120,20);
        holder.headPic.setImageBitmap(renderBitmap);
        holder.openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.putExtra("username",friend.getUsername());
                intent.putExtra("from","home");
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allFriends.size();
    }

    static class HomeMessagesHolder extends RecyclerView.ViewHolder{
        TextView nickname,lastMessage,timeText;
        ImageButton headPic;
        Button openButton;
        TextView redpot;
        public HomeMessagesHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeView);
            nickname = itemView.findViewById(R.id.nicknameView);
            lastMessage = itemView.findViewById(R.id.introduction);
            headPic = itemView.findViewById(R.id.headPic);
            openButton = itemView.findViewById(R.id.searchButton);
            redpot = itemView.findViewById(R.id.redpot);
        }
    }
}
