package com.darrenyuen.webdavclient

import java.util.*

/**
 * Create by yuan on 2021/3/1
 */
data class FileBean(var path: String, var name: String, var lastModified: Date, var size: Long) {
    override fun toString(): String {
        return "path: $path, name: $name, lastModified: $lastModified, size: $size"
    }
}
