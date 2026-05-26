package com.hope.echo.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenManager(private val context: Context) {
  companion object {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
  }

  val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

  suspend fun saveToken(token: String) {
    context.dataStore.edit { it[TOKEN_KEY] = token }
  }

  suspend fun clearToken() {
    context.dataStore.edit { it.remove(TOKEN_KEY) }
  }

  suspend fun isLoggedIn(): Boolean = context.dataStore.data.first()[TOKEN_KEY] != null

  suspend fun getToken(): String? = context.dataStore.data.first()[TOKEN_KEY]
}
