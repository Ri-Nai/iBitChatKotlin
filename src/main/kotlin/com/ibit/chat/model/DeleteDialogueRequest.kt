package com.ibit.chat.model

import com.google.gson.annotations.SerializedName

data class DeleteDialogueRequest(
    @SerializedName("ids") val ids: List<Long>
) 
