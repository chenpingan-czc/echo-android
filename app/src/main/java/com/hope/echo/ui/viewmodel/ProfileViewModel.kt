package com.hope.echo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.data.api.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
  val userName: String = "",
  val avatarUrl: String = "",
  val listenedCount: Int = 0,
  val isLoading: Boolean = true,
)

class ProfileViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  init {
    loadProfile()
  }

  fun loadProfile() {
    viewModelScope.launch {
      val profileDeferred = async {
        runCatching { RetrofitClient.userApi.getProfile() }.getOrNull()
      }
      val countDeferred = async {
        runCatching { RetrofitClient.bookApi.listenedCount() }.getOrNull()
      }

      val profileResp = profileDeferred.await()
      val countResp = countDeferred.await()

      _uiState.update {
        it.copy(
          userName = profileResp?.data?.name ?: it.userName,
          avatarUrl = profileResp?.data?.avatar ?: it.avatarUrl,
          listenedCount = countResp?.data?.count ?: it.listenedCount,
          isLoading = false,
        )
      }
    }
  }
}
