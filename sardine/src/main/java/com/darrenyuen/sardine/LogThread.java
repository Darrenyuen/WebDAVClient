package com.darrenyuen.sardine;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.darrenyuen.sardine.impl.OkHttpSardine;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Create by yuan on 2021/3/27
 */
public class LogThread implements Callable<Boolean> {

    private String TAG = "LogThread";

    public static AtomicLong LOCAL_FINISH_SIZE = new AtomicLong();
    public static AtomicLong DOWNLOAD_SIZE = new AtomicLong();
    public static AtomicLong DOWNLOAD_FINISH_THREAD = new AtomicLong();
    private long httpFileContentLength;

    private DownloadListener listener;

    public LogThread(long httpFileContentLength, DownloadListener listener) {
        this.httpFileContentLength = httpFileContentLength;
        this.listener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Boolean call() throws Exception {
        int[] downSizeArr = new int[5];
        int i = 0;
        double size = 0;
        double mb = 1024 * 1024;
        @SuppressLint("DefaultLocale")
        String httpFileSize = String.format("%.2f", httpFileContentLength / mb);
        while (DOWNLOAD_FINISH_THREAD.get() != OkHttpSardine.DOWNLOAD_THREAD_NUM) {
            double downloadSize = DOWNLOAD_SIZE.get();
            downSizeArr[++i % 5] = Double.valueOf(downloadSize - size).intValue();

            //每秒速度
            double fiveSecDownloadSize = Arrays.stream(downSizeArr).sum();
            int speed = (int) ((fiveSecDownloadSize / 1024d) / (Math.min(i, 5)));

            // 剩余时间
            double surplusSize = httpFileContentLength - downloadSize - LOCAL_FINISH_SIZE.get();
            @SuppressLint("DefaultLocale")
            String surplusTime = String.format("%.1f", surplusSize / 1024d / speed);
            if (surplusTime.equals("Infinity")) {
                surplusTime = "-";
            }
            // 已下大小
            @SuppressLint("DefaultLocale")
            String currentFileSize = String.format("%.2f", downloadSize / mb + LOCAL_FINISH_SIZE.get() / mb);
            String speedLog = String.format("> 已下载 %smb/%smb,速度 %skb/s,剩余时间 %ss", currentFileSize, httpFileSize, speed, surplusTime);
            listener.onProgress((float) (downloadSize / mb + LOCAL_FINISH_SIZE.get() / mb) / httpFileContentLength);
            Log.d(TAG, speedLog);
            Thread.sleep(1000);
        }
        return true;
    }
}
