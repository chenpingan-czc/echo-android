package com.hope.echo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.R
import com.hope.echo.data.api.BatchDeleteBookRequest
import com.hope.echo.data.api.BookCardDto
import com.hope.echo.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyBooksUiState(
  val isLoading: Boolean = true,
  val isRefreshing: Boolean = false,
  val books: List<BookCardDto> = emptyList(),
  val errorMessage: String? = null,
  val isEditing: Boolean = false,
  val selectedBookIds: Set<Long> = emptySet(),
  val showDeleteConfirm: Boolean = false,
  val isDeleting: Boolean = false,
)

class MyBooksViewModel(application: Application) : AndroidViewModel(application) {

  private val bookApi = RetrofitClient.bookApi

  private val _uiState = MutableStateFlow(MyBooksUiState())
  val uiState: StateFlow<MyBooksUiState> = _uiState.asStateFlow()

  init {
    load()
  }

  fun load() {
    viewModelScope.launch {
      _uiState.value = MyBooksUiState(isLoading = true)
      fetchBooks(keepExistingOnError = false)
    }
  }

  fun pullRefresh() {
    if (_uiState.value.isRefreshing) return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
      fetchBooks(keepExistingOnError = true)
    }
  }

  fun toggleEditMode() {
    val current = _uiState.value
    if (current.isEditing) {
      _uiState.value = current.copy(isEditing = false, selectedBookIds = emptySet())
    } else {
      _uiState.value = current.copy(isEditing = true, selectedBookIds = emptySet())
    }
  }

  fun toggleBookSelection(bookId: Long) {
    val current = _uiState.value
    val newSelected = current.selectedBookIds.toMutableSet()
    if (newSelected.contains(bookId)) {
      newSelected.remove(bookId)
    } else {
      newSelected.add(bookId)
    }
    _uiState.value = current.copy(selectedBookIds = newSelected)
  }

  fun showDeleteConfirm() {
    if (_uiState.value.selectedBookIds.isNotEmpty()) {
      _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }
  }

  fun dismissDeleteConfirm() {
    _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
  }

  fun confirmBatchDelete() {
    val ids = _uiState.value.selectedBookIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isDeleting = true)
      try {
        val response = bookApi.batchDeleteBooks(BatchDeleteBookRequest(ids))
        if (response.code == 0) {
          // 删除成功：关闭弹窗、退出编辑态，并从服务端重新拉取最新列表
          _uiState.value = _uiState.value.copy(
            selectedBookIds = emptySet(),
            isEditing = false,
            showDeleteConfirm = false,
            isDeleting = false,
          )
          fetchBooks(keepExistingOnError = true)
        } else {
          _uiState.value = _uiState.value.copy(
            showDeleteConfirm = false,
            isDeleting = false,
          )
        }
      } catch (_: Exception) {
        _uiState.value = _uiState.value.copy(
          showDeleteConfirm = false,
          isDeleting = false,
        )
      }
    }
  }

  private suspend fun fetchBooks(keepExistingOnError: Boolean) {
    try {
      val response = bookApi.myBooks()
      if (response.code == 0 && response.data != null) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          isRefreshing = false,
          books = response.data,
          errorMessage = null,
        )
      } else {
        val msg = response.message.ifBlank { getApplication<Application>().getString(R.string.my_books_load_failed) }
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          isRefreshing = false,
          books = if (keepExistingOnError) _uiState.value.books else emptyList(),
          errorMessage = if (keepExistingOnError) null else msg,
        )
      }
    } catch (e: Exception) {
      val msg = e.message ?: getApplication<Application>().getString(R.string.my_books_network_error)
      _uiState.value = _uiState.value.copy(
        isLoading = false,
        isRefreshing = false,
        books = if (keepExistingOnError) _uiState.value.books else emptyList(),
        errorMessage = if (keepExistingOnError) null else msg,
      )
    }
  }
}
