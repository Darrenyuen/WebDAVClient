package com.darrenyuen.downloader.ok;

/**
 * Create by yuan on 2020/12/10
 */
public interface DownloadListener {
    void onProgress(float progress);
    void onSuccess();
    void onFailure(String errMsg);
}
