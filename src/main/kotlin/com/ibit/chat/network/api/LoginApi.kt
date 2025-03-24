package com.ibit.chat.network.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

private val iBitLoginService: String = "https://ibit.yanhekt.cn/proxy/v1/cas/callback"

interface LoginApi {

    @GET("/authserver/login")
    fun getLoginPage(
        @Query("service") service: String = iBitLoginService
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("/authserver/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("execution") execution: String,
        @Field("_eventId") eventId: String = "submit",
        @Field("cllt") cllt: String = "userNameLogin",
        @Field("dllt") dllt: String = "generalLogin",
        @Field("lt") lt: String = "",
        @Field("rememberMe") rememberMe: String = "true",
        @Field("service") service: String = iBitLoginService,
    ): Call<ResponseBody>

}
