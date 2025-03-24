package com.ibit.chat.config

data class LoginConfig(
    val username: String = "",
    val password: String = ""
) {
    val loginUrl: String = "https://login.bit.edu.cn/"
}
