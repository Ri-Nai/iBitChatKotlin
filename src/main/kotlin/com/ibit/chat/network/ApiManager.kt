package com.ibit.chat.network

import mu.KotlinLogging
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * API管理器
 * 负责创建和管理网络请求客户端
 */
object ApiManager {
    private val logger = KotlinLogging.logger {}

    // Cookie管理器
    val bitCookieJar = CookieJarImpl()

    // HTTP客户端
    val bitClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cookieJar(bitCookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build().also {
            logger.info { "初始化OkHttpClient配置完成" }
        }

    // API实例
    val api by lazy {
        logger.debug { "创建API实例" }
        ApiFactory.create()
    }
}
