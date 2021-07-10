package com.example.wechatproj.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Database.Entity.FriendRequest;
import com.example.wechatproj.Database.ViewModel.FriendRequestsViewModel;
import com.example.wechatproj.R;
import com.example.wechatproj.Utils.ImageShader;
import com.example.wechatproj.functionpages.AddFriendActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestHolder> {
    private List<FriendRequest> allFriendRequests = new ArrayList<>();
    private FriendRequestsViewModel friendRequestsViewModel;
    private String username;
    private AddFriendActivity activity;

    //两个方法初始化Adapter
    public void setAllFriendRequests(List<FriendRequest> allFriendRequests) {
        this.allFriendRequests = allFriendRequests;
    }

    public FriendRequestAdapter(AddFriendActivity activity, String username){
        this.activity = activity;
        this.username = username;
        this.friendRequestsViewModel = ViewModelProviders.of((FragmentActivity) activity).get(FriendRequestsViewModel.class);
        AddFriendActivity addFriendActivity = activity;
    }

    //重写三个方法
    @NonNull
    @Override
    public FriendRequestAdapter.RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        itemView = layoutInflater.inflate(R.layout.cell_friend_request,parent,false);
        return new RequestHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestHolder holder, int position) {

        final FriendRequest friendRequest = allFriendRequests.get(position);
        String nickname = friendRequest.getName();
        if(nickname.length()<12){
            holder.nickname.setText(nickname);
        }else {
            holder.nickname.setText(nickname.substring(0,11)+"...");
        }
        String introduction = friendRequest.getText();
        if(introduction.length()<20){
            holder.introduction.setText(introduction);
        }else {
            holder.introduction.setText(introduction.substring(0,19)+"...");
        }
        /** 裁剪图片-圆角 **/
        Bitmap bitmap = BitmapFactory.decodeFile(friendRequest.getHeadPicPath());
        Log.d("排错", "onBindViewHolder: headpicpath " + friendRequest.getHeadPicPath());
        ImageShader imageShader = new ImageShader();
        Bitmap renderBitmap = imageShader.roundBitmapByShader(bitmap,120,120,20);
        holder.headPic.setImageBitmap(renderBitmap);
        //下面两个是到本地数据库查询是否已经处理过
        if(friendRequest.getStatus().equals("yes")){
            holder.noButton.setVisibility(View.INVISIBLE);
            holder.yesButton.setVisibility(View.INVISIBLE);
            holder.result.setVisibility(View.VISIBLE);
            holder.result.setText("已同意");
        }else if(friendRequest.getStatus().equals("no")){
            holder.noButton.setVisibility(View.INVISIBLE);
            holder.yesButton.setVisibility(View.INVISIBLE);
            holder.result.setVisibility(View.VISIBLE);
            holder.result.setText("已拒绝");
        }
        //如果未处理
        holder.yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestsViewModel.yesFriendRequest(friendRequest.getUsername());  //本地
                activity.responseFriendRequest(friendRequest.getUsername(),username,"yes"); //通过service向服务器发送结果
                holder.noButton.setVisibility(View.INVISIBLE);
                holder.yesButton.setVisibility(View.INVISIBLE);
                holder.result.setVisibility(View.VISIBLE);
                holder.result.setText("已同意");
                activity.new AddFriend().execute(friendRequest.getUsername());
                Toast.makeText(activity,"成功添加好友",Toast.LENGTH_LONG).show();
            }
        });
        holder.noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestsViewModel.noFriendRequest(friendRequest.getUsername());   //本地
                activity.responseFriendRequest(friendRequest.getUsername(),username,"no"); //通过service向服务器发送结果
                holder.noButton.setVisibility(View.INVISIBLE);
                holder.yesButton.setVisibility(View.INVISIBLE);
                holder.result.setVisibility(View.VISIBLE);
                holder.result.setText("已拒绝");
            }
        });
    }

    @Override
    public int getItemCount() {
        return allFriendRequests.size();
    }

    static class RequestHolder extends RecyclerView.ViewHolder{
        TextView nickname,introduction,result;
        ImageButton headPic;
        Button yesButton,noButton;
        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.nicknameView);
            introduction = itemView.findViewById(R.id.introduction);
            result = itemView.findViewById(R.id.result);
            headPic = itemView.findViewById(R.id.headPic);
            yesButton = itemView.findViewById(R.id.yesButton);
            noButton = itemView.findViewById(R.id.noButton);
        }
    }
}
