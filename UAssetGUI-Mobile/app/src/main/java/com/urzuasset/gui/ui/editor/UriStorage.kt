package com.urzuasset.gui.ui.editor

import android.content.Context
import android.net.Uri

class UriStorage(context: Context) {
    private val prefs = context.getSharedPreferences("uassetgui_prefs", Context.MODE_PRIVATE)

    fun saveLastUri(uri: Uri) {
        prefs.edit().putString(KEY_LAST_URI, uri.toString()).apply()
    }

    fun readLastUri(): Uri? {
        val raw = prefs.getString(KEY_LAST_URI, null) ?: return null
        return Uri.parse(raw)
    }

    companion object {
        private const val KEY_LAST_URI = "last_opened_uri"
    }
}
