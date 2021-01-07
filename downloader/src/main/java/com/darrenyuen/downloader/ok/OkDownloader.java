package com.darrenyuen.downloader.ok;

import com.darrenyuen.downloader.DownloadListener;
import com.darrenyuen.downloader.Downloader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Create by yuan on 2020/12/10
 */
public class OkDownloader implements Downloader {

    private static OkHttpClient client = new OkHttpClient.Builder().build();
    private static DownloadListener mListener;

    public void download(String url, String destPath, DownloadListener listener) {
        mListener = listener;
        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mListener.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (ensureFile(destPath)) writeToDisk(new File(destPath), response);
            }
        });
    }

    private static void writeToDisk(File file, Response response) throws IOException {
        long contentLength = Objects.requireNonNull(response.body()).contentLength();
        BufferedSource source = Objects.requireNonNull(response.body()).source();
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        Buffer sinkBuffer = sink.buffer();
        long totalRead = 0;
        int bufferSize = 8 * 1024;
        long bytesRead;
        while ((bytesRead = source.read(sinkBuffer, bufferSize)) != -1) {
            sink.emit();
            totalRead += bytesRead;
            mListener.onProgress((float) (totalRead * 100.0 / contentLength));
        }
        sink.flush();
        if (file.length() == contentLength) mListener.onSuccess();
        else mListener.onFailure("Write to disk error");
    }

    private static boolean ensureFile(String destPath) {
        File file = new File(destPath);
        if (file.exists()) return true;
        else {
            try {
//                if (file.getParentFile() == null || !file.getParentFile().exists()) file.getParentFile().mkdirs();
                File parentFile = new File(file.getParent());
                parentFile.mkdirs();
//                if (file.getParentFile() == null || !file.getParentFile().exists()) file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                mListener.onFailure("Create File Error");
                return false;
            }
        }
        return file.exists();
    }

//    public static class Builder {
//
//        String mUrl;
//
//        long mCallTimeOut = 5 * 60;
//
//        public Builder url(String url) {
//            mUrl = url;
//            return this;
//        }
//
//        public Builder callTimeOut(long callTimeOut) {
//            mCallTimeOut = callTimeOut;
//            return this;
//        }
//
//        public OkDownloader build() {
//            return new OkDownloader();
//        }
//    }
}
