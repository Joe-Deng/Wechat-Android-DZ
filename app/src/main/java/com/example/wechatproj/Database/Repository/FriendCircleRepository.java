package com.example.wechatproj.Database.Repository;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.wechatproj.Database.DAO.FriendCircleDao;
import com.example.wechatproj.Database.Database.FriendCircleDatabase;
import com.example.wechatproj.Database.Entity.FriendCircle;
import com.example.wechatproj.Database.Entity.Message;

import java.util.List;

public class FriendCircleRepository {
    String myUsername = null;
    LiveData<List<FriendCircle>> allFriendCirclesLive;
    FriendCircleDatabase friendCircleDatabase;
    FriendCircleDao friendCircleDao;
    FriendCircle friendCircle;
    Context context;
    Boolean ifFind = false;

    //构造，保证所有实体静态唯一
    public FriendCircleRepository(String myUsername, Context context) {
        this.myUsername = myUsername;
        this.context = context;
        friendCircleDatabase = FriendCircleDatabase.getDatabase(context);
        friendCircleDao = friendCircleDatabase.getFriendCircleDao();
        allFriendCirclesLive = friendCircleDao.getAllFriendCircleLive();
    }

    public LiveData<List<FriendCircle>> getAllFriendCirclesLive() {
        return allFriendCirclesLive;
    }

    //    ————————————————————————————
//    方法区
    public void insertFriendCircle(FriendCircle...friendCircles){
        new InsertAsyncTask(friendCircleDao).execute(friendCircles);
    }

    public void updateFriendCircle(FriendCircle...friendCircles){
        new UpdateAsyncTask(friendCircleDao).execute(friendCircles);
    }

    public void deleteFriendCircle(FriendCircle...friendCircles){
        new DeleteAsyncTask(friendCircleDao).execute(friendCircles);
    }

    public FriendCircle findFriendCircleBySID(String SID){
        new FindFriendCircleBySIDAsync(SID,friendCircleDao).start();
        ifFind = false;
        while (ifFind == false){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return friendCircle;
    }

    public FriendCircle findFriendCircleByTime(Long M_Time){
        new FindFriendCircleByTimeAsync(M_Time,friendCircleDao).start();
        ifFind = false;
        while (ifFind == false){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return friendCircle;
    }





    //    ______________________________________________
//    异步任务区
    static class InsertAsyncTask extends AsyncTask<FriendCircle,Void,Void> {
        private FriendCircleDao friendCircleDao;

        public InsertAsyncTask(FriendCircleDao friendCircleDao) {
            this.friendCircleDao = friendCircleDao;
        }

        @Override
        protected Void doInBackground(FriendCircle... friendCircles) {
            Log.d("重点排查42", "M_Time: "+friendCircles[0].getM_Time());
            friendCircleDao.insertFriendCircle(friendCircles[0]);
            return null;
        }
    }

    static class UpdateAsyncTask extends AsyncTask<FriendCircle,Void,Void> {
        private FriendCircleDao friendCircleDao;

        public UpdateAsyncTask(FriendCircleDao friendCircleDao) {
            this.friendCircleDao = friendCircleDao;
        }

        @Override
        protected Void doInBackground(FriendCircle... friendCircles) {
            friendCircleDao.updateFriendCircle(friendCircles[0]);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<FriendCircle,Void,Void> {
        private FriendCircleDao friendCircleDao;

        public DeleteAsyncTask(FriendCircleDao friendCircleDao) {
            this.friendCircleDao = friendCircleDao;
        }

        @Override
        protected Void doInBackground(FriendCircle... friendCircles) {
            friendCircleDao.deleteFriendCircle(friendCircles[0]);
            return null;
        }
    }

    public class FindFriendCircleBySIDAsync extends Thread implements Runnable{
        String SID;
        FriendCircleDao friendCircleDao;

        public FindFriendCircleBySIDAsync(String SID, FriendCircleDao friendCircleDao) {
            this.SID = SID;
            this.friendCircleDao = friendCircleDao;
        }

        @Override
        public void run() {
            super.run();
            friendCircle=friendCircleDao.getFriendCircleBySID(SID);
        }
    }

    public class FindFriendCircleByTimeAsync extends Thread implements Runnable{
        Long M_Time;
        FriendCircleDao friendCircleDao;

        public FindFriendCircleByTimeAsync(Long M_Time, FriendCircleDao friendCircleDao) {
            this.M_Time = M_Time;
            this.friendCircleDao = friendCircleDao;
            ifFind = true;
        }

        @Override
        public void run() {
            super.run();
            friendCircle=friendCircleDao.getFriendCircleByTime(M_Time);
            ifFind = true;
        }
    }
}
