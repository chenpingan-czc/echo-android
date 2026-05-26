package com.hope.echo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.R
import com.hope.echo.data.api.HomeFeedDto
import com.hope.echo.data.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
  val isLoading: Boolean = true,
  val isRefreshing: Boolean = false,
  val feed: HomeFeedDto? = null,
  val errorMessage: String? = null,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

  private val homeApi = RetrofitClient.homeApi

  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  private var pollingJob: Job? = null

  init {
    load()
  }

  /** 首次加载：全屏 loading，清空旧数据 */
  fun load() {
    viewModelScope.launch {
      _uiState.value = HomeUiState(isLoading = true, isRefreshing = false, feed = null)
      fetchFeed(keepExistingOnError = false)
    }
  }

  /** 下拉刷新：保留旧数据，只显示顶部指示器 */
  fun pullRefresh() {
    if (_uiState.value.isRefreshing) return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
      fetchFeed(keepExistingOnError = true)
    }
  }

  /** 静默刷新：不显示任何 loading 指示器，仅更新数据 */
  fun silentRefresh() {
    viewModelScope.launch {
      fetchFeed(keepExistingOnError = true)
    }
  }

  private suspend fun fetchFeed(keepExistingOnError: Boolean) {
    try {
      val response = homeApi.feed()
      if (response.code == 0 && response.data != null) {
        _uiState.value =
            HomeUiState(isLoading = false, isRefreshing = false, feed = response.data)
        startPollingIfNeeded(response.data)
      } else {
        val msg =
            when {
              response.code == 0 -> getApplication<Application>().getString(R.string.home_error_fetch_failed)
              else -> response.message.ifBlank { getApplication<Application>().getString(R.string.home_load_failed_message) }
            }
        applyFeedFailure(keepExistingOnError, msg)
      }
    } catch (e: Exception) {
      val msg = e.message?.ifBlank { null } ?: getApplication<Application>().getString(R.string.home_network_error)
      applyFeedFailure(keepExistingOnError, msg)
    }
  }

  /** 失败时：若仍有旧数据可展示则保留并隐藏错误；否则进入兜底页并展示 [message]。 */
  private fun applyFeedFailure(keepExistingOnError: Boolean, message: String) {
    val keptFeed = if (keepExistingOnError) _uiState.value.feed else null
    _uiState.value =
        _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            feed = keptFeed,
            errorMessage = if (keptFeed != null) null else message,
        )
  }

  private fun startPollingIfNeeded(feed: HomeFeedDto) {
    val hasProcessing = feed.myBooks.any { it.status == 0 }
    if (!hasProcessing) {
      pollingJob?.cancel()
      pollingJob = null
      return
    }
    if (pollingJob?.isActive == true) return
    pollingJob = viewModelScope.launch {
      while (isActive) {
        delay(10_000L)
        try {
          val response = homeApi.feed()
          if (response.code == 0 && response.data != null) {
            _uiState.value =
              _uiState.value.copy(feed = response.data)
            val stillProcessing = response.data.myBooks.any { it.status == 0 }
            if (!stillProcessing) break
          }
        } catch (_: Exception) {
          // ignore, retry next cycle
        }
      }
    }
  }
}
