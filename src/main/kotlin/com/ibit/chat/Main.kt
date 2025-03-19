package com.ibit.chat

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
    
    // 从配置文件加载badge和cookie
    if (!ConfigLoader.load()) {
        println("无法加载配置文件，请确保local.properties文件存在")
        return@runBlocking
    }
    
    val badge = ConfigLoader.getBadge()
    val cookie = ConfigLoader.getCookie()

    if (badge.isBlank() || cookie.isBlank()) {
        println("badge或cookie不能为空，请检查local.properties配置")
        return@runBlocking
    }
    
    println("成功加载配置")
    val client = IBitChatClient(badge, cookie)
    
    // 演示历史消息
    val history = mutableListOf<Message>(
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
