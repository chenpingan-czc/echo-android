package com.hope.echo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hope.echo.data.api.RetrofitClient
import com.hope.echo.ui.navigation.AppNavigation
import com.hope.echo.ui.theme.EchoTheme

class MainActivity : ComponentActivity() {
  private var isReady by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { !isReady }
    super.onCreate(savedInstanceState)
    RetrofitClient.init(applicationContext)
    enableEdgeToEdge(
      statusBarStyle =
        SystemBarStyle.light(
          scrim = android.graphics.Color.TRANSPARENT,
          darkScrim = android.graphics.Color.TRANSPARENT,
        ),
      navigationBarStyle =
        SystemBarStyle.light(
          scrim = android.graphics.Color.WHITE,
          darkScrim = android.graphics.Color.BLACK,
        ),
    )
    setContent {
      EchoTheme(dynamicColor = false) {
        AppNavigation(onComposeReady = { isReady = true })
      }
    }
  }
}
