package com.darrenyuen.sardine;

/**
 * Create by yuan on 2021/3/25
 */
public interface DownloadListener {
    void onProgress(float progress);
    void onSuccess();
    void onFailure(String errMsg);
}
