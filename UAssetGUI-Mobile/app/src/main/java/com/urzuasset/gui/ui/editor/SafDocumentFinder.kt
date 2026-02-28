package com.urzuasset.gui.ui.editor

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

object SafDocumentFinder {

    data class PairMatch(
        val uassetUri: Uri,
        val uexpUri: Uri,
        val ubulkUri: Uri?
    )

    fun findMatchingPair(contentResolver: ContentResolver, selectedUasset: Uri): PairMatch? {
        val authority = selectedUasset.authority ?: return null
        val selectedDocId = DocumentsContract.getDocumentId(selectedUasset)
        val split = selectedDocId.split(":", limit = 2)
        if (split.size != 2) return null

        val relative = split[1]
        if (!relative.contains("/")) return null
        val parentPath = relative.substringBeforeLast('/')
        val selectedFile = relative.substringAfterLast('/')
        val baseName = selectedFile.substringBeforeLast('.', selectedFile)

        if (!selectedFile.endsWith(".uasset", ignoreCase = true)) return null

        val parentDocId = split[0] + ":" + parentPath
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(selectedUasset, parentDocId)

        var uexpDocId: String? = null
        var ubulkDocId: String? = null

        contentResolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null
        )?.use { c ->
            val idIdx = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIdx = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (c.moveToNext()) {
                val docId = c.getString(idIdx)
                val display = c.getString(nameIdx)
                val displayBase = display.substringBeforeLast('.', display)
                val ext = display.substringAfterLast('.', "")
                if (!displayBase.equals(baseName, ignoreCase = true)) continue

                if (ext.equals("uexp", ignoreCase = true)) uexpDocId = docId
                if (ext.equals("ubulk", ignoreCase = true)) ubulkDocId = docId
            }
        }

        val uexpUri = uexpDocId?.let { DocumentsContract.buildDocumentUri(authority, it) } ?: return null
        val ubulkUri = ubulkDocId?.let { DocumentsContract.buildDocumentUri(authority, it) }

        return PairMatch(
            uassetUri = selectedUasset,
            uexpUri = uexpUri,
            ubulkUri = ubulkUri
        )
    }
}
