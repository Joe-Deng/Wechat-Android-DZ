package com.example.wechatproj.Database.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.wechatproj.Database.DAO.FriendDao;
import com.example.wechatproj.Database.DAO.FriendRequestDao;
import com.example.wechatproj.Database.Entity.Friend;
import com.example.wechatproj.Database.Entity.FriendRequest;

@Database(entities = {FriendRequest.class},version = 1,exportSchema = false)
public abstract class FriendRequestDatabase extends RoomDatabase {
    private static FriendRequestDatabase INSTANCE;
    public static synchronized FriendRequestDatabase getDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), FriendRequestDatabase.class,"friendRequest_database")
//                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public abstract FriendRequestDao getFrienRequestdDao();

    //数据库升级
//    static final Migration MIGRATION_1_2 = new Migration(1,2) {
////        @Override
////        public void migrate(@NonNull SupportSQLiteDatabase database) {
////            database.execSQL("ALTER TABLE friend ADD COLUMN foo_data INTEGER NOT NULL DEFAULT 1");
////        }
////    };
}
