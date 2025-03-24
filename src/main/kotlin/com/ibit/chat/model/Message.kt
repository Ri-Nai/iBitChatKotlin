package com.ibit.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 聊天消息模型
 * 用于表示聊天中的消息，包括角色和内容
 *
 * @property role 消息发送者角色: system, user, assistant
 * @property content 消息内容
 */
data class Message(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)
