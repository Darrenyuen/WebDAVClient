package com.darrenyuen.webdavclient;

import android.content.Context;
import android.database.Cursor;

/**
 * Create by yuan on 2021/3/16
 */
public class DBService {
    public UserInfo getUserInfo(Context context) {
        UserInfo userInfo = null;
        Cursor cursor = WebDAVContext.getDatabaseHelper(context).getReadableDatabase()
                .rawQuery("select * from UserInfo", null);
        while (cursor.moveToNext()) {
            String account  = cursor.getString(cursor.getColumnIndex("account"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            userInfo = new UserInfo(account, password);
        }
        cursor.close();
        WebDAVContext.getDatabaseHelper(context).getReadableDatabase().close();
        return userInfo;
    }
}
