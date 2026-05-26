package com.hope.echo.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.R
import com.hope.echo.data.UploadPreferences
import com.hope.echo.data.api.BookVoiceDto
import com.hope.echo.data.resolvePreferredVoiceId
import com.hope.echo.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class UploadUiState(
  val selectedImages: List<Uri> = emptyList(),
  val isUploading: Boolean = false,
  val uploadSuccess: Boolean = false,
  val bookId: Long? = null,
  val errorMessage: String? = null,
  val voices: List<BookVoiceDto> = emptyList(),
  val selectedVoiceId: String? = null,
)

class UploadViewModel(application: Application) : AndroidViewModel(application) {

  private val uploadPreferences = UploadPreferences(application.applicationContext)

  private val _uiState = MutableStateFlow(UploadUiState())
  val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

  init {
    loadVoices()
  }

  private fun loadVoices() {
    viewModelScope.launch {
      try {
        val response = RetrofitClient.bookApi.getVoices()
        if (response.code == 0 && response.data != null) {
          val voices = response.data
          val stored = uploadPreferences.getLastVoiceId()
          val resolved = resolvePreferredVoiceId(voices, stored)
          _uiState.value =
            _uiState.value.copy(voices = voices, selectedVoiceId = resolved)
        }
      } catch (_: Exception) {
        // 加载失败时使用默认音色
      }
    }
  }

  fun selectVoice(voiceId: String?) {
    _uiState.value = _uiState.value.copy(selectedVoiceId = voiceId)
    viewModelScope.launch { uploadPreferences.setLastVoiceId(voiceId) }
  }

  fun addImages(uris: List<Uri>) {
    val current = _uiState.value.selectedImages.toMutableList()
    uris.forEach { uri -> if (!current.contains(uri)) current.add(uri) }
    _uiState.value = _uiState.value.copy(selectedImages = current, errorMessage = null)
  }

  fun removeImage(uri: Uri) {
    val current = _uiState.value.selectedImages.toMutableList()
    current.remove(uri)
    _uiState.value = _uiState.value.copy(selectedImages = current)
  }

  fun upload(context: Context) {
    val images = _uiState.value.selectedImages
    if (images.isEmpty()) {
      _uiState.value = _uiState.value.copy(errorMessage = getApplication<Application>().getString(R.string.upload_need_image))
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
      try {
        val parts = images.map { uri -> uriToMultipartPart(context, uri) }
        val voiceId = _uiState.value.selectedVoiceId
        val voicePart =
          voiceId?.let {
            MultipartBody.Part.createFormData("voiceId", it)
          }
        val response = RetrofitClient.bookApi.uploadBook(parts, voicePart)
        if (response.code == 0 && response.data != null) {
          _uiState.value =
            _uiState.value.copy(
              isUploading = false,
              uploadSuccess = true,
              bookId = response.data.bookId,
            )
        } else {
          _uiState.value =
            _uiState.value.copy(
              isUploading = false,
              errorMessage = response.message.ifBlank { getApplication<Application>().getString(R.string.upload_failed_retry) },
            )
        }
      } catch (e: Exception) {
        _uiState.value =
          _uiState.value.copy(isUploading = false, errorMessage = getApplication<Application>().getString(R.string.upload_failed_with_reason, e.message ?: ""))
      }
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }

  private fun uriToMultipartPart(context: Context, uri: Uri): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
    val bytes =
      contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException(getApplication<Application>().getString(R.string.upload_image_read_failed, uri.toString()))
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    val filename = "image_${System.currentTimeMillis()}.jpg"
    return MultipartBody.Part.createFormData("images", filename, requestBody)
  }
}
