package com.hope.echo.data.api

data class ApiResponse<T>(val code: Int, val message: String, val data: T?) {
  /** echo-server 历史成功码为 0；hope-user Result 成功码为 200 */
  fun isSuccess(): Boolean = code == 0 || code == 200
}

/** 字段需与 hope-user 的 PhoneCodeRequest 一致；type 取值同 CodeType */
data class SendCodeRequest(
  val phone: String,
  val type: String,
) {
  companion object {
    /** 与 com.hope.user.common.enums.CodeType.LOGIN 一致 */
    const val TYPE_LOGIN = "login"
  }
}

data class LoginRequest(val phone: String, val code: String)

data class GoogleLoginRequest(val authCode: String)

data class LoginData(val token: String)
