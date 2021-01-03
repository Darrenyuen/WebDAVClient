package com.darrenyuen.downloader.normal;

import java.io.File;

/**
 * Create by yuan on 2021/1/3
 */
public class FileUtils {

    public static long getFileContentLength(String name) {
        File file =  new File(name);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
