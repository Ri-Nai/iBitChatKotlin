package com.ibit.chat.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 聊天响应模型
 * @property id 响应ID
 * @property objectName 对象类型名称
 * @property created 创建时间戳
 * @property model 模型名称
 * @property answer AI回答内容
 * @property reasoningContent 推理内容
 * @property logId 日志ID
 * @property type 类型标识
 */
@Serializable
data class ChatResponse(
    val id: String,
    @SerialName("object") val objectName: String,
    val created: Long,
    val model: String,
    val answer: String,
    @SerialName("reasoning_content") val reasoningContent: String = "",
    @SerialName("log_id") val logId: Long = 0,
    val type: Int = 0
) 
