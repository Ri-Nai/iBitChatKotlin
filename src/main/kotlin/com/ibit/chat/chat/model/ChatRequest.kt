package com.ibit.chat.chat.model

import kotlinx.serialization.Serializable

/**
 * 聊天请求模型
 * @property query 用户查询
 * @property dialogueId 对话ID
 * @property stream 是否启用流式传输
 * @property history 历史消息列表
 * @property temperature 温度参数
 * @property topK Top-K参数
 * @property scoreThreshold 分数阈值
 * @property promptName 提示名称
 * @property knowledgeBaseName 知识库名称
 */
@Serializable
data class ChatRequest(
    val query: String,
    val dialogueId: Long,
    val stream: Boolean = true,
    val history: List<Message> = emptyList(),
    val temperature: Double = 0.7,
    val topK: Int = 3,
    val scoreThreshold: Double = 0.5,
    val promptName: String = "default",
    val knowledgeBaseName: String = "cuc"
) 
