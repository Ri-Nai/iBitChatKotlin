package com.ibit.chat.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Cookie管理器实现，用于OkHttp客户端
 */
class CookieJarImpl : CookieJar {
    private val TAG = "CookieJarImpl"

    // 使用ConcurrentHashMap存储不同域名下的Cookie
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    // SharedPreference名称
    private val PREFS_NAME = "cookie_preferences"
    private val COOKIE_STORE_KEY = "cookie_store"


    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host


        // 获取当前域名的Cookie列表，如果不存在则创建新列表
        val domainCookies = cookieStore.getOrPut(host) { mutableListOf() }

        for (cookie in cookies) {
            // 查找同名cookie的索引
            val index = domainCookies.indexOfFirst { it.name == cookie.name }
            if (index != -1) {
                // 更新已存在的cookie
                domainCookies[index] = cookie
            } else {
                // 添加新cookie
                domainCookies.add(cookie)
            }
        }

        // 移除过期的cookie
        domainCookies.removeAll { it.expiresAt < System.currentTimeMillis() }


    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        // 获取当前域名的Cookie列表
        val cookies = cookieStore[host] ?: emptyList()

        return cookies
    }

    /**
     * 获取指定域名下的所有Cookie
     */
    fun getCookies(host: String): List<Cookie> {
        return cookieStore[host] ?: emptyList()
    }

}
