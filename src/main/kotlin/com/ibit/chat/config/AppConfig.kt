package com.ibit.chat.config


import mu.KotlinLogging

/**
 * 应用全局配置
 * 统一管理应用配置信息
 */
object AppConfig {
    private val logger = KotlinLogging.logger {}

    // 默认聊天模型
    val model = "iBit"

    // iBit配置
    val ibitConfig = IBitConfig()

    val loginConfig = LoginConfig()
}

