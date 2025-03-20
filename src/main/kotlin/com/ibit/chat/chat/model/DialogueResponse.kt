package com.ibit.chat.chat.model

import kotlinx.serialization.Serializable

/**
 * 对话响应数据
 * @property id 对话ID
 */
@Serializable
data class DialogueData(
    val id: Long
)

/**
 * 对话响应模型
 * @property data 对话数据
 */
@Serializable
data class DialogueResponse(
    val data: DialogueData
) 
