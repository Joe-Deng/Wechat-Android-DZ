package com.example.wechatproj.Database.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.wechatproj.Database.DAO.FriendCircleDao;
import com.example.wechatproj.Database.Entity.FriendCircle;

@Database(entities = {FriendCircle.class},version = 1,exportSchema = false)
public abstract class FriendCircleDatabase extends RoomDatabase {
    private static FriendCircleDatabase INSTANCE;
    public static synchronized FriendCircleDatabase getDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),FriendCircleDatabase.class,"friendCircle_database")
                    .build();
        }
        return INSTANCE;
    }

    public abstract FriendCircleDao getFriendCircleDao();
}
