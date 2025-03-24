package com.ibit.chat.model

import com.google.gson.annotations.SerializedName

data class DialogueResponse(
    @SerializedName("data") val data: DialogueData
) {
    /**
     * 对话数据
     * @property id 对话ID
     */
    data class DialogueData(
        @SerializedName("id") val id: Long
    )
} 
