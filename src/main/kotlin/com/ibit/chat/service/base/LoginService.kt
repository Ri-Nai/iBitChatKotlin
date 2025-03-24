package com.ibit.chat.service.base


import com.ibit.chat.network.ApiManager
import com.ibit.chat.service.util.AESUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jsoup.Jsoup

private val logger = KotlinLogging.logger {}

/**
 * 北京理工大学ibit平台登录服务
 */
class LoginService {
    private val TAG = "LoginService"
    private val errorPattern = "<span id=\"showErrorTip\">(.*?)</span>".toRegex()

    // 使用ApiManager获取API实例
    private val loginApi = ApiManager.api.loginApi

    /**
     * 从HTML中提取错误信息
     */
    private fun getHtmlErrorReason(html: String): String {
        return errorPattern.find(html)?.groupValues?.get(1) ?: ""
    }

    /**
     * 获取登录页面参数
     */
    private suspend fun getLoginParams(): Pair<String, String> = withContext(Dispatchers.IO) {
        val response = loginApi.getLoginPage().execute()

        if (!response.isSuccessful) {
            throw RuntimeException("获取登录页面失败: ${response.code()}")
        }



        val html = response.body()?.string() ?: throw RuntimeException("获取登录页面失败: 无法获取响应体")
        val document = Jsoup.parse(html)

        val form = document.getElementById("pwdFromId") ?: throw RuntimeException("找不到登录表单")
        val execution = form.getElementsByAttributeValue("name", "execution").first()?.attr("value")
            ?: throw RuntimeException("无法获取execution参数")
        val pwdEncryptSalt = document.getElementById("pwdEncryptSalt")?.attr("value")
            ?: throw RuntimeException("无法获取pwdEncryptSalt参数")

        return@withContext Pair(execution, pwdEncryptSalt)
    }

    /**
     * 登录北京理工大学统一身份认证系统
     */
    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val (execution, pwdEncryptSalt) = getLoginParams()
        val encryptedPassword = AESUtils.encryptPassword(password, pwdEncryptSalt)

        try {
            val response = loginApi.login(
                username = username,
                password = encryptedPassword,
                execution = execution,
            ).execute()
            logger.debug { "BIT登录响应: ${response.code()}" }
            if (!response.isSuccessful) {
                val html = response.body()?.string() ?: ""
                val reason = getHtmlErrorReason(html)
                logger.error { "BIT登录失败: ${response.code()}, 原因: $reason" }
                throw RuntimeException("BIT登录失败: ${response.code()}, 原因: $reason")
            }
            try {
                verifySession()
            } catch (e: Exception) {
                logger.error { "BIT登录失败，错误原因: ${e.message}" }
                throw RuntimeException("BIT登录失败，错误原因: ${e.message}")
            }
            return@withContext true
        } catch (e: Exception) {
            logger.error { "BIT登录失败，错误原因: ${e.message}" }
            throw RuntimeException("BIT登录失败，错误原因: ${e.message}")
        }
    }

    suspend fun verifySession(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = loginApi.getLoginPage().execute()
            if (!response.isSuccessful) {
                logger.error { "获取登录页面失败: ${response.code()}" }
                return@withContext false
            }

            // 若存在密码输入框，则认为未登录
            val html = response.body()?.string() ?: throw RuntimeException("无法获取登录页面")
            val document = Jsoup.parse(html)
            val pwdForm = document.getElementById("pwdFromId")

            val isLoggedIn = pwdForm == null
            if (!isLoggedIn) {
                logger.error { "会话验证失败: 未登录" }
                throw RuntimeException("会话验证失败: 未登录")
            }
            return@withContext true
        } catch (e: Exception) {
            logger.error { "会话验证失败: ${e.message}" }
            throw e  // 重新抛出异常，确保登录流程能感知到验证失败
        }
    }
} 
