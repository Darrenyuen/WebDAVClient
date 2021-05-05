package com.darrenyuen.webdavclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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

    public List<FileHashData> getFileHashData(Context context) {
        List<FileHashData> ret = new LinkedList<>();
        Cursor cursor = WebDAVContext.getDatabaseHelper(context).getReadableDatabase().rawQuery("select * from FileHashData", null);
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndex("path"));
            String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            FileHashData fileHashData = new FileHashData(filePath, fileName);
            ret.add(fileHashData);
        }
        cursor.close();
        WebDAVContext.getDatabaseHelper(context).getReadableDatabase().close();
        return ret;
    }

    public void writeFileHashData(Context context, String path, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", path);
        contentValues.put("fileName", fileName);
        WebDAVContext.getDatabaseHelper(context)
                .getWritableDatabase()
                .insert("FileHashData", null, contentValues);
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().close();
    }

    public void clearAllData(Context context) {
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().delete("UserInfo", null, null);
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().delete("FileHashData", null, null);
        WebDAVContext.getDatabaseHelper(context).close();
    }

}
