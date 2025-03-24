package com.ibit.chat.network

import com.ibit.chat.network.api.LoginApi
import com.ibit.chat.network.api.chat.IBitApi
import retrofit2.Retrofit
import retrofit2.create


class Api(
    loginRetrofit: Retrofit,
    iBitRetrofit: Retrofit,
) {
    val loginApi: LoginApi = loginRetrofit.create()
    val iBitApi: IBitApi = iBitRetrofit.create()
}


