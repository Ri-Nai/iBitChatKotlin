package com.ibit.chat.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 创建对话请求模型
 * @property assistantId 助手ID
 * @property title 对话标题
 */
@Serializable
data class DialogueRequest(
    @SerialName("assistant_id")
    val assistantId: Int,
    val title: String
)
