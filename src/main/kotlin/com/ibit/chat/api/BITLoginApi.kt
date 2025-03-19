package com.ibit.chat.api

import com.ibit.chat.api.http.HttpClient
import com.ibit.chat.api.util.AESUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jsoup.Jsoup

private val logger = KotlinLogging.logger {}

/**
 * 北京理工大学统一身份认证登录API
 */
class BITLoginApi {
    private val bitLoginUrl = "https://login.bit.edu.cn/authserver/login"
    private val iBitUrl = "https://ibit.yanhekt.cn"

    /**
     * 从HTML中提取错误信息
     */
    private fun getHtmlErrorReason(html: String): String {
        val findKey = "<span id=\"showErrorTip\">"
        val start = html.indexOf(findKey)
        if (start == -1) return ""
        
        var count = 0
        var i = start
        do {
            if (i + 1 < html.length && html[i] == '<' && html[i + 1] == 's') count++
            if (i + 1 < html.length && html[i] == '<' && html[i + 1] == '/') count--
            i++
        } while (count > 0 && i < html.length)
        
        return html.substring(start + findKey.length, i - 1 - findKey.length)
    }

    /**
     * 获取登录页面参数
     */
    private suspend fun getLoginParams(): Triple<String, String, List<String>> = withContext(Dispatchers.IO) {
        val request = HttpClient.buildRequest(bitLoginUrl)
        val response = HttpClient.client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw RuntimeException("获取登录页面失败: ${response.code}")
        }

        val cookiesFromHeader = response.headers("Set-Cookie")
        logger.debug { "获取到Set-Cookie头: $cookiesFromHeader" }

        val html = response.body?.string() ?: throw RuntimeException("登录页面内容为空")
        val document = Jsoup.parse(html)

        val form = document.getElementById("pwdFromId") ?: throw RuntimeException("找不到登录表单")
        val execution = form.getElementsByAttributeValue("name", "execution").first()?.attr("value")
            ?: throw RuntimeException("无法获取execution参数")
        val pwdEncryptSalt = document.getElementById("pwdEncryptSalt")?.attr("value")
            ?: throw RuntimeException("无法获取pwdEncryptSalt参数")

        logger.debug { "获取到登录参数: execution=$execution, pwdEncryptSalt=$pwdEncryptSalt" }
        return@withContext Triple(execution, pwdEncryptSalt, cookiesFromHeader)
    }

    /**
     * 登录北京理工大学统一身份认证系统
     */
    private suspend fun loginBIT(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val (execution, pwdEncryptSalt, _) = getLoginParams()
        val encryptedPassword = AESUtils.encryptPassword(password, pwdEncryptSalt)
        
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", encryptedPassword)
            .add("execution", execution)
            .add("_eventId", "submit")
            .add("cllt", "userNameLogin")
            .add("dllt", "generalLogin")
            .add("lt", "")
            .add("rememberMe", "true")
            .build()

        val request = HttpClient.buildRequest(bitLoginUrl, "POST", formBody)
        var response = HttpClient.client.newCall(request).execute()

        if (response.code == 302) {
            logger.info { "BIT登录成功：收到302重定向" }
            return@withContext true
        }

        val html = response.body?.string() ?: ""
        val errorReason = getHtmlErrorReason(html)
        logger.error { "BIT登录失败，错误原因: $errorReason" }
        
        return@withContext false
    }

    /**
     * 获取ibit.yanhekt.cn的badge cookie
     */
    private suspend fun getIBitBadge(): Pair<String, String> = withContext(Dispatchers.IO) {
        val request = HttpClient.buildRequest(iBitUrl)
        var response = HttpClient.client.newCall(request).execute()
        
        if (response.code == 302 || response.code == 301) {
            val redirectUrl = response.header("Location")
            if (redirectUrl != null && redirectUrl.contains("badgeFromPc=")) {
                val badgeFromPc = redirectUrl.substringAfter("badgeFromPc=").substringBefore("&")
                val cookieStr = "badgeFromPc=$badgeFromPc"
                
                val redirectRequest = HttpClient.buildRequest(redirectUrl)
                HttpClient.client.newCall(redirectRequest).execute()
                
                return@withContext Pair(badgeFromPc, cookieStr)
            }
        }

        if (!response.isSuccessful) {
            throw RuntimeException("访问ibit网站失败: ${response.code}")
        }

        val cookies = HttpClient.client.cookieJar.loadForRequest(iBitUrl.toHttpUrlOrNull()!!)
        val badgeCookie = cookies.find { it.name == "badge" }
            ?: throw RuntimeException("未找到badge cookie")

        val cookieStr = cookies.joinToString("; ") { "${it.name}=${it.value}" }
        logger.info { "成功获取ibit badge: ${badgeCookie.value}" }
        
        return@withContext Pair(badgeCookie.value, cookieStr)
    }

    /**
     * 执行登录流程并获取IBitChatClient实例
     */
    suspend fun login(username: String, password: String): IBitChatClient = withContext(Dispatchers.IO) {
        if (!loginBIT(username, password)) {
            throw RuntimeException("BIT统一身份认证登录失败")
        }

        val (badge, cookieStr) = getIBitBadge()
        return@withContext IBitChatClient(badge, cookieStr)
    }
} 
