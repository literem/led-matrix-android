package com.literam.matrix.common.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * author : literem
 * time   : 2022/11/12
 * desc   :
 * version: 1.0
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(@Nullable Context context) {
        super(context, "music.db", null, 1);
    }

    private static SQLiteHelper singleton;
    public static synchronized SQLiteHelper getInstance(Context context){
        if(singleton == null){
            synchronized (SQLiteHelper.class){
                if(singleton == null){
                    singleton = new SQLiteHelper(context);
                }
            }
        }
        return singleton;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String musicTable = "create table music(" +
                "id integer primary key not null," +
                "song_id integer unique," +
                "title varchar(50)," +
                "singer varchar(30)," +
                "album varchar(30)," +
                "length integer,"+
                "path varchar(50)," +
                "image varchar(50),"+
                "lyric text," +
                "sort integer," +
                "add_time varchar(20));";
        db.execSQL(musicTable);

        String playList = "create table play_list(" +
                "id integer primary key not null," +
                "music_id integer," +
                "title varchar(50)," +
                "singer varchar(30)," +
                "sort integer);";

        db.execSQL(playList);

        String createAccount = "create table lyric(lyric_id integer primary key," +
                "lyric_name varchar(50),lyric_content text,create_time datetime);";
        db.execSQL(createAccount);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
        //把tag表中的 sort_id 字段更名为 tag_sort
        db.execSQL("alter table tag rename to tagOld");
        String createExpendTag = "create table tag(tag_id integer primary key autoincrement," +
                "tag_name varchar(15) unique,tag_head varchar(2),tag_sort integer,tag_account integer,tag_type int1);";
        db.execSQL(createExpendTag);

        db.execSQL("insert into tag(tag_id,tag_name,tag_head,tag_sort,tag_account,tag_type) select tag_id,tag_name,tag_head,sort_id,pay_way,tag_type from tagOld");
        db.execSQL("drop table tagOld");*/
    }
}
