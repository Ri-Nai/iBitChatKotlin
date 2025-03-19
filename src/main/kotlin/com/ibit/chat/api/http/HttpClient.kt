package com.ibit.chat.api.http

import mu.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private val logger = KotlinLogging.logger {}

object HttpClient {
    val defaultHeaders = mapOf(
        "Referer" to "https://login.bit.edu.cn/authserver/login",
        "Host" to "login.bit.edu.cn",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:103.0) Gecko/20100101 Firefox/103.0"
    )

    val client = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val host = url.host
                cookieStore[host] = cookies
                logger.debug { "保存Cookie: $host -> ${cookies.joinToString(", ") { it.name + "=" + it.value }}" }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val host = url.host
                val cookies = cookieStore[host] ?: emptyList()
                logger.debug { "加载Cookie: $host -> ${cookies.joinToString(", ") { it.name + "=" + it.value }}" }
                return cookies
            }
        })
        .build()

    fun buildRequest(url: String, method: String = "GET", body: RequestBody? = null): Request {
        val builder = Request.Builder()
            .url(url)

        // 添加默认请求头
        defaultHeaders.forEach { (key, value) -> 
            builder.addHeader(key, value)
        }

        // 添加Cookie头
        val cookieStr = client.cookieJar.loadForRequest(url.toHttpUrlOrNull()!!)
            .joinToString("; ") { "${it.name}=${it.value}" }
        if (cookieStr.isNotEmpty()) {
            builder.addHeader("Cookie", cookieStr)
            logger.debug { "添加Cookie头: $cookieStr" }
        }

        when (method.uppercase()) {
            "POST" -> builder.post(body ?: FormBody.Builder().build())
            else -> builder.get()
        }

        return builder.build()
    }
} 
