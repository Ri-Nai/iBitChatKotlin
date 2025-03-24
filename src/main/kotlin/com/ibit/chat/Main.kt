package com.ibit.chat

import com.ibit.chat.model.Message
import com.ibit.chat.service.ServiceManager
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.util.*

// 创建日志记录器
private val logger = KotlinLogging.logger {}

/**
 * 主函数，用于测试iBit聊天客户端
 */
fun main() = runBlocking {
    logger.info { "启动iBitChat Kotlin客户端" }
    val propertiesFile = File("local.properties")
    val properties = loadProperties(propertiesFile)

    // 检查是否需要初始化配置
    if (!isConfigValid(properties)) {
        initializeConfig(properties, propertiesFile)
    }

    // 获取用户名和密码
    val username = properties.getProperty("username")
    val password = properties.getProperty("password")

    // 执行登录
    performLogin(username, password)

    var messages = listOf<Message>()
    while (true) {
        val message = readln("请输入消息（输入exit退出）： ")
        if (message == "exit") {
            break
        }
        messages += Message(role = "user", content = message)
        var totalContent = ""
        ServiceManager.iBitService.streamChat(messages).collect { result ->
            result.onSuccess {
                totalContent += it
                print(it)
            }
            result.onFailure {
                logger.error { "聊天失败: ${it.message}" }
            }
        }
        messages += Message(role = "assistant", content = totalContent)
    }
    logger.info { "聊天客户端退出" }
    println("感谢使用iBitChat Kotlin Demo!")
}


fun readln(prompt: String): String {
    print(prompt)
    return readlnOrNull() ?: ""
}

/**
 * 加载配置文件中的属性
 */
private fun loadProperties(file: File): Properties {
    return if (file.exists()) {
        Properties().apply { load(file.inputStream()) }
    } else {
        logger.info { "配置文件不存在，将创建新文件" }
        Properties()
    }
}

/**
 * 检查配置是否有效（包含 username 和 password）
 */
private fun isConfigValid(properties: Properties): Boolean {
    return properties.containsKey("username") && properties.containsKey("password")
}

/**
 * 初始化配置（用户输入并保存到文件）
 */
private fun initializeConfig(properties: Properties, file: File) {
    logger.info { "未在配置中找到 username 和 password，开始初始化配置" }
    val username = readln("请输入 username: ")
    val password = readln("请输入 password: ")

    properties.setProperty("username", username)
    properties.setProperty("password", password)

    // 保存配置到文件
    file.outputStream().use { properties.store(it, "Configuration for login") }
    logger.info { "配置已保存: username=$username, password=$password" }
}

/**
 * 执行登录操作
 */
private suspend fun performLogin(username: String, password: String) {
    logger.info { "尝试登录: username=$username, password=$password" }
    try {
        ServiceManager.loginService.login(username, password)
        logger.info { "登录成功" }
    } catch (e: Exception) {
        logger.error { "登录失败: ${e.message}" }
    }
}
