package com.inventory.system.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val username: Flow<String?> = dataStore.data.map { it[USERNAME_KEY] }

    suspend fun saveToken(token: String, username: String) {
        dataStore.edit {
            it[TOKEN_KEY] = token
            it[USERNAME_KEY] = username
        }
    }

    suspend fun clearToken() {
        dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USERNAME_KEY)
        }
    }
}
