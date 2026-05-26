package com.hope.echo.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.R
import com.hope.echo.data.api.RetrofitClient
import com.hope.echo.data.api.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class EditProfileUiState(
  val name: String = "",
  val avatar: String = "",
  val birthday: String = "",
  val isLoading: Boolean = true,
  val isSaving: Boolean = false,
  val saveSuccess: Boolean = false,
  val error: String? = null,
)

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(EditProfileUiState())
  val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

  init {
    loadProfile()
  }

  private fun loadProfile() {
    viewModelScope.launch {
      try {
        val resp = RetrofitClient.userApi.getProfile()
        if (resp.isSuccess() && resp.data != null) {
          _uiState.update {
            it.copy(
              name = resp.data.name,
              avatar = resp.data.avatar,
              birthday = resp.data.birthday ?: "",
              isLoading = false,
            )
          }
        } else {
          _uiState.update { it.copy(isLoading = false, error = resp.message) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = getApplication<Application>().getString(R.string.edit_profile_load_failed)) }
      }
    }
  }

  fun onNameChange(name: String) {
    _uiState.update { it.copy(name = name) }
  }

  fun onBirthdayChange(birthday: String) {
    _uiState.update { it.copy(birthday = birthday) }
  }

  fun uploadAvatar(context: Context, uri: Uri) {
    viewModelScope.launch {
      _uiState.update { it.copy(error = null) }
      try {
        val bytes = withContext(Dispatchers.IO) {
          context.contentResolver.openInputStream(uri)?.readBytes()
        } ?: return@launch
        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(contentType.toMediaType())
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)
        val resp = RetrofitClient.userApi.uploadAvatar(part)
        if (resp.isSuccess() && resp.data != null) {
          _uiState.update { it.copy(avatar = resp.data.avatarUrl) }
        } else {
          _uiState.update { it.copy(error = resp.message) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = getApplication<Application>().getString(R.string.edit_profile_avatar_upload_failed)) }
      }
    }
  }

  fun save() {
    val state = _uiState.value
    if (state.name.isBlank()) {
      _uiState.update { it.copy(error = getApplication<Application>().getString(R.string.edit_profile_nickname_empty)) }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isSaving = true, error = null) }
      try {
        val request = UpdateProfileRequest(
          name = state.name.trim(),
          birthday = state.birthday.ifBlank { null },
        )
        val resp = RetrofitClient.userApi.updateProfile(request)
        if (resp.isSuccess()) {
          _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        } else {
          _uiState.update { it.copy(isSaving = false, error = resp.message) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isSaving = false, error = getApplication<Application>().getString(R.string.edit_profile_save_failed)) }
      }
    }
  }
}
