package com.hope.echo.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UserProfileDto(
  @SerializedName("nickname") val name: String,
  val avatar: String,
  val birthday: String?,
)

data class UpdateProfileRequest(
  @SerializedName("nickname") val name: String,
  val birthday: String?,
)

data class AvatarUploadDto(
  val avatarUrl: String,
)

data class VoiceDto(
  val id: Long,
  val name: String,
  val audioUrl: String,
  val duration: Int,
  val createdAt: String,
)

data class VoiceUploadDto(
  val id: Long,
  val audioUrl: String,
)

data class VoiceRenameRequest(
  val id: Long,
  val name: String,
)

data class BatchDeleteVoicesRequest(
  val ids: List<Long>,
)

interface UserApi {
  @GET("api/user/profile")
  suspend fun getProfile(): ApiResponse<UserProfileDto>

  @POST("api/user/profile/update")
  suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<Void>

  @Multipart
  @POST("api/user/avatar/upload")
  suspend fun uploadAvatar(@Part file: MultipartBody.Part): ApiResponse<AvatarUploadDto>

  @GET("api/user/voice/prompt-text")
  suspend fun getVoicePromptText(): ApiResponse<String>

  @GET("api/user/voice/list")
  suspend fun getVoiceList(): ApiResponse<List<VoiceDto>>

  @Multipart
  @POST("api/user/voice/upload")
  suspend fun uploadVoice(
    @Part audio: MultipartBody.Part,
    @Part("name") name: okhttp3.RequestBody,
  ): ApiResponse<VoiceUploadDto>

  @POST("api/user/voice/delete")
  suspend fun deleteVoices(@Body request: BatchDeleteVoicesRequest): ApiResponse<Boolean?>
}
