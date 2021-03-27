package com.darrenyuen.sardine.util;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Create by yuan on 2021/3/27
 */
public class OkHttpUtil {
    /**
     *
     * @param url
     * @return 网络文件大小
     * @throws IOException
     */
    public static long getHttpFileContentLength(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        return client.newCall(new Request.Builder().url(url).build()).execute().body().contentLength();
    }

    public static Request getRequestWithRange(String url, long start, long end) {
        return new Request.Builder()
                .url(url)
                .addHeader("RANGE", "bytes=" + start + "-" + end)
                .build();
    }

}
