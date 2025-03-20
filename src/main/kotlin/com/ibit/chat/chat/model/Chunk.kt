package com.ibit.chat.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 表示 Deepseek API 返回的流式响应数据块。
 *
 * @param model 返回的模型名称，例如 "deepseek-r1"。
 * @param objectType JSON 中对应的 "object" 字段，表示返回数据的类型。
 * @param choices 响应中所有候选数据列表，每个候选项包含具体的内容和结束标识。
 */
@Serializable
data class Chunk(
    val model: String,
    @SerialName("object") val objectType: String,
    val choices: List<Choice>
) {
    /**
     * 表示候选响应数据。
     *
     * @param index 候选项索引。
     * @param delta 包含实际返回文本内容的变化数据。
     * @param finish_reason 候选项结束的原因，如果尚未结束则为 null。
     */
    @Serializable
    data class Choice(
        val index: Int,
        val delta: Delta,
        val finish_reason: String?
    ) {
        /**
         * 表示候选项中的变化数据，包含返回文本内容。
         *
         * @param content API 返回的文本内容，可能为 null。
         * @return 返回变化中的文本内容。
         */
        @Serializable
        data class Delta(val content: String? = null)
    }
}
