package com.example.eatstoday.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eatstoday_settings")

class LocalDataStore(context: Context) {
    private val store = context.applicationContext.dataStore

    private object Keys {
        val LOGGED_IN = booleanPreferencesKey("logged_in")
        val SEEDED = booleanPreferencesKey("seeded")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val AVATAR = stringPreferencesKey("avatar_path")
        val PERMISSIONS_SEEN = booleanPreferencesKey("permissions_seen")
        val PREMIUM = booleanPreferencesKey("premium")
        val CUISINES = stringSetPreferencesKey("cuisines")
        val SKILL = stringPreferencesKey("skill")
        val HOUSEHOLD = stringPreferencesKey("household")
        val THEME = stringPreferencesKey("theme")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val isLoggedIn: Flow<Boolean> = store.data.map { it[Keys.LOGGED_IN] ?: false }

    val user: Flow<User> = store.data.map {
        User(
            name = it[Keys.NAME] ?: "Nguyễn An",
            email = it[Keys.EMAIL].orEmpty(),
            isPremium = it[Keys.PREMIUM] ?: false,
            avatarPath = it[Keys.AVATAR],
        )
    }

    val preferences: Flow<UserPreferences> = store.data.map {
        UserPreferences(
            cuisines = it[Keys.CUISINES] ?: emptySet(),
            skill = it[Keys.SKILL] ?: "Mới học",
            householdSize = it[Keys.HOUSEHOLD] ?: "2-3",
        )
    }

    val settings: Flow<AppSettings> = store.data.map {
        AppSettings(
            theme = enumOrDefault(it[Keys.THEME], AppThemeMode.LIGHT),
            textSize = enumOrDefault(it[Keys.TEXT_SIZE], TextSizeOption.MEDIUM),
            notificationsEnabled = it[Keys.NOTIFICATIONS] ?: true,
            keepScreenOn = it[Keys.KEEP_SCREEN_ON] ?: true,
        )
    }


    suspend fun setAvatar(path: String?) = edit {
        if (path == null) it.remove(Keys.AVATAR) else it[Keys.AVATAR] = path
    }

    suspend fun permissionsSeen(): Boolean = store.data.first()[Keys.PERMISSIONS_SEEN] ?: false
    suspend fun markPermissionsSeen() = edit { it[Keys.PERMISSIONS_SEEN] = true }

    suspend fun isSeeded(): Boolean = store.data.first()[Keys.SEEDED] ?: false
    suspend fun markSeeded() = edit { it[Keys.SEEDED] = true }

    suspend fun setLoggedIn(email: String?) = edit {
        it[Keys.LOGGED_IN] = true
        if (email != null) it[Keys.EMAIL] = email
    }

    suspend fun logout() = edit { it[Keys.LOGGED_IN] = false }

    suspend fun updateProfile(name: String, email: String) = edit {
        it[Keys.NAME] = name
        it[Keys.EMAIL] = email
    }

    suspend fun setPremium(value: Boolean) = edit { it[Keys.PREMIUM] = value }

    suspend fun savePreferences(value: UserPreferences) = edit {
        it[Keys.CUISINES] = value.cuisines
        it[Keys.SKILL] = value.skill
        it[Keys.HOUSEHOLD] = value.householdSize
    }

    suspend fun setTheme(value: AppThemeMode) = edit { it[Keys.THEME] = value.name }
    suspend fun setTextSize(value: TextSizeOption) = edit { it[Keys.TEXT_SIZE] = value.name }
    suspend fun setNotifications(value: Boolean) = edit { it[Keys.NOTIFICATIONS] = value }
    suspend fun setKeepScreenOn(value: Boolean) = edit { it[Keys.KEEP_SCREEN_ON] = value }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        store.edit { block(it) }
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}
