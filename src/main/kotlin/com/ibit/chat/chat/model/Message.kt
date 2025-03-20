package com.ibit.chat.chat.model

import kotlinx.serialization.Serializable

/**
 * 聊天消息模型
 * @property role 消息角色，可以是 "user", "assistant" 或 "system"
 * @property content 消息内容
 */
@Serializable
data class Message(
    val role: String,
    val content: String
) 
