package com.ibit.chat.service.base

import com.google.gson.Gson
import com.ibit.chat.model.DeleteDialogueRequest
import com.ibit.chat.model.DialogueRequest
import com.ibit.chat.model.IBitRequest
import com.ibit.chat.model.Message
import com.ibit.chat.network.ApiManager
import com.ibit.chat.network.api.chat.IBitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import okhttp3.ResponseBody
import java.io.IOException
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * iBit服务实现
 * 提供iBit API的流式聊天功能
 */
class IBitService {
    // API接口实例
    val chatApi: IBitApi = ApiManager.api.iBitApi

    // JSON处理器
    val gson = Gson()
    private val TAG = "IBitService"

    /**
     * 创建HTTP请求头
     * @return HTTP请求头
     */
    private fun createHeaders(): Map<String, String> {
        return mapOf(
            "Xdomain-Client" to "web_user",
            "Content-Type" to "application/json",
            "x-assistant-id" to "43",
            "Origin" to "https://ibit.yanhekt.cn",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
    }

    /**
     * 创建新对话
     * @return 对话ID
     */
    private suspend fun createDialogue(): Long = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val title = "[程序生成]$timestamp-${UUID.randomUUID().toString().substring(0, 4)}"

        val request = DialogueRequest(
            assistantId = 43,
            title = title
        )

        val response = chatApi.createDialogue(headers = createHeaders(), request = request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("创建对话失败: ${response.code()}")
        }

        return@withContext response.body()?.data?.id
            ?: throw RuntimeException("响应体为空")
    }

    /**
     * 删除对话
     * @param dialogueId 对话ID
     * @return 是否成功
     */
    private suspend fun deleteDialogue(dialogueId: Long): Boolean = withContext(Dispatchers.IO) {
        val request = DeleteDialogueRequest(
            ids = listOf(dialogueId)
        )

        val response = chatApi.deleteDialogue(headers = createHeaders(), request = request).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("删除对话失败: ${response.code()}")
        }

        return@withContext response.body()?.data?.success
            ?: throw RuntimeException("响应体为空")
    }

    /**
     * 处理历史消息
     * @param history 历史消息列表
     * @return 处理后的提示词
     */
    private fun processHistoryPrompt(history: List<Message>): String {
        if (history.isEmpty()) return ""

        val historyPrompt =
            StringBuilder("[历史对话](请注意这是由程序提供的历史对话功能,不要把它当成用户对话的一部分,不要刻意提及它):")

        history.forEach { message ->
            historyPrompt.append("\n${message.role}:${message.content}")
        }

        historyPrompt.append("\n接下来是用户的新一轮问题:\n")

        return historyPrompt.toString()
    }

    private fun processSystemPrompt(message: Message): String {
        return "[系统消息]${message.content}\n"
    }

    private fun prepareQuery(messages: List<Message>): Pair<String, List<Message>> {
        if (messages.isEmpty() || messages.last().role != "user") {
            throw IllegalArgumentException("无效请求：最后一条消息必须来自用户")
        }

        val history = mutableListOf<Message>()

        var systemPrompt = ""
        for (message in messages) {
            if (message.role == "system") {
                systemPrompt += processSystemPrompt(message)
            } else {
                history += message
            }
        }


        for (i in 0 until messages.size - 1 step 1) {
            if (i + 1 < messages.size &&
                messages[i].role == "user" &&
                messages[i + 1].role == "assistant"
            ) {
                history.add(Message(role = "user", content = messages[i].content))
                history.add(Message(role = "assistant", content = messages[i + 1].content))
            }
        }
        val query = systemPrompt + messages.last().content +
                processHistoryPrompt(history) + messages.last().content
        return Pair(query, history)
    }

    /**
     * 实现流式聊天功能
     *
     * @param messages 消息列表
     * @return 包含解析后ChatEvent对象的Flow
     */
    fun streamChat(messages: List<Message>): Flow<Result<String>> = flow {
        try {
            val (query, history) = prepareQuery(messages)
            val dialogueId = createDialogue()

            try {
                // 调用 Retrofit 接口获取流式响应
                val response = chatApi.streamChatCompletion(
                    headers = createHeaders(),
                    request = IBitRequest(
                        query = query,
                        dialogueId = dialogueId,
                        history = history,
                    )
                ).execute()

                if (!response.isSuccessful) {
                    throw IOException("请求失败，状态码：${response.code()}")
                }

                // 处理响应体
                response.body()?.let { body ->
                    processResponseBody(body)
                } ?: throw IOException("响应体为空")
            } catch (e: Exception) {
                emit(Result.failure(e))
            } finally {
                try {
                    deleteDialogue(dialogueId)
                } catch (e: Exception) {
                    println("删除对话失败: ${e.message}")
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<Result<String>>.processResponseBody(body: ResponseBody) {
        body.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data: ")) {
                    val jsonStr = line.substring(6) // 去掉 "data: " 前缀
                    when {
                        jsonStr.contains("\"is_end\":true") -> {
                            emit(Result.success("\n"))
                            return // 终止整个处理流程
                        }

                        else -> emit(parseJsonResponse(jsonStr))
                    }
                }
            }
        }
    }

    private fun parseJsonResponse(jsonStr: String): Result<String> {
        return try {
            val responseMap = gson.fromJson(jsonStr, Map::class.java)
            val answer = responseMap["answer"] as? String ?: ""
            Result.success(answer)
        } catch (e: Exception) {
            Result.failure(Exception("JSON 解析失败: $jsonStr", e))
        }
    }


}

