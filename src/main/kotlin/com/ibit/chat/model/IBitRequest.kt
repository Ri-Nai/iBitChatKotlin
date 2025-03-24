package com.ibit.chat.model

import com.google.gson.annotations.SerializedName


data class IBitRequest(
    @SerializedName("query") val query: String,
    @SerializedName("dialogueId") val dialogueId: Long,
    @SerializedName("history") val history: List<Message> = emptyList(),
    @SerializedName("stream") val stream: Boolean = true,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("topK") val topK: Int = 3,
    @SerializedName("scoreThreshold") val scoreThreshold: Double = 0.5,
    @SerializedName("promptName") val promptName: String = "default",
    @SerializedName("knowledgeBaseName") val knowledgeBaseName: String = "cuc"
)
