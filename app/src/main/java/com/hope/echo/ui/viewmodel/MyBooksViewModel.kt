package com.hope.echo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hope.echo.R
import com.hope.echo.data.api.BookCardDto
import com.hope.echo.data.api.DeleteBookRequest
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
  val pendingDeleteBookId: Long? = null,
  val deletingBookId: Long? = null,
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

  fun showDeleteAction(bookId: Long) {
    if (_uiState.value.deletingBookId != null) return
    _uiState.value = _uiState.value.copy(pendingDeleteBookId = bookId)
  }

  fun dismissDeleteAction() {
    if (_uiState.value.deletingBookId != null) return
    _uiState.value = _uiState.value.copy(pendingDeleteBookId = null)
  }

  fun deleteBook(bookId: Long) {
    if (_uiState.value.deletingBookId != null) return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(deletingBookId = bookId)
      try {
        val response = bookApi.deleteBook(DeleteBookRequest(bookId))
        if (response.isSuccess()) {
          _uiState.value = _uiState.value.copy(
            books = _uiState.value.books.filterNot { it.id == bookId },
            pendingDeleteBookId = null,
            deletingBookId = null,
          )
          fetchBooks(keepExistingOnError = true)
        } else {
          _uiState.value = _uiState.value.copy(
            deletingBookId = null,
          )
        }
      } catch (_: Exception) {
        _uiState.value = _uiState.value.copy(
          deletingBookId = null,
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
