package com.ibit.chat.service

import kotlinx.coroutines.flow.Flow
import com.ibit.chat.config.AppConfig
import com.ibit.chat.model.Message
import com.ibit.chat.service.base.IBitService
import com.ibit.chat.service.base.LoginService

object ServiceManager {
    val loginService = LoginService()
    val iBitService = IBitService()

}
