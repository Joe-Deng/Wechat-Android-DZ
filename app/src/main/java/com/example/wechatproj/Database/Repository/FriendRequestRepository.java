package com.example.wechatproj.Database.Repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.wechatproj.Database.DAO.FriendRequestDao;
import com.example.wechatproj.Database.Database.FriendRequestDatabase;
import com.example.wechatproj.Database.Entity.FriendRequest;

import java.util.List;

public class FriendRequestRepository {
    private LiveData<List<FriendRequest>> allFriendRequests;
    private LiveData<List<FriendRequest>> allNewFriendRequests;
    FriendRequestDao friendRequestDao;
    FriendRequestDatabase friendRequestDatabase;
    Context context;
    String myUsername;

    //两个get方法
    public LiveData<List<FriendRequest>> getAllFriendRequests() {
        return allFriendRequests;
    }

    public LiveData<List<FriendRequest>> getNewFriendRequests() {
//        new findNewFriendRequests(friendRequestDao).execute();
//        while (allNewFriendRequests.getValue() == null) {
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d("重点排查1", "livedataNewRq:" + allNewFriendRequests.getValue());
//        return allNewFriendRequests;
        return allNewFriendRequests;
    }

    //构造
    public FriendRequestRepository(Context context, String username) {
        this.context = context;
        this.myUsername = username;
        friendRequestDatabase = FriendRequestDatabase.getDatabase(context);
        friendRequestDao = friendRequestDatabase.getFrienRequestdDao();
        allFriendRequests = friendRequestDao.findAllFriendRequests();
        allNewFriendRequests = friendRequestDao.findAllNewFriendRequestsLive("unknow");
        Log.d("FriendResquestRepo", "仓库初始化完成 "+allFriendRequests.getValue());
    }

    //————————————————————方法区————————————
    public void insetFriendRequest(FriendRequest friendRequest) {
        new insetFriendRequestAsync(friendRequestDao).execute(friendRequest);
    }

    public void deleteFriendRequest(FriendRequest friendRequest) {
        new deleteFriendRequestAsync(friendRequestDao).execute(friendRequest);
    }

    public void deleteFriendRequestByUsername(String username) {
        new deleteFriendRequestByUsernameAsync(friendRequestDao).execute(username);
    }

    public void yesFriendRequest(String username) {
        new yesFriendRequestAsync(friendRequestDao).execute(username);
    }

    public void noFriendRequest(String username) {
        new noFriendRequestAsync(friendRequestDao).execute(username);
    }

    //根据用户名去查找该用户的请求(这里同步执行，不异步了）
    public FriendRequest findFriendRequestByUsername(String username) {
        FriendRequest foundFriendRequest=friendRequestDao.findFriendRequestByUsername(username);
//        Log.d("重点排查10", "findFriendRequestByUsername: "+foundFriendRequest);
        return foundFriendRequest;
    }


    //————————————————————异步调用区————————————
    //前面两个基本用不着
    static class insetFriendRequestAsync extends AsyncTask<FriendRequest, Void, Void> {
        FriendRequestDao friendRequestDao;

        public insetFriendRequestAsync(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(FriendRequest... friendRequests) {
            friendRequestDao.insertFriendRequest(friendRequests[0]);
            return null;
        }
    }

    static class deleteFriendRequestAsync extends AsyncTask<FriendRequest, Void, Void> {
        FriendRequestDao friendRequestDao;

        public deleteFriendRequestAsync(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(FriendRequest... friendRequests) {
            friendRequestDao.deleteFriendRequest(friendRequests[0]);
            return null;
        }
    }

    //这三个是主要的
    static class deleteFriendRequestByUsernameAsync extends AsyncTask<String, Void, Void> {
        FriendRequestDao friendRequestDao;

        public deleteFriendRequestByUsernameAsync(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            friendRequestDao.deleteFriendRequestByUsername(strings[0]);
            return null;
        }
    }

    static class yesFriendRequestAsync extends AsyncTask<String, Void, Void> {
        FriendRequestDao friendRequestDao;

        public yesFriendRequestAsync(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            friendRequestDao.yesFriendRequest(strings[0]);
            return null;
        }
    }

    static class noFriendRequestAsync extends AsyncTask<String, Void, Void> {
        FriendRequestDao friendRequestDao;

        public noFriendRequestAsync(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            friendRequestDao.noFriendRequest(strings[0]);
            return null;
        }
    }

    class findNewFriendRequests extends AsyncTask<Void, Void, Void> {
        FriendRequestDao friendRequestDao;

        public findNewFriendRequests(FriendRequestDao friendRequestDao) {
            this.friendRequestDao = friendRequestDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            allNewFriendRequests=friendRequestDao.findAllNewFriendRequestsLive("unknow");
            return null;
        }
    }
}
