package com.hope.echo.data.api

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class UploadBookData(val bookId: Long)

data class BookVoiceDto(
  val voiceId: String?,
  val name: String,
  val emoji: String,
)

data class ReportListenHistoryRequest(val bookId: Long)

data class DeleteBookRequest(val bookId: Long)
data class BatchDeleteBookRequest(val bookIds: List<Long>)

data class ListenedCountDto(val count: Int)

data class BookPageDto(
  val id: Long,
  val pageNum: Int,
  val imageUrl: String,
  val textContent: String?,
  val audioUrl: String?,
)

data class BookDetailDto(
  val id: Long,
  val title: String,
  val coverUrl: String,
  val status: Int,
  val pages: List<BookPageDto>,
)

interface BookApi {
  @GET("api/book/voices")
  suspend fun getVoices(): ApiResponse<List<BookVoiceDto>>

  @Multipart
  @POST("api/book/upload")
  suspend fun uploadBook(
    @Part images: List<MultipartBody.Part>,
    @Part voiceId: MultipartBody.Part? = null,
  ): ApiResponse<UploadBookData>

  @GET("api/book/detail")
  suspend fun getBookDetail(@Query("bookId") bookId: Long): ApiResponse<BookDetailDto>

  @POST("api/book/reportListenHistory")
  suspend fun reportListenHistory(@Body request: ReportListenHistoryRequest): ApiResponse<Unit?>

  @GET("api/book/myBooks")
  suspend fun myBooks(): ApiResponse<List<BookCardDto>>

  @POST("api/book/delete")
  suspend fun deleteBook(@Body request: DeleteBookRequest): ApiResponse<Unit?>

  @POST("api/book/batchDelete")
  suspend fun batchDeleteBooks(@Body request: BatchDeleteBookRequest): ApiResponse<Unit?>

  @GET("api/book/listenedCount")
  suspend fun listenedCount(): ApiResponse<ListenedCountDto>
}
