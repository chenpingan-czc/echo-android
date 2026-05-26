package com.hope.echo.ui.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.data.api.BatchDeleteVoicesRequest
import com.hope.echo.data.api.RetrofitClient
import com.hope.echo.data.api.VoiceDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class MyVoiceUiState(
  val voices: List<VoiceDto> = emptyList(),
  val isLoading: Boolean = true,
  val isUploading: Boolean = false,
  val playingVoiceId: Long? = null,
  val playProgress: Float = 0f,
  val error: String? = null,
  val promptText: String? = null,
  val pendingDeleteVoiceId: Long? = null,
  val deletingVoiceId: Long? = null,
)

class MyVoiceViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(MyVoiceUiState())
  val uiState: StateFlow<MyVoiceUiState> = _uiState.asStateFlow()

  private var mediaPlayer: MediaPlayer? = null
  private var progressJob: Job? = null

  init {
    loadVoices()
    loadPromptText()
  }

  fun loadVoices() {
    viewModelScope.launch {
      fetchVoices(showLoading = true)
    }
  }

  fun loadPromptText() {
    viewModelScope.launch {
      try {
        val resp = RetrofitClient.userApi.getVoicePromptText()
        _uiState.update { it.copy(promptText = resp.data) }
      } catch (_: Exception) {
      }
    }
  }

  fun playVoice(voice: VoiceDto, context: Context) {
    if (_uiState.value.playingVoiceId == voice.id) {
      stopPlayback()
      return
    }
    stopPlayback()
    try {
      mediaPlayer = MediaPlayer().apply {
        setDataSource(context, Uri.parse(voice.audioUrl))
        prepareAsync()
        setOnPreparedListener { mp ->
          mp.start()
          _uiState.update { it.copy(playingVoiceId = voice.id, playProgress = 0f) }
          startProgressTracking()
        }
        setOnCompletionListener {
          _uiState.update { it.copy(playingVoiceId = null, playProgress = 0f) }
          releasePlayer()
        }
        setOnErrorListener { _, _, _ ->
          _uiState.update { it.copy(playingVoiceId = null, playProgress = 0f) }
          releasePlayer()
          true
        }
      }
    } catch (_: Exception) {
      _uiState.update { it.copy(playingVoiceId = null) }
    }
  }

  fun showDeleteAction(voiceId: Long) {
    if (_uiState.value.deletingVoiceId != null) return
    stopPlayback()
    _uiState.update { it.copy(pendingDeleteVoiceId = voiceId) }
  }

  fun dismissDeleteAction() {
    if (_uiState.value.deletingVoiceId != null) return
    _uiState.update { it.copy(pendingDeleteVoiceId = null) }
  }

  fun deleteVoice(voiceId: Long) {
    if (_uiState.value.deletingVoiceId != null) return
    viewModelScope.launch {
      _uiState.update { it.copy(deletingVoiceId = voiceId) }
      try {
        val resp = RetrofitClient.userApi.deleteVoices(BatchDeleteVoicesRequest(listOf(voiceId)))
        if (resp.isSuccess()) {
          _uiState.update {
            it.copy(
              voices = it.voices.filterNot { voice -> voice.id == voiceId },
              pendingDeleteVoiceId = null,
              deletingVoiceId = null,
            )
          }
          fetchVoices(showLoading = false)
        } else {
          _uiState.update { it.copy(deletingVoiceId = null) }
        }
      } catch (_: Exception) {
        _uiState.update { it.copy(deletingVoiceId = null) }
      }
    }
  }

  private suspend fun fetchVoices(showLoading: Boolean) {
    if (showLoading) {
      _uiState.update { it.copy(isLoading = true, error = null) }
    }
    try {
      val resp = RetrofitClient.userApi.getVoiceList()
      _uiState.update { it.copy(voices = resp.data ?: emptyList(), isLoading = false) }
    } catch (e: Exception) {
      _uiState.update { it.copy(isLoading = false, error = e.message) }
    }
  }

  fun uploadVoice(file: File, name: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      _uiState.update { it.copy(isUploading = true) }
      try {
        val requestFile = file.asRequestBody("audio/wav".toMediaType())
        val audioPart = MultipartBody.Part.createFormData("audio", file.name, requestFile)
        val namePart = name.toRequestBody("text/plain".toMediaType())
        RetrofitClient.userApi.uploadVoice(audioPart, namePart)
        loadVoices()
        onSuccess()
      } catch (_: Exception) {
      } finally {
        _uiState.update { it.copy(isUploading = false) }
      }
    }
  }

  private fun startProgressTracking() {
    progressJob?.cancel()
    progressJob = viewModelScope.launch {
      while (true) {
        val mp = mediaPlayer ?: break
        if (!mp.isPlaying) break
        val progress = mp.currentPosition.toFloat() / mp.duration.coerceAtLeast(1)
        _uiState.update { it.copy(playProgress = progress) }
        delay(200)
      }
    }
  }

  fun stopPlaybackIfAny() {
    stopPlayback()
  }

  private fun stopPlayback() {
    progressJob?.cancel()
    _uiState.update { it.copy(playingVoiceId = null, playProgress = 0f) }
    releasePlayer()
  }

  private fun releasePlayer() {
    mediaPlayer?.apply {
      try {
        if (isPlaying) stop()
        release()
      } catch (_: Exception) {
      }
    }
    mediaPlayer = null
  }

  override fun onCleared() {
    super.onCleared()
    stopPlayback()
  }
}
