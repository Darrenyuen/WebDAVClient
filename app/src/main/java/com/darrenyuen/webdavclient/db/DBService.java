package com.darrenyuen.webdavclient.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.darrenyuen.webdavclient.bean.FileHashData;
import com.darrenyuen.webdavclient.bean.UserInfo;
import com.darrenyuen.webdavclient.WebDAVContext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Create by yuan on 2021/3/16
 */
public class DBService {

    private HashMap<String, Boolean> lockListHashMap = new HashMap<>();

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
//            int isLock = cursor.getInt(cursor.getColumnIndex("isLock"));
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
//        contentValues.put("isLock", 0);
        WebDAVContext.getDatabaseHelper(context)
                .getWritableDatabase()
                .insert("FileHashData", null, contentValues);
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().close();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void changeLockStatus(Context context, boolean isLock, String path) {
        if (lockListHashMap.containsKey(path)) {
            lockListHashMap.replace(path, isLock);
        } else lockListHashMap.put(path, isLock);
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("isLock", isLock);
//        WebDAVContext.getDatabaseHelper(context)
//                .getWritableDatabase()
//                .update("FileHashData", contentValues, "path=?", new String[]{path});
    }

    public boolean isLock(Context context, String path) {
        if (lockListHashMap.containsKey(path) && lockListHashMap.get(path)) return true;
        else return false;
//        int isLock = 0;
//        Cursor cursor = WebDAVContext.getDatabaseHelper(context).getReadableDatabase().rawQuery("select * from FileHashData where path = `" + path + "`", null);
//        while (cursor.moveToNext()) {
//            isLock = cursor.getInt(cursor.getColumnIndex("isLock"));
//        }
//        return isLock == 1;
    }
    public void clearAllData(Context context) {
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().delete("UserInfo", null, null);
        WebDAVContext.getDatabaseHelper(context).getWritableDatabase().delete("FileHashData", null, null);
        WebDAVContext.getDatabaseHelper(context).close();
    }

}
