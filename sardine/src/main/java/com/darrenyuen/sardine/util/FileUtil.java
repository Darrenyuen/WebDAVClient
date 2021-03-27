package com.darrenyuen.sardine.util;

import java.io.File;

/**
 * Create by yuan on 2021/3/25
 */
public class FileUtil {
    public static long getFileContentLength(String name) {
        File file =  new File(name);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
