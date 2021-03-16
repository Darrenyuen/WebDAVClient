package com.darrenyuen.webdavclient;

import android.content.Context;

/**
 * Create by yuan on 2021/3/14
 */
public final class WebDAVContext {

    private final static SingleTon<DBService, Void> mDBService = new SingleTon<DBService, Void>() {
        @Override
        protected DBService create(Void aVoid) {
            return new DBService();
        }
    };

    public static DBService getDBService() {
        return mDBService.getInstance(null);
    }

    private final static SingleTon<DatabaseHelper, Context> mDataBaseHelper = new SingleTon<DatabaseHelper, Context>() {
        @Override
        protected DatabaseHelper create(Context context) {
            return new DatabaseHelper(context, "info.db", null, 1);
        }
    };

    public static DatabaseHelper getDatabaseHelper(Context context) {
        return mDataBaseHelper.getInstance(context);
    }
}
