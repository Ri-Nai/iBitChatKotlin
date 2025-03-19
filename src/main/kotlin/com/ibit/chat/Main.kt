package com.ibit.chat

import com.ibit.chat.api.BITLoginApi
import com.ibit.chat.api.IBitChatClient
import com.ibit.chat.model.Message
import com.ibit.chat.util.ConfigLoader
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 主函数，用于测试iBit聊天客户端
 */
fun main() = runBlocking {
    println("iBitChat Kotlin Demo")
    
    val client: IBitChatClient
    
    // 尝试从配置文件加载badge和cookie
    if (ConfigLoader.load() && 
        ConfigLoader.getBadge().isNotBlank() && 
        ConfigLoader.getCookie().isNotBlank()) {
        
        println("从配置文件加载凭据")
        val badge = ConfigLoader.getBadge()
        val cookie = ConfigLoader.getCookie()
        client = IBitChatClient(badge, cookie)
        
    } else {
        // 尝试从local.properties读取用户名和密码
        println("未找到有效的badge和cookie")
        
        if (ConfigLoader.load() &&
            ConfigLoader.getUsername().isNotBlank() &&
            ConfigLoader.getPassword().isNotBlank()) {
            
            val username = ConfigLoader.getUsername()
            val password = ConfigLoader.getPassword()
            
            println("从配置文件加载用户名和密码")
            println("正在使用北理工统一身份认证登录...")
            
            try {
                val loginApi = BITLoginApi()
                client = loginApi.login(username, password)
                println("登录成功！")
            } catch (e: Exception) {
                logger.error(e) { "登录失败" }
                println("登录失败: ${e.message}")
                return@runBlocking
            }
            
        } else {
            println("配置文件中未找到登录凭据，请手动输入")
            
            print("请输入学号: ")
            val username = readLine() ?: ""
            
            print("请输入密码: ")
            val password = readLine() ?: ""
            
            if (username.isBlank() || password.isBlank()) {
                println("学号或密码不能为空")
                return@runBlocking
            }
            
            println("正在登录...")
            try {
                val loginApi = BITLoginApi()
                client = loginApi.login(username, password)
                println("登录成功！")
            } catch (e: Exception) {
                logger.error(e) { "登录失败" }
                println("登录失败: ${e.message}")
                return@runBlocking
            }
        }
    }
    
    // 演示历史消息
    val history = mutableListOf(
        Message(role = "user", content = "今天的天气怎么样？"),
        Message(role = "assistant", content = "今天天气晴，适合出门活动！"),
    )
    
    while (true) {
        print("\n请输入问题(输入'exit'退出): ")
        val query = readLine() ?: ""
        
        if (query.equals("exit", ignoreCase = true)) {
            break
        }
        
        if (query.isBlank()) {
            continue
        }
        
        print("回复: ")
        val userMessage = Message(role = "user", content = query)
        val response = StringBuilder()
        
        try {
            client.chatStream(query, history)
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
                // 将用户问题和AI回复添加到历史记录
                history.add(userMessage)
                history.add(Message(role = "assistant", content = response.toString()))
                
                // 保持历史记录在合理范围内（最近5轮对话）
                if (history.size > 10) {
                    history.removeAt(0)
                    history.removeAt(0)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "聊天过程发生异常" }
            println("\n发生错误: ${e.message}")
        }
    }
    
    println("感谢使用iBitChat Kotlin Demo!")
} 
