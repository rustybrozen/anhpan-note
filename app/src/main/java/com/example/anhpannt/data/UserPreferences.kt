package com.example.anhpannt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(
    private val context: Context
) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("key_onboarding_completed")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("key_selected_language")
        val KEY_AUTO_SYNC = booleanPreferencesKey("key_auto_sync")
        val KEY_USER_EMAIL = stringPreferencesKey("key_user_email")
        val KEY_USER_ID = stringPreferencesKey("key_user_id")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.userDataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val selectedLanguage: Flow<String> = context.userDataStore.data.map { preferences ->
        preferences[KEY_SELECTED_LANGUAGE] ?: "vi"
    }

    val isAutoSync: Flow<Boolean> = context.userDataStore.data.map { preferences ->
        preferences[KEY_AUTO_SYNC] ?: true
    }

    val userEmail: Flow<String?> = context.userDataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    val userId: Flow<String?> = context.userDataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }




    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }




    suspend fun setLanguage(languageCode: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_SELECTED_LANGUAGE] = languageCode
        }
    }




    suspend fun setAutoSync(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_AUTO_SYNC] = enabled
        }
    }




    suspend fun saveUserSession(id: String, email: String) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
            preferences[KEY_USER_EMAIL] = email
        }
    }




    suspend fun clearUserSession() {
        context.userDataStore.edit { preferences ->
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
        }
    }
}
