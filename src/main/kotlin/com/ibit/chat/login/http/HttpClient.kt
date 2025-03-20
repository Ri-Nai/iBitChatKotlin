package com.ibit.chat.login.http

import mu.KotlinLogging
import okhttp3.*
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * HTTP客户端工具类
 */
object HttpClient {
    // 默认请求头
    val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    )

    // CookieJar实例
    private val cookieJar = CookieJarImpl()

    // 创建OkHttpClient实例
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 构建请求
     */
    fun buildRequest(
        url: String,
        method: String = "GET",
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap()
    ): Request {
        val requestBuilder = Request.Builder()
            .url(url)

        // 添加默认请求头
        defaultHeaders.forEach { (name, value) ->
            requestBuilder.addHeader(name, value)
        }

        // 添加自定义请求头
        headers.forEach { (name, value) ->
            requestBuilder.addHeader(name, value)
        }

        // 根据不同的HTTP方法设置请求体
        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> requestBuilder.post(body ?: FormBody.Builder().build())
            "PUT" -> requestBuilder.put(body ?: FormBody.Builder().build())
            "DELETE" -> requestBuilder.delete(body)
            else -> throw IllegalArgumentException("不支持的HTTP方法: $method")
        }

        return requestBuilder.build()
    }

    /**
     * 获取CookieJar实例
     */
    fun getCookieJar(): CookieJarImpl = cookieJar
} 
