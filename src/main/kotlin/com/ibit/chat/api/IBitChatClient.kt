package com.ibit.chat.api

import com.ibit.chat.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * iBit聊天客户端
 * @property badge Cookie中的badge值
 * @property cookie 完整的cookie字符串
 */
class IBitChatClient(
    private val badge: String,
    private val cookie: String
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://ibit.yanhekt.cn"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 创建HTTP请求头
     * @return HTTP请求头
     */
    private fun createHeaders(): Map<String, String> {
        val badgeDecoded = URLEncoder.encode(badge, "UTF-8")
        
        return mapOf(
            "Host" to "ibit.yanhekt.cn",
            "Connection" to "keep-alive",
            "sec-ch-ua-platform" to "\"Windows\"",
            "Authorization" to "Bearer undefined",
            "Xdomain-Client" to "web_user",
            "sec-ch-ua" to "\"Not(A:Brand\";v=\"99\", \"Microsoft Edge\";v=\"133\", \"Chromium\";v=\"133\"",
            "sec-ch-ua-mobile" to "?0",
            "badge" to badge,  
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
            "DNT" to "1",
            "Content-Type" to "application/json",
            "x-assistant-id" to "43",
            "Accept" to "*/*",
            "Origin" to "https://ibit.yanhekt.cn",
            "Sec-Fetch-Site" to "same-origin",
            "Sec-Fetch-Mode" to "cors",
            "Sec-Fetch-Dest" to "empty",
            "Cookie" to cookie
        )
    }

    /**
     * 创建新对话
     * @return 对话ID
     */
    suspend fun createDialogue(): Long = withContext(Dispatchers.IO) {
        val url = "$baseUrl/proxy/v1/dialogue"
        val timestamp = System.currentTimeMillis()
        val title = "[程序生成]$timestamp-${UUID.randomUUID().toString().substring(0, 4)}"
        
        val requestBody = DialogueRequest(
            assistantId = 43,
            title = title
        )
        
        val requestBodyString = json.encodeToString(DialogueRequest.serializer(), requestBody)
        val request = Request.Builder()
            .url(url)
            .post(requestBodyString.toRequestBody("application/json".toMediaType()))
            .apply { createHeaders().forEach { (key, value) -> header(key, value) } }
            .build()
            
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            logger.error { "创建对话失败: ${response.code}" }
            throw RuntimeException("创建对话失败: ${response.code}")
        }
        
        val responseBodyString = response.body?.string() ?: throw RuntimeException("响应体为空")
        val dialogueResponse = json.decodeFromString(DialogueResponse.serializer(), responseBodyString)
        
        return@withContext dialogueResponse.data.id
    }
    
    /**
     * 删除对话
     * @param dialogueId 对话ID
     * @return 是否成功
     */
    suspend fun deleteDialogue(dialogueId: Long): Boolean = withContext(Dispatchers.IO) {
        val url = "$baseUrl/proxy/v1/dialogue"
        
        val requestBody = DeleteDialogueRequest(
            ids = listOf(dialogueId)
        )
        
        val requestBodyString = json.encodeToString(DeleteDialogueRequest.serializer(), requestBody)
        
        val request = Request.Builder()
            .url(url)
            .delete(requestBodyString.toRequestBody("application/json".toMediaType()))
            .apply { createHeaders().forEach { (key, value) -> header(key, value) } }
            .build()
            
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            logger.error { "删除对话失败: ${response.code}" }
            throw RuntimeException("删除对话失败: ${response.code}")
        }
        
        val responseBodyString = response.body?.string() ?: throw RuntimeException("响应体为空")
        val deleteResponse = json.decodeFromString(DeleteDialogueResponse.serializer(), responseBodyString)
        
        return@withContext deleteResponse.data.success
    }

    /**
     * 处理历史消息
     * @param history 历史消息列表
     * @return 处理后的提示词
     */
    private fun processHistoryPrompt(history: List<Message>): String {
        if (history.isEmpty()) return ""
        
        val historyPrompt = StringBuilder("[历史对话](请注意这是由程序提供的历史对话功能,不要把它当成用户对话的一部分,不要刻意提及它):")
        
        history.forEach { message ->
            historyPrompt.append("\n${message.role}:${message.content}")
        }
        
        historyPrompt.append("\n接下来是用户的新一轮问题:\n")
        
        return historyPrompt.toString()
    }

    /**
     * 流式聊天
     * @param query 用户查询
     * @param history 历史消息列表
     * @return 回复流
     */
    fun chatStream(
        query: String,
        history: List<Message> = emptyList(),
        temperature: Double = 0.7,
        topK: Int = 3,
        scoreThreshold: Double = 0.5,
        promptName: String = "default",
        knowledgeBaseName: String = "cuc"
    ): Flow<String> = flow {
        val dialogueId = createDialogue()
        val url = "$baseUrl/proxy/v1/chat/stream/private/kb"
        val processedQuery = processHistoryPrompt(history) + query
        
        val chatRequest = ChatRequest(
            query = processedQuery,
            dialogueId = dialogueId,
            stream = true,
            history = history,
            temperature = temperature,
            topK = topK,
            scoreThreshold = scoreThreshold,
            promptName = promptName,
            knowledgeBaseName = knowledgeBaseName
        )
        
        val requestBodyString = json.encodeToString(ChatRequest.serializer(), chatRequest)
        
        val request = Request.Builder()
            .url(url)
            .post(requestBodyString.toRequestBody("application/json".toMediaType()))
            .apply { createHeaders().forEach { (key, value) -> header(key, value) } }
            .build()
        
        // 创建响应对象和读取器
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            logger.error { "聊天请求失败: ${response.code}" }
            throw RuntimeException("聊天请求失败: ${response.code}")
        }
        
        val responseBody = response.body ?: throw RuntimeException("响应体为空")
        val reader = responseBody.charStream().buffered()
        
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                // println("接收到数据: $line")
                
                if (line!!.startsWith("data: ")) {
                    val jsonStr = line!!.substring(6)
                    try {
                        val chatResponse = json.decodeFromString<ChatResponse>(jsonStr)
                        val answer = chatResponse.answer
                        // println("发送回复片段: $answer")
                        emit(answer)
                    } catch (e: Exception) {
                        if (jsonStr.contains("\"is_end\":true")) {
                            emit("[DONE]")
                        } else {
                            logger.error { "解析JSON失败: $jsonStr" }
                        }
                    }
                }
            }
        } finally {
            try {
                responseBody.close()
                response.close()
                // 删除临时对话
                deleteDialogue(dialogueId)
            } catch (e: Exception) {
                logger.error { "清理资源时出错: ${e.message}" }
            }
        }
    }.flowOn(Dispatchers.IO) // 确保Flow在IO调度器上执行，解决上下文问题
} 
