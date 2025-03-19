package com.ibit.chat.util

import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * 配置加载工具类
 */
object ConfigLoader {
    private val properties = Properties()
    private var isLoaded = false

    /**
     * 加载配置文件
     * @return 是否加载成功
     */
    fun load(): Boolean {
        if (isLoaded) return true
        
        try {
            val propertiesFile = File("local.properties")
            if (!propertiesFile.exists()) {
                logger.error { "配置文件不存在: local.properties" }
                return false
            }
            
            FileInputStream(propertiesFile).use { fis -> 
                properties.load(fis)
            }
            
            isLoaded = true
            logger.debug { "成功加载配置文件" }
            return true
        } catch (e: Exception) {
            logger.error(e) { "加载配置文件失败" }
            return false
        }
    }

    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    fun getProperty(key: String, defaultValue: String = ""): String {
        if (!isLoaded && !load()) {
            return defaultValue
        }
        
        return properties.getProperty(key, defaultValue)
    }
    
    /**
     * 获取Badge值
     * @return Badge值
     */
    fun getBadge(): String {
        return getProperty("badge")
    }
    
    /**
     * 获取Cookie值
     * @return Cookie值
     */
    fun getCookie(): String {
        return getProperty("cookie")
    }
} 
