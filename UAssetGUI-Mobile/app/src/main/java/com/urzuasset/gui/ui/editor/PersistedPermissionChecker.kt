package com.urzuasset.gui.ui.editor

import android.content.Context
import android.net.Uri

object PersistedPermissionChecker {
    fun hasReadPermission(context: Context, uri: Uri): Boolean {
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }
    }
}
