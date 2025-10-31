package com.example.redcurtainapp

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Simple AuthManager - Handles credential storage and authentication
 */
object AuthManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_USERS_JSON = "users_json" // persisted users (email -> password)

    // In-memory cache hydrated from SharedPreferences; defaults include a test user on first run
    private var users: MutableMap<String, String>? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun loadUsers(context: Context): MutableMap<String, String> {
        users?.let { return it }
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_USERS_JSON, null)
        val map = mutableMapOf<String, String>()
        if (json.isNullOrEmpty()) {
            // seed with a default account on first run for convenience
            map["test@example.com"] = "password123"
            saveUsers(context, map)
        } else {
            val obj = JSONObject(json)
            obj.keys().forEach { key ->
                map[key] = obj.getString(key)
            }
        }
        users = map
        return map
    }

    private fun saveUsers(context: Context, map: Map<String, String>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        getPrefs(context).edit().putString(KEY_USERS_JSON, obj.toString()).apply()
        users = map.toMutableMap()
    }

    /**
     * Save login state after successful authentication
     */
    fun saveLoginState(
        context: Context,
        email: String,
        rememberMe: Boolean,
        token: String
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_AUTH_TOKEN, token)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    /**
     * Get saved email for Remember Me functionality
     */
    fun getRememberedEmail(context: Context): String? {
        val prefs = getPrefs(context)
        return if (prefs.getBoolean(KEY_REMEMBER_ME, false)) {
            prefs.getString(KEY_USER_EMAIL, null)
        } else null
    }

    /**
     * Check if Remember Me was enabled
     */
    fun isRememberMeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REMEMBER_ME, false)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Get current user's email
     */
    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }

    /**
     * Get current auth token
     */
    fun getAuthToken(context: Context): String? {
        return getPrefs(context).getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Logout user
     */
    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /**
     * Register a new user
     */
    fun registerUser(context: Context, email: String, password: String): Boolean {
        val store = loadUsers(context)
        if (store.containsKey(email)) {
            return false // User already exists
        }
        store[email] = password
        saveUsers(context, store)
        return true
    }

    /**
     * Validate login credentials
     */
    fun validateCredentials(context: Context, email: String, password: String): Boolean {
        val store = loadUsers(context)
        return store[email] == password
    }

    /**
     * Generate a simple auth token
     */
    fun generateAuthToken(email: String): String {
        return "token_${email.hashCode()}_${System.currentTimeMillis()}"
    }
}