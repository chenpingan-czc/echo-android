package com.hope.echo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hope.echo.data.TokenManager
import com.hope.echo.data.api.RetrofitClient
import com.hope.echo.ui.screen.AboutScreen
import com.hope.echo.ui.screen.EditProfileScreen
import com.hope.echo.ui.screen.LoginScreen
import com.hope.echo.ui.screen.MainScreen
import com.hope.echo.ui.screen.MyBooksScreen
import com.hope.echo.ui.screen.MyVoiceScreen
import com.hope.echo.ui.screen.PlayerScreen
import com.hope.echo.ui.screen.RecordVoiceScreen
import com.hope.echo.ui.screen.SplashScreen
import com.hope.echo.ui.screen.UploadScreen
import com.hope.echo.ui.screen.WebViewScreen
import com.hope.echo.ui.viewmodel.LoginViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.launch

private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
  if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action()
}

object Routes {
  const val SPLASH = "splash"
  const val LOGIN = "login"
  const val HOME = "home"
  const val UPLOAD = "upload"
  const val MY_BOOKS = "my_books"
  const val ABOUT = "about"
  const val MY_VOICE = "my_voice"
  const val RECORD_VOICE = "record_voice"
  const val EDIT_PROFILE = "edit_profile"
  const val PLAYER = "player/{bookId}"
  const val WEBVIEW = "webview/{title}/{url}"

  fun player(bookId: Long): String = "player/$bookId"

  fun webView(title: String, url: String): String {
    val encodedTitle = URLEncoder.encode(title, "UTF-8")
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    return "webview/$encodedTitle/$encodedUrl"
  }
}

@Composable
fun AppNavigation(onComposeReady: () -> Unit = {}) {
  val navController = rememberNavController()
  val context = LocalContext.current
  val tokenManager = remember { TokenManager(context) }
  val scope = rememberCoroutineScope()
  var checkedAuth by remember { mutableStateOf(false) }
  var isLoggedIn by remember { mutableStateOf(false) }
  var homeRefreshTrigger by remember { mutableIntStateOf(0) }
  var profileRefreshTrigger by remember { mutableIntStateOf(0) }
  var voiceRefreshTrigger by remember { mutableIntStateOf(0) }

  LaunchedEffect(Unit) {
    isLoggedIn = tokenManager.isLoggedIn()
    checkedAuth = true
  }

  NavHost(navController = navController, startDestination = Routes.SPLASH) {
    composable(Routes.SPLASH) { entry ->
      LaunchedEffect(Unit) { onComposeReady() }
      SplashScreen(
        onTimeout = {
          if (!checkedAuth) return@SplashScreen
          entry.ifResumed {
            val target = if (isLoggedIn) Routes.HOME else Routes.LOGIN
            navController.navigate(target) { popUpTo(Routes.SPLASH) { inclusive = true } }
          }
        }
      )
    }

    composable(Routes.LOGIN) { entry ->
      val loginViewModel: LoginViewModel = viewModel()
      val uiState by loginViewModel.uiState.collectAsState()
      val activity = LocalContext.current as Activity

      LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
          navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
      }

      LoginScreen(
        phone = uiState.phone,
        code = uiState.code,
        isLoading = uiState.isLoading,
        countdown = uiState.countdown,
        errorMessage = uiState.errorMessage,
        isGoogleLoading = uiState.isGoogleLoading,
        onPhoneChange = loginViewModel::onPhoneChange,
        onCodeChange = loginViewModel::onCodeChange,
        onSendCode = loginViewModel::sendVerificationCode,
        onLogin = loginViewModel::login,
        onGoogleSignIn = { loginViewModel.googleSignIn(activity) },
        onNavigateToWebView = { title, url ->
          entry.ifResumed { navController.navigate(Routes.webView(title, url)) }
        },
      )
    }

    composable(Routes.HOME) { entry ->
      MainScreen(
        refreshTrigger = homeRefreshTrigger,
        onNavigateToUpload = { entry.ifResumed { navController.navigate(Routes.UPLOAD) } },
        onNavigateToMyBooks = { entry.ifResumed { navController.navigate(Routes.MY_BOOKS) } },
        onNavigateToMyVoice = { entry.ifResumed { navController.navigate(Routes.MY_VOICE) } },
        profileRefreshTrigger = profileRefreshTrigger,
        onNavigateToEditProfile = { entry.ifResumed { navController.navigate(Routes.EDIT_PROFILE) } },
        onNavigateToAbout = { entry.ifResumed { navController.navigate(Routes.ABOUT) } },
        onNavigateToPlayer = { bookId ->
          entry.ifResumed { navController.navigate(Routes.player(bookId)) }
        },
        onLogout = {
          entry.ifResumed {
            scope.launch {
              try {
                RetrofitClient.authApi.logout()
              } catch (_: Exception) {
              }
              tokenManager.clearToken()
              navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.HOME) { inclusive = true }
              }
            }
          }
        },
      )
    }

    composable(Routes.PLAYER) { entry ->
      val bookId = entry.arguments?.getString("bookId")?.toLongOrNull() ?: return@composable
      PlayerScreen(bookId = bookId, onBack = { entry.ifResumed { navController.popBackStack() } })
    }

    composable(Routes.UPLOAD) { entry ->
      UploadScreen(
        onBack = { entry.ifResumed { navController.popBackStack() } },
        onUploadSuccess = {
          entry.ifResumed {
            homeRefreshTrigger++
            navController.popBackStack()
          }
        },
        onNavigateToRecordVoice = {
          entry.ifResumed { navController.navigate(Routes.RECORD_VOICE) }
        },
      )
    }

    composable(Routes.EDIT_PROFILE) { entry ->
      EditProfileScreen(onBack = {
        entry.ifResumed {
          profileRefreshTrigger++
          navController.popBackStack()
        }
      })
    }

    composable(Routes.ABOUT) { entry ->
      AboutScreen(
        onBack = { entry.ifResumed { navController.popBackStack() } },
        onNavigateToWebView = { title, url ->
          entry.ifResumed { navController.navigate(Routes.webView(title, url)) }
        },
      )
    }

    composable(Routes.MY_BOOKS) { entry ->
      MyBooksScreen(
        onBack = { entry.ifResumed { navController.popBackStack() } },
        onBookClick = { bookId ->
          entry.ifResumed { navController.navigate(Routes.player(bookId)) }
        },
      )
    }

    composable(Routes.MY_VOICE) { entry ->
      MyVoiceScreen(
        onBack = { entry.ifResumed { navController.popBackStack() } },
        onNavigateToRecord = { entry.ifResumed { navController.navigate(Routes.RECORD_VOICE) } },
        refreshTrigger = voiceRefreshTrigger,
      )
    }

    composable(Routes.RECORD_VOICE) { entry ->
      RecordVoiceScreen(
        onBack = {
          entry.ifResumed {
            voiceRefreshTrigger++
            navController.popBackStack()
          }
        },
      )
    }

    composable(Routes.WEBVIEW) { entry ->
      val title =
        URLDecoder.decode(entry.arguments?.getString("title") ?: "", "UTF-8")
      val url =
        URLDecoder.decode(entry.arguments?.getString("url") ?: "", "UTF-8")
      WebViewScreen(
        title = title,
        url = url,
        onBack = { entry.ifResumed { navController.popBackStack() } },
      )
    }
  }
}
