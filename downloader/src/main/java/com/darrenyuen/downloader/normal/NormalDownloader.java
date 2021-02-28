package com.darrenyuen.downloader.normal;

import android.util.Log;

import com.darrenyuen.downloader.DownloadListener;
import com.darrenyuen.downloader.Downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Create by yuan on 2021/1/3
 */
public class NormalDownloader implements Downloader {

    private static String TAG = "NormalDownloader";

    //下载线程数量
    public static int DOWNLOAD_THREAD_NUM = 5;
    //临时文件后缀
    public static String FILE_TEMP_SUFFIX = ".temp";
    //下载线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_NUM);

    @Override
    public void download(String url, String fileName, DownloadListener listener) {
//        String fileName = HttpUtils.getHttpFileName(url);
        long localFileSize = FileUtils.getFileContentLength(fileName);
        long httpFileSize = HttpUtils.getHttpFileContentLength(url);
        if (localFileSize >= httpFileSize) {
            Log.i(TAG, "文件已存在，不需重复下载");
            listener.onSuccess();
            return;
        }
        List<Future<Boolean>> futureList = new ArrayList<>();
        if (localFileSize > 0) {
            Log.i(TAG, "开始断点续传");
        } else {
            Log.i(TAG, "开始下载文件");
        }
        try {
            splitDownload(url, futureList, fileName);
            LogThread logThread = new LogThread(httpFileSize);
            Future<Boolean> future = executor.submit(logThread);
            futureList.add(future);
            //开始下载
            for (Future<Boolean> booleanFuture : futureList) {
                booleanFuture.get();
            }
            boolean isMerged = merge(fileName);
            if (isMerged) {
                clearTemp(fileName);
            }
            listener.onSuccess();
            Log.i(TAG, "本次下载任务已完成");
        } catch (Exception e) {

        }
    }

    /**
     * 切分下载任务到多个线程
     * @param url
     * @param futureList
     * @throws IOException
     */
    public void splitDownload(String url, List<Future<Boolean>> futureList, String fileName) throws IOException {
        long httpFileContentLength = HttpUtils.getHttpFileContentLength(url);
        long size = httpFileContentLength / DOWNLOAD_THREAD_NUM;
        long lastSize = httpFileContentLength - (size * (DOWNLOAD_THREAD_NUM - 1));
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            long start = i * size;
            Long downloadWindow = (i == DOWNLOAD_THREAD_NUM - 1) ? lastSize : size;
            Long end = start + downloadWindow;
            if (start != 0) {
                start++;
            }
            DownloadThread downloadThread = new DownloadThread(url, fileName, start, end, i, httpFileContentLength);
            Future<Boolean> future = executor.submit(downloadThread);
            futureList.add(future);
        }
    }

    public boolean merge(String fileName) throws IOException {
        byte[] buffer = new byte[1024 * 10];
        int len = -1;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + FILE_TEMP_SUFFIX + i))) {
                    while ((len = bis.read(buffer)) != -1) {
                        randomAccessFile.write(buffer, 0, len);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean clearTemp(String fileName) {
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            File file = new File(fileName + FILE_TEMP_SUFFIX + i);
            file.delete();
        }
        return true;
    }

}
