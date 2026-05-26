package com.hope.echo.data.api

import android.content.Context
import com.hope.echo.BuildConfig
import com.hope.echo.data.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

  private lateinit var retrofit: Retrofit

  fun init(context: Context) {
    val appContext = context.applicationContext
    val tokenManager = TokenManager(appContext)
    val okHttpClient =
      OkHttpClient.Builder()
        .addInterceptor(
          HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        )
        .addInterceptor { chain ->
          val token = runBlocking(Dispatchers.IO) { tokenManager.getToken() }
          val request =
            chain.request().newBuilder().apply {
              if (!token.isNullOrBlank()) {
                header("Authorization", token)
              }
            }.build()
          chain.proceed(request)
        }
        .build()

    retrofit =
      Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
  }

  val authApi: AuthApi
    get() = retrofit.create(AuthApi::class.java)

  val homeApi: HomeApi
    get() = retrofit.create(HomeApi::class.java)

  val bookApi: BookApi
    get() = retrofit.create(BookApi::class.java)

  val userApi: UserApi
    get() = retrofit.create(UserApi::class.java)
}
