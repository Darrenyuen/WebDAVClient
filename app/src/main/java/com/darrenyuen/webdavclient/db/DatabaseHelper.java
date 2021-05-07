package com.darrenyuen.webdavclient.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Create by yuan on 2021/3/14
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserInfoTable = "create table UserInfo(account varchar(64), password varchar(64))";
        String createHashData = "create table FileHashData(path varchar(64), fileName varchar(64), isLock int)"; //isLock: 0 没被锁，1 被锁定
        db.execSQL(createUserInfoTable);
        db.execSQL(createHashData);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
