package com.darrenyuen.downloader;

/**
 * Create by yuan on 2021/1/7
 */
public interface Downloader {
    void download(String url, String fileName, DownloadListener listener);
}
