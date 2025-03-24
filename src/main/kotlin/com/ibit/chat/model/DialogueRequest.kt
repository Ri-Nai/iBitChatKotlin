package com.ibit.chat.model

import com.google.gson.annotations.SerializedName

data class DialogueRequest(
    @SerializedName("assistant_id") val assistantId: Int,
    @SerializedName("title") val title: String
)
