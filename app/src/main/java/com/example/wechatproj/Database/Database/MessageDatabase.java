package com.example.wechatproj.Database.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.wechatproj.Database.DAO.MessageDao;
import com.example.wechatproj.Database.Entity.Message;

@Database(entities = {Message.class},version = 1,exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {
    private static MessageDatabase INSTANCE;
    public static synchronized MessageDatabase getDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MessageDatabase.class,"message_database")
//                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }

    public abstract MessageDao getMessageDao();

    //数据库升级
//    static final Migration MIGRATION_1_2 = new Migration(1,2) {
////        @Override
////        public void migrate(@NonNull SupportSQLiteDatabase database) {
////            database.execSQL("ALTER TABLE message ADD COLUMN foo_data INTEGER NOT NULL DEFAULT 1");
////        }
////    };
}
