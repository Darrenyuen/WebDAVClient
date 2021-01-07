package com.darrenyuen.downloader.normal;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Create by yuan on 2021/1/3
 */
public class HttpUtils {

    /**
     *
     * @param url
     * @return HTTP链接
     * @throws IOException
     */
    public static HttpURLConnection getHttpUrlConnection(String url) {
        HttpURLConnection httpURLConnection = null;
        try {
            URL httpUrl = new URL(url);
            httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
            return httpURLConnection;
        } catch (Exception e) {

        }
        return  httpURLConnection;
    }

    /**
     *
     * @param url
     * @param start
     * @param end
     * @return HTTP链接
     * @throws IOException
     */
    public static HttpURLConnection getHttpUrlConnection(String url, long start, Long end) throws IOException {
        HttpURLConnection httpURLConnection = getHttpUrlConnection(url);
        if (end != null) {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
        } else {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-");
        }
        return httpURLConnection;
    }

    /**
     *
     * @param url
     * @return 网络文件大小
     * @throws IOException
     */
    public static long getHttpFileContentLength(String url) {
        HttpURLConnection httpURLConnection =getHttpUrlConnection(url);
        int contentLength = httpURLConnection.getContentLength();
        httpURLConnection.disconnect();
        return contentLength;
    }

    /**
     *
     * @param url
     * @return 网络文件ETag
     * @throws IOException
     */
    public static String getHttpFileETag(String url) throws IOException {
        HttpURLConnection httpURLConnection = getHttpUrlConnection(url);
        Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
        List<String> eTagList = headerFields.get("ETag");
        httpURLConnection.disconnect();
        return eTagList.get(0);
    }

    /**
     *
     * @param url
     * @return 网络文件名
     */
    public static String getHttpFileName(String url) {
        int indexOf = url.lastIndexOf("/");
        return url.substring(indexOf + 1);
    }
}
