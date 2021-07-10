package com.example.wechatproj.mainpages.ui.found;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.R;

public class foundFragment extends Fragment {

    View friendCircleLayout;
    private foundViewModel foundViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foundViewModel =
                ViewModelProviders.of(this).get(foundViewModel.class);
        View root = inflater.inflate(R.layout.fragment_found, container, false);
        foundViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        friendCircleLayout = getView().findViewById(R.id.friendCircleLayout);

        friendCircleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendCircleActivity.class);
                startActivity(intent);
            }
        });
    }
}