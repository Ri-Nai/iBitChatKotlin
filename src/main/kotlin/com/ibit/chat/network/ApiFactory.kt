package com.ibit.chat.network

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.ibit.chat.config.AppConfig
import mu.KotlinLogging
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API工厂
 * 负责创建各种API接口实例
 */
object ApiFactory {
    private val logger = KotlinLogging.logger {}

    // 构建GSON实例
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setLenient()
        .create()

    /**
     * 创建API接口实例
     * @return API接口集合
     */
    fun create(): Api {
        logger.info { "创建API接口集合" }

        // 创建登录API
        val loginRetrofit = createRetrofit(
            baseUrl = AppConfig.loginConfig.loginUrl,
            apiName = "登录API"
        )

        // 创建iBit API
        val iBitRetrofit = createRetrofit(
            baseUrl = AppConfig.ibitConfig.ibitUrl,
            apiName = "iBit API"
        )




        return Api(
            loginRetrofit = loginRetrofit,
            iBitRetrofit = iBitRetrofit,
        )
    }

    /**
     * 创建Retrofit实例
     * @param baseUrl 基础URL
     * @param apiName API名称（用于日志）
     * @return Retrofit实例
     */
    private fun createRetrofit(baseUrl: String, apiName: String): Retrofit {
        logger.debug { "创建 $apiName Retrofit实例: $baseUrl" }

        return Retrofit.Builder()
            .client(ApiManager.bitClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
