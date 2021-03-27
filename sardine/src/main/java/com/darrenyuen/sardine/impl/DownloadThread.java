package com.darrenyuen.sardine.impl;

import android.util.Log;

import com.darrenyuen.sardine.LogThread;
import com.darrenyuen.sardine.util.FileUtil;
import com.darrenyuen.sardine.util.OkHttpUtil;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;

/**
 * Create by yuan on 2021/3/27
 */
public class DownloadThread implements Callable<Boolean> {

    private String TAG = "DownloadThread";

    /**
     * 每次读取的数据块大小
     */
    private static int BYTE_SIZE = 1024 * 100;
    /**
     * 下载链接
     */
    private String url;
    /**
     * 目标存储路径
     */
    private String fileName;
    /**
     * 下载开始位置
     */
    private long startPos;
    /**
     * 要下载的文件区块大小
     */
    private Long endPos;
    /**
     * 标识多线程下载切分的第几部分
     */
    private Integer part;
    /**
     * 文件总大小
     */
    private Long contentLength;

    public DownloadThread(String url, String fileName, long start, Long endPos, Integer partFlag, Long contentLength) {
        this.url = url;
        this.startPos = start;
        this.endPos = endPos;
        this.part = partFlag;
        this.contentLength = contentLength;
        this.fileName = fileName;
    }

    @Override
    public Boolean call() throws Exception {
        if (url == null || url.isEmpty()) {
            throw  new RuntimeException("下载链接出错");
        }

        //文件名
//        String httpFileName = HttpUtils.getHttpFileName(url);
        String httpFileName = "";
        if (part != null) {
            httpFileName = fileName + OkHttpSardine.FILE_TEMP_SUFFIX + part;
        }

        //本地文件大小
        Long localFileSize = FileUtil.getFileContentLength(httpFileName);
        LogThread.LOCAL_FINISH_SIZE.addAndGet(localFileSize);
        if (localFileSize >= endPos - startPos) {
            Log.i(TAG, httpFileName + "已下载完毕，无需重复下载");
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
            return true;
        }

        if (endPos.equals(contentLength)) {
            endPos = null;
        }


//        HttpURLConnection httpURLConnection = OkHttpUtil.getHttpUrlConnection(url, startPos + localFileSize, endPos);
        try(InputStream inputStream = new OkHttpClient.Builder().build().newCall(OkHttpUtil.getRequestWithRange(url, startPos + localFileSize, endPos)).execute().body().byteStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            RandomAccessFile randomAccessFile = new RandomAccessFile(httpFileName, "rw")) {
            byte[] buffer = new byte[BYTE_SIZE];
            int len = -1;
            while ((len = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, len);
                LogThread.DOWNLOAD_SIZE.addAndGet(len);
            }
        } catch (FileNotFoundException e) {
            Log.i(TAG, "下载路径不存在");
            return false;
        } catch (Exception e) {
            Log.i(TAG, "下载出现异常");
        } finally {
//            httpURLConnection.disconnect();
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
        }

        return true;
    }
}