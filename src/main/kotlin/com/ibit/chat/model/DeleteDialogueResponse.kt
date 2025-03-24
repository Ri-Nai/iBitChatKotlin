package com.ibit.chat.model

import com.google.gson.annotations.SerializedName

data class DeleteDialogueResponse(
    @SerializedName("data") val data: DeleteDialogueData
) {
    data class DeleteDialogueData(
        @SerializedName("success") val success: Boolean
    )
}
