package com.hope.echo.ui.viewmodel

import android.app.Application
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.hope.echo.BuildConfig
import com.hope.echo.R
import com.hope.echo.data.TokenManager
import com.hope.echo.data.api.GoogleLoginRequest
import com.hope.echo.data.api.LoginRequest
import com.hope.echo.data.api.RetrofitClient
import com.hope.echo.data.api.SendCodeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
  val phone: String = "",
  val code: String = "",
  val isLoading: Boolean = false,
  val countdown: Int = 0,
  val loginSuccess: Boolean = false,
  val errorMessage: String? = null,
  val isGoogleLoading: Boolean = false,
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
  private val tokenManager = TokenManager(application)
  private val authApi = RetrofitClient.authApi
  private val credentialManager = CredentialManager.create(application)

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  fun onPhoneChange(phone: String) {
    _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null)
  }

  fun onCodeChange(code: String) {
    _uiState.value = _uiState.value.copy(code = code, errorMessage = null)
  }

  fun sendVerificationCode() {
    val state = _uiState.value
    if (state.phone.length != 11 || state.countdown > 0) return

    _uiState.value = _uiState.value.copy(countdown = 60)

    viewModelScope.launch {
      try {
        authApi.sendCode(SendCodeRequest(state.phone, SendCodeRequest.TYPE_LOGIN))
      } catch (_: Exception) {}
    }

    viewModelScope.launch {
      while (_uiState.value.countdown > 0) {
        delay(1000)
        _uiState.value = _uiState.value.copy(countdown = _uiState.value.countdown - 1)
      }
    }
  }

  fun login() {
    val state = _uiState.value
    if (state.phone.length != 11 || state.code.length != 6) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val response = authApi.login(LoginRequest(state.phone, state.code))
        if (response.code == 0 && response.data != null) {
          tokenManager.saveToken(response.data.token)
          _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = response.message)
        }
      } catch (e: Exception) {
        _uiState.value =
          _uiState.value.copy(isLoading = false, errorMessage = e.message ?: getApplication<Application>().getString(R.string.login_failed))
      }
    }
  }

  fun googleSignIn(activityContext: android.app.Activity) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGoogleLoading = true, errorMessage = null)
      try {
        val googleIdOption =
          GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        val result = credentialManager.getCredential(activityContext, request)

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
          val idToken = googleIdTokenCredential.idToken

          val response = authApi.googleLogin(GoogleLoginRequest(authCode = idToken))
          if (response.isSuccess() && response.data != null) {
            tokenManager.saveToken(response.data.token)
            _uiState.value = _uiState.value.copy(isGoogleLoading = false, loginSuccess = true)
          } else {
            _uiState.value =
              _uiState.value.copy(isGoogleLoading = false, errorMessage = response.message)
          }
        } else {
          _uiState.value =
            _uiState.value.copy(isGoogleLoading = false, errorMessage = getApplication<Application>().getString(R.string.login_google_failed))
        }
      } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
        _uiState.value = _uiState.value.copy(isGoogleLoading = false)
      } catch (e: Exception) {
        _uiState.value =
          _uiState.value.copy(
            isGoogleLoading = false,
            errorMessage = e.message ?: getApplication<Application>().getString(R.string.login_google_failed),
          )
      }
    }
  }
}
