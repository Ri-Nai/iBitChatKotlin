package com.ibit.chat

import com.ibit.chat.login.BITLoginService
import com.ibit.chat.chat.IBitChatClient
import com.ibit.chat.chat.model.Message
import com.ibit.chat.config.ConfigLoader
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 主函数，用于测试iBit聊天客户端
 */
fun main() = runBlocking {
    println("iBitChat Kotlin Demo")
    
    val client = initializeClient() ?: return@runBlocking
    
    startChatLoop(client)
    
    println("感谢使用iBitChat Kotlin Demo!")
} 

/**
 * 初始化聊天客户端
 * @return 初始化成功返回客户端实例，失败返回null
 */
private suspend fun initializeClient(): IBitChatClient? {
    return tryLoginWithBadge() ?: tryLoginWithSavedCredentials() ?: tryLoginWithUserInput()
}

/**
 * 尝试使用配置中的Badge登录
 * @return 成功返回客户端实例，失败返回null
 */
private fun tryLoginWithBadge(): IBitChatClient? {
    if (ConfigLoader.load() && ConfigLoader.getBadge().isNotBlank()) {
        println("从配置文件加载凭据")
        val badge = ConfigLoader.getBadge()
        return IBitChatClient(badge)
    }
    println("未找到有效的badge")
    return null
}

/**
 * 尝试使用配置中保存的用户名和密码登录
 * @return 成功返回客户端实例，失败返回null
 */
private suspend fun tryLoginWithSavedCredentials(): IBitChatClient? {
    if (!ConfigLoader.load() || 
        ConfigLoader.getUsername().isBlank() || 
        ConfigLoader.getPassword().isBlank()) {
        return null
    }
    
    val username = ConfigLoader.getUsername()
    val password = ConfigLoader.getPassword()
    
    println("从配置文件加载用户名和密码")
    println("正在使用北理工统一身份认证登录...")
    
    return loginWithCredentials(username, password)
}

/**
 * 尝试使用用户输入的凭据登录
 * @return 成功返回客户端实例，失败返回null
 */
private suspend fun tryLoginWithUserInput(): IBitChatClient? {
    println("配置文件中未找到登录凭据，请手动输入")
    
    print("请输入学号: ")
    val username = readLine() ?: ""
    
    print("请输入密码: ")
    val password = readLine() ?: ""
    
    if (username.isBlank() || password.isBlank()) {
        println("学号或密码不能为空")
        return null
    }
    
    println("正在登录...")
    return loginWithCredentials(username, password)
}

/**
 * 使用用户名和密码登录
 * @param username 用户名
 * @param password 密码
 * @return 成功返回客户端实例，失败返回null
 */
private suspend fun loginWithCredentials(username: String, password: String): IBitChatClient? {
    return try {
        val loginService = BITLoginService()
        val badge = loginService.login(username, password)
        val client = IBitChatClient(badge)
        println("登录成功！")
        client
    } catch (e: Exception) {
        logger.error(e) { "登录失败" }
        println("登录失败: ${e.message}")
        null
    }
}

/**
 * 启动聊天循环
 * @param client 聊天客户端
 */
private suspend fun startChatLoop(client: IBitChatClient) {
    // 演示历史消息
    val history = mutableListOf(
        Message(role = "system", content = "你是一个AI助手，回答用户的问题。"),
        Message(role = "user", content = "今天的天气怎么样？"),
        Message(role = "assistant", content = "今天天气晴，适合出门活动！"),
    )
    val messages = mutableListOf<Message>()
    messages += history
    
    while (true) {
        print("\n请输入问题(输入'exit'退出): ")
        val query = readLine() ?: ""
        
        if (query.equals("exit", ignoreCase = true)) {
            break
        }
        
        if (query.isBlank()) {
            continue
        }
        
        handleChatQuery(client, messages, query)
    }
}

/**
 * 处理聊天查询
 * @param client 聊天客户端
 * @param messages 消息历史
 * @param query 用户查询
 */
private suspend fun handleChatQuery(client: IBitChatClient, messages: MutableList<Message>, query: String) {
    print("回复: ")
    val userMessage = Message(role = "user", content = query)
    messages.add(userMessage)
    val response = StringBuilder()
    
    try {
        client.chatStream(messages)
            .catch { e -> 
                logger.error(e) { "聊天流处理错误" }
                println("\n发生错误: ${e.message}")
            }
            .collect { chunk ->
                if (chunk == "[DONE]") {
                    return@collect
                }
                print(chunk)
                response.append(chunk)
            }
        
        if (response.isNotEmpty()) {
            // 将AI回复添加到历史记录
            messages.add(Message(role = "assistant", content = response.toString()))
            
            // 保持历史记录在合理范围内（最近5轮对话）
            if (messages.size > 10) {
                messages.removeAt(0)
                messages.removeAt(0)
            }
        }
    } catch (e: Exception) {
        logger.error(e) { "聊天过程发生异常" }
        println("\n发生错误: ${e.message}")
    }
} 
