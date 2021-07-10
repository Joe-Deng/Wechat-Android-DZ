package com.example.wechatproj.mainpages.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechatproj.Adapters.HomeMessagesAdapter;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.ViewModel.FriendsViewModel;
import com.example.wechatproj.R;
import com.example.wechatproj.ViewModel.HomeBottomViewModel;

import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    HomeMessagesAdapter adapter;
    FriendsViewModel friendsViewModel;
    String username = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
       friendsViewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        username = getActivity().getSharedPreferences("my_data", Context.MODE_PRIVATE).getString("username","");
        Log.d("重点排查21", "username= "+username);
        adapter = new HomeMessagesAdapter(getActivity(),username);
        Log.d("重点排查21", "2");
        recyclerView.setAdapter(adapter);
        Log.d("重点排查21", "3");
        friendsViewModel.getAllFriendLive().observe(getActivity(), new Observer<List<Friend>>() {
            @Override
            public void onChanged(List<Friend> friends) {
                Log.d("重点排查21", "4");
                adapter.setAllFriends(friends);
                adapter.notifyDataSetChanged();
            }
        });

    }
}