package com.ibit.chat.config

import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * 配置加载器，用于从local.properties文件中加载配置
 */
object ConfigLoader {
    private val properties = Properties()
    private var isLoaded = false
    
    private const val LOCAL_PROPERTIES = "local.properties"
    private const val BADGE_KEY = "badge"
    private const val COOKIE_KEY = "cookie"
    private const val USERNAME_KEY = "username"
    private const val PASSWORD_KEY = "password"
    
    /**
     * 加载配置
     * @return 是否加载成功
     */
    fun load(): Boolean {
        if (isLoaded) return true
        
        val file = File(LOCAL_PROPERTIES)
        if (!file.exists()) {
            logger.info { "配置文件不存在: $LOCAL_PROPERTIES" }
            return false
        }
        
        return try {
            FileInputStream(file).use { fis ->
                properties.load(fis)
            }
            isLoaded = true
            logger.info { "配置文件加载成功: $LOCAL_PROPERTIES" }
            true
        } catch (e: Exception) {
            logger.error(e) { "加载配置文件出错: $LOCAL_PROPERTIES" }
            false
        }
    }
    
    /**
     * 获取badge值
     * @return badge值，如果不存在则返回空字符串
     */
    fun getBadge(): String {
        return properties.getProperty(BADGE_KEY, "")
    }
    
    /**
     * 获取cookie值
     * @return cookie值，如果不存在则返回空字符串
     */
    fun getCookie(): String {
        return properties.getProperty(COOKIE_KEY, "")
    }
    
    /**
     * 获取用户名
     * @return 用户名，如果不存在则返回空字符串
     */
    fun getUsername(): String {
        return properties.getProperty(USERNAME_KEY, "")
    }
    
    /**
     * 获取密码
     * @return 密码，如果不存在则返回空字符串
     */
    fun getPassword(): String {
        return properties.getProperty(PASSWORD_KEY, "")
    }
    
    /**
     * 保存配置
     * @param badge badge值
     * @param cookie cookie值
     * @param username 用户名
     * @param password 密码
     * @return 是否保存成功
     */
    fun save(badge: String, cookie: String, username: String, password: String): Boolean {
        properties.setProperty(BADGE_KEY, badge)
        properties.setProperty(COOKIE_KEY, cookie)
        properties.setProperty(USERNAME_KEY, username)
        properties.setProperty(PASSWORD_KEY, password)
        
        val file = File(LOCAL_PROPERTIES)
        
        return try {
            file.outputStream().use { fos ->
                properties.store(fos, "iBitChat配置")
            }
            logger.info { "配置文件保存成功: $LOCAL_PROPERTIES" }
            true
        } catch (e: Exception) {
            logger.error(e) { "保存配置文件出错: $LOCAL_PROPERTIES" }
            false
        }
    }
} 
