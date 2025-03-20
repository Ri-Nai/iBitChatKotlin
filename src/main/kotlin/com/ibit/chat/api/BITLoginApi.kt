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
    private val bitLoginUrl = "https://login.bit.edu.cn/authserver/login?service=https%3A%2F%2Fibit.yanhekt.cn%2Fproxy%2Fv1%2Fcas%2Fcallback"
    private val iBitUrl = "https://ibit.yanhekt.cn"

    private val errorPattern = "<span id=\"showErrorTip\">(.*?)</span>".toRegex()

    /**
     * 从HTML中提取错误信息
     */
    fun getHtmlErrorReason(html: String): String {
        return errorPattern.find(html)?.groupValues?.get(1) ?: ""
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

        try {
            val request = HttpClient.buildRequest(bitLoginUrl, "POST", formBody)
            val response = HttpClient.client.newCall(request).execute()

            logger.info { "BIT登录成功: ${response.code}" }
            return@withContext true
        } catch (e: Exception) {
            logger.error { "BIT登录失败，错误原因: ${e.message}" }
            return@withContext false
        }

        

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
        val badgeCookie = cookies.find { it.name == "badge_2" }
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

        logger.info { "获取到ibit badge: $badge, cookieStr: $cookieStr" }
        return@withContext IBitChatClient(badge, cookieStr)
    }
} 
