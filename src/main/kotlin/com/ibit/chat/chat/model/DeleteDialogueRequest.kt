package com.ibit.chat.chat.model

import kotlinx.serialization.Serializable

/**
 * 删除对话请求模型
 * @property ids 要删除的对话ID列表
 */
@Serializable
data class DeleteDialogueRequest(
    val ids: List<Long>
) 
