package com.example.wechatproj.Database.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.wechatproj.Database.DAO.FriendDao;
import com.example.wechatproj.Database.Entity.Friend;

@Database(entities = {Friend.class},version = 1,exportSchema = false)
public abstract class FriendDatabase extends RoomDatabase {
    private static FriendDatabase INSTANCE;
    public static synchronized FriendDatabase getDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),FriendDatabase.class,"friend_database")
//                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public abstract FriendDao getFriendDao();

    //数据库升级
//    static final Migration MIGRATION_1_2 = new Migration(1,2) {
////        @Override
////        public void migrate(@NonNull SupportSQLiteDatabase database) {
////            database.execSQL("ALTER TABLE friend ADD COLUMN foo_data INTEGER NOT NULL DEFAULT 1");
////        }
////    };
}
