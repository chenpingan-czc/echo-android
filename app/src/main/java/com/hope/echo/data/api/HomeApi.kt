package com.hope.echo.data.api

import retrofit2.http.GET

data class BookCardDto(
  val id: Long,
  val title: String,
  val coverUrl: String,
  val status: Int = 1,
  val progress: Int = 100,
)

data class HomeFeedDto(
  val userName: String,
  val userAvatarUrl: String,
  val continueListening: List<BookCardDto>,
  val myBooks: List<BookCardDto>,
  val featured: List<BookCardDto>,
)

interface HomeApi {
  @GET("api/home/feed") suspend fun feed(): ApiResponse<HomeFeedDto>
}
