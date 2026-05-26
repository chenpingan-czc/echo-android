package com.hope.echo.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hope.echo.data.api.BookVoiceDto
import kotlinx.coroutines.flow.first

private val Context.uploadPrefsDataStore by preferencesDataStore(name = "upload_prefs")

class UploadPreferences(private val context: Context) {
  companion object {
    private val LAST_VOICE_ID = stringPreferencesKey("last_upload_voice_id")
  }

  suspend fun getLastVoiceId(): String? {
    val raw = context.uploadPrefsDataStore.data.first()[LAST_VOICE_ID]
    return raw?.takeIf { it.isNotEmpty() }
  }

  suspend fun setLastVoiceId(voiceId: String?) {
    context.uploadPrefsDataStore.edit { prefs ->
      if (voiceId.isNullOrEmpty()) {
        prefs.remove(LAST_VOICE_ID)
      } else {
        prefs[LAST_VOICE_ID] = voiceId
      }
    }
  }
}

/** 在列表中解析应选中的 voiceId：优先上次保存且在列表中存在，否则第一个。 */
fun resolvePreferredVoiceId(voices: List<BookVoiceDto>, storedId: String?): String? {
  if (voices.isEmpty()) return null
  if (!storedId.isNullOrEmpty()) {
    val hit = voices.firstOrNull { (it.voiceId ?: "") == storedId }
    if (hit != null) return hit.voiceId
  }
  return voices.first().voiceId
}
