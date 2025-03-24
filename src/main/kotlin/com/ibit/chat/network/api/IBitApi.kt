package com.ibit.chat.network.api.chat

import com.ibit.chat.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Streaming

interface IBitApi {
    @POST("proxy/v1/dialogue")
    fun createDialogue(
        @HeaderMap headers: Map<String, String>,
        @Body request: DialogueRequest
    ): Call<DialogueResponse>

    @POST("proxy/v1/dialogue")
    fun deleteDialogue(
        @HeaderMap headers: Map<String, String>,
        @Body request: DeleteDialogueRequest
    ): Call<DeleteDialogueResponse>

    @Streaming
    @POST("proxy/v1/chat/stream/private/kb")
    fun streamChatCompletion(
        @HeaderMap headers: Map<String, String>,
        @Body request: IBitRequest
    ): Call<ResponseBody>

}



