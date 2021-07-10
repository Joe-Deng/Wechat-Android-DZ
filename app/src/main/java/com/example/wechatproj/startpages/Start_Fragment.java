package com.example.wechatproj.startpages;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wechatproj.MainActivity;
import com.example.wechatproj.R;
import com.example.wechatproj.ViewModel.StartPageViewModel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class Start_Fragment extends Fragment {
    Button loginButton,registerButton;
    StartPageViewModel viewModel ;
    private MutableLiveData<Boolean> isFirstStartLive;
    private Boolean isFirstStart;

    public Start_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StartPageViewModel.class);
        isFirstStartLive = viewModel.getIsFirstStart();
        isFirstStart = isFirstStartLive.getValue();
        Log.d("StartPage", "isFirstStart: "+isFirstStart);
        loginButton = getView().findViewById(R.id.login_button);
        registerButton = getView().findViewById(R.id.regist_button);



        final Handler handler=new Handler();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(runnableUi);
            }
        };
        if(isFirstStart == false){
            timer.schedule(task,0);
        }else {
            timer.schedule(task,2000);
            isFirstStart = false;
            isFirstStartLive = new MutableLiveData<Boolean>(false);
            viewModel.setIsFirstStart(isFirstStartLive);
        }

    }

    final Runnable runnableUi=new  Runnable(){
        @Override
        public void run() {
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.VISIBLE);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController controller = Navigation.findNavController(v);
                    controller.navigate(R.id.action_start_Fragment_to_login_Fragment);
                }
            });
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController controller = Navigation.findNavController(v);
                    controller.navigate(R.id.action_start_Fragment_to_register_Fragment);
                }
            });
        }
    };
}
