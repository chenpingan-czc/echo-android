package com.hope.echo.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.hope.echo.R
import com.hope.echo.data.api.BookDetailDto
import com.hope.echo.data.api.ReportListenHistoryRequest
import com.hope.echo.data.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
  val isLoading: Boolean = true,
  val errorMessage: String? = null,
  val book: BookDetailDto? = null,
  val pageIndex: Int = 0,
  val isPlaying: Boolean = false,
  val progress: Float = 0f,
  val currentTimeMs: Long = 0L,
  val durationMs: Long = 0L,
) {
  val totalPages: Int get() = book?.pages?.size ?: 0
  val canGoPrev: Boolean get() = pageIndex > 0
  val canGoNext: Boolean get() = pageIndex < totalPages - 1
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

  private val bookApi = RetrofitClient.bookApi

  private val _uiState = MutableStateFlow(PlayerUiState())
  val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

  private var mediaPlayer: MediaPlayer? = null
  private var progressJob: Job? = null

  fun loadBook(bookId: Long) {
    viewModelScope.launch {
      _uiState.value = PlayerUiState(isLoading = true)
      try {
        val response = bookApi.getBookDetail(bookId)
        if (response.code == 0 && response.data != null) {
          val book = response.data
          _uiState.value = PlayerUiState(isLoading = false, book = book)
          // 进入播放页即上报收听历史（与是否有音频页无关）
          reportListenHistory(bookId)
          if (book.pages.isNotEmpty()) {
            play()
          }
        } else {
          _uiState.value =
            PlayerUiState(isLoading = false, errorMessage = response.message.ifBlank { getApplication<Application>().getString(R.string.player_load_failed) })
        }
      } catch (e: Exception) {
        _uiState.value = PlayerUiState(isLoading = false, errorMessage = e.message ?: getApplication<Application>().getString(R.string.player_network_error))
      }
    }
  }

  private fun reportListenHistory(bookId: Long) {
    viewModelScope.launch {
      try {
        bookApi.reportListenHistory(ReportListenHistoryRequest(bookId))
      } catch (e: Exception) {
        Log.w("PlayerViewModel", "Failed to report listen history", e)
      }
    }
  }

  fun togglePlayPause() {
    val state = _uiState.value
    if (state.book == null || state.book.pages.isEmpty()) return

    if (state.isPlaying) {
      pause()
    } else {
      play()
    }
  }

  private fun play() {
    val state = _uiState.value
    val page = state.book?.pages?.getOrNull(state.pageIndex) ?: return
    val audioUrl = page.audioUrl

    if (audioUrl.isNullOrBlank()) {
      simulatePlayback()
      return
    }

    if (mediaPlayer != null && !state.isPlaying) {
      mediaPlayer?.start()
      _uiState.value = state.copy(isPlaying = true)
      startProgressTracking()
      return
    }

    releasePlayer()
    _uiState.value = state.copy(isPlaying = true, progress = 0f, currentTimeMs = 0L, durationMs = 0L)

    mediaPlayer = MediaPlayer().apply {
      setOnCompletionListener { onPageAudioComplete() }
      setOnErrorListener { _, _, _ ->
        _uiState.value = _uiState.value.copy(isPlaying = false)
        true
      }
      setOnPreparedListener { mp ->
        _uiState.value = _uiState.value.copy(durationMs = mp.duration.toLong().coerceAtLeast(1L))
        mp.start()
        startProgressTracking()
      }
      try {
        setDataSource(audioUrl)
        prepareAsync()
      } catch (_: Exception) {
        _uiState.value = _uiState.value.copy(isPlaying = false)
      }
    }
  }

  private fun simulatePlayback() {
    val state = _uiState.value
    val totalMs = 5000L
    _uiState.value = state.copy(isPlaying = true, durationMs = totalMs)
    progressJob?.cancel()
    progressJob = viewModelScope.launch {
      val intervalMs = 32L
      val step = 100f / (totalMs / intervalMs.toFloat())
      var current = state.progress
      var elapsedMs = (current / 100f * totalMs).toLong()
      while (current < 100f) {
        delay(intervalMs)
        elapsedMs = (elapsedMs + intervalMs).coerceAtMost(totalMs)
        current = (current + step).coerceAtMost(100f)
        _uiState.value = _uiState.value.copy(
          progress = current,
          currentTimeMs = elapsedMs,
        )
      }
      onPageAudioComplete()
    }
  }

  private fun startProgressTracking() {
    progressJob?.cancel()
    progressJob = viewModelScope.launch {
      while (isActive) {
        val mp = mediaPlayer ?: break
        try {
          val totalMs = mp.duration.toLong()
          val posMs = mp.currentPosition.toLong()
          if (totalMs > 0) {
            _uiState.value = _uiState.value.copy(
              progress = (posMs * 100f / totalMs).coerceIn(0f, 100f),
              currentTimeMs = posMs.coerceAtLeast(0L),
              durationMs = totalMs,
            )
          }
        } catch (_: IllegalStateException) {
          break
        }
        delay(16)
        ensureActive()
      }
    }
  }

  private fun onPageAudioComplete() {
    val state = _uiState.value
    _uiState.value = state.copy(progress = 100f)

    if (state.canGoNext) {
      goToPage(state.pageIndex + 1, autoPlay = true)
    } else {
      _uiState.value = _uiState.value.copy(isPlaying = false)
    }
  }

  private fun pause() {
    progressJob?.cancel()
    mediaPlayer?.let { if (it.isPlaying) it.pause() }
    _uiState.value = _uiState.value.copy(isPlaying = false)
  }

  fun goNext() {
    val state = _uiState.value
    if (state.canGoNext) {
      goToPage(state.pageIndex + 1, autoPlay = false)
    }
  }

  fun goPrev() {
    val state = _uiState.value
    if (state.canGoPrev) {
      goToPage(state.pageIndex - 1, autoPlay = false)
    }
  }

  private fun goToPage(index: Int, autoPlay: Boolean) {
    releasePlayer()
    _uiState.value = _uiState.value.copy(
      pageIndex = index, progress = 0f, isPlaying = false,
      currentTimeMs = 0L, durationMs = 0L,
    )
    if (autoPlay) {
      play()
    }
  }

  private fun releasePlayer() {
    progressJob?.cancel()
    mediaPlayer?.apply {
      try { stop() } catch (_: Exception) {}
      release()
    }
    mediaPlayer = null
  }

  override fun onCleared() {
    super.onCleared()
    releasePlayer()
  }
}
