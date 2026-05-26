package com.hope.echo.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
  @POST("api/user/auth/phone/code")
  suspend fun sendCode(@Body request: SendCodeRequest): ApiResponse<Any>

  @POST("api/user/auth/phone/login")
  suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

  @POST("api/user/auth/google/login")
  suspend fun googleLogin(@Body request: GoogleLoginRequest): ApiResponse<LoginData>

  @POST("api/user/auth/logout") suspend fun logout(): ApiResponse<Any>
}
