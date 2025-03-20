package com.ibit.chat.chat.model

import kotlinx.serialization.Serializable

/**
 * 删除对话响应数据
 * @property success 是否成功
 */
@Serializable
data class DeleteDialogueData(
    val success: Boolean
)

/**
 * 删除对话响应模型
 * @property data 响应数据
 */
@Serializable
data class DeleteDialogueResponse(
    val data: DeleteDialogueData
) 
