package com.hope.echo.ui.screen

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hope.echo.R
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange900
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun WebViewScreen(title: String, url: String, onBack: () -> Unit) {
  var htmlContent by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var error by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(url) {
    try {
      val html =
        withContext(Dispatchers.IO) {
          val client = OkHttpClient()
          val request = Request.Builder().url(url).build()
          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            response.body?.string() ?: throw Exception("Empty response")
          }
        }
      htmlContent = html
    } catch (e: Exception) {
      error = e.message
    }
  }

  Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(Color(0xFFFDF8F5))
          .padding(top = 48.dp, bottom = 12.dp, start = 4.dp, end = 16.dp),
    ) {
      IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
        Icon(
          painter = painterResource(R.drawable.ic_arrow_back),
          contentDescription = stringResource(R.string.common_back),
          tint = Orange900,
          modifier = Modifier.size(24.dp),
        )
      }
      Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900,
        modifier = Modifier.align(Alignment.Center),
      )
    }

    when {
      error != null -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = stringResource(R.string.web_view_load_failed, error ?: ""), color = Color.Gray, fontSize = 14.sp)
        }
      }
      htmlContent == null -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = Orange400, modifier = Modifier.size(36.dp))
        }
      }
      else -> {
        val content = htmlContent!!
        AndroidView(
          factory = { context ->
            WebView(context).apply {
              webViewClient =
                object : WebViewClient() {
                  override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    isLoading = true
                  }

                  override fun onPageFinished(view: WebView?, url: String?) {
                    isLoading = false
                  }

                  override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                  ): Boolean = false
                }
              settings.apply {
                @Suppress("SetJavaScriptEnabled") javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
              }
              loadDataWithBaseURL(url, content, "text/html", "UTF-8", null)
            }
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
