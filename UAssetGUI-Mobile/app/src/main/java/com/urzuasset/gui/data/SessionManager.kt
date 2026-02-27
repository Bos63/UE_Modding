package com.urzuasset.gui.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("uassetgui_session", Context.MODE_PRIVATE)

    fun saveSessionKey(key: String) {
        prefs.edit().putString(KEY_AUTH, key).apply()
    }

    fun getSessionKey(): String? = prefs.getString(KEY_AUTH, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_AUTH = "auth_key"
    }
}
