package com.darrenyuen.downloader;

import com.darrenyuen.downloader.normal.NormalDownloader;
import com.darrenyuen.downloader.ok.OkDownloader;
/**
 * Create by yuan on 2021/1/3
 */
public class DownloaderFactory {

    public Downloader createDownloader(DownloaderType type) {
        if (type == DownloaderType.Normal_downloader) return new NormalDownloader();
        else return new OkDownloader();
    }
}
