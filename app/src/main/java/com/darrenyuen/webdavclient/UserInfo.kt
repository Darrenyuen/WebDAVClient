package com.darrenyuen.webdavclient

/**
 * Create by yuan on 2021/3/16
 */
data class UserInfo(val account: String, val password: String) {
    override fun toString(): String {
        return "account: $account, password: $password"
    }
}
