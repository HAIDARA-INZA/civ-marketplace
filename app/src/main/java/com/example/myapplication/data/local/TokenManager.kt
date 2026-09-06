package com.example.myapplication.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("devsphere_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val NAME_KEY = stringPreferencesKey("user_name")
        private val EMAIL_KEY = stringPreferencesKey("user_email")
        private val VERIFIED_KEY = booleanPreferencesKey("email_verified")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_pref")
    }

    fun getUserId(): Flow<Int?> = context.dataStore.data.map { it[USER_ID_KEY] }
    fun getToken(): Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    fun getRole(): Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    fun getName(): Flow<String?> = context.dataStore.data.map { it[NAME_KEY] }
    fun getEmail(): Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }
    fun isVerified(): Flow<Boolean> = context.dataStore.data.map { it[VERIFIED_KEY] ?: false }
    fun isOnboardingCompleted(): Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED_KEY] ?: false }
    fun getDarkMode(): Flow<Boolean?> = context.dataStore.data.map { it[DARK_MODE_KEY] }

    suspend fun saveAuthData(userId: Int, token: String, role: String, name: String, email: String, isVerified: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[NAME_KEY] = name
            preferences[EMAIL_KEY] = email
            preferences[VERIFIED_KEY] = isVerified
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(NAME_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(VERIFIED_KEY)
        }
    }
}
