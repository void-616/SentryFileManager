/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchFilter(
    val query: String = "",
    val useRegex: Boolean = false,
    val caseSensitive: Boolean = false,
    val scope: SearchScope = SearchScope.CURRENT_AND_SUBDIRS,
    val fileTypeFilter: FileTypeFilter = FileTypeFilter.ALL,
    val minSizeBytes: Long = -1L,
    val maxSizeBytes: Long = -1L,
    val modifiedAfterMs: Long = -1L,
    val modifiedBeforeMs: Long = -1L,
    val searchContent: Boolean = false,
    val contentQuery: String = "",
    val savedName: String = ""
) : Parcelable {

    fun isActive(): Boolean = query.isNotEmpty() || contentQuery.isNotEmpty()

    fun matchesName(name: String): Boolean {
        if (query.isEmpty()) return true
        return if (useRegex) {
            try {
                val options = if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
                Regex(query, options).containsMatchIn(name)
            } catch (e: Exception) {
                name.contains(query, ignoreCase = !caseSensitive)
            }
        } else {
            name.contains(query, ignoreCase = !caseSensitive)
        }
    }

    fun matchesSize(sizeBytes: Long): Boolean {
        if (minSizeBytes >= 0 && sizeBytes < minSizeBytes) return false
        if (maxSizeBytes >= 0 && sizeBytes > maxSizeBytes) return false
        return true
    }

    fun matchesDate(lastModifiedMs: Long): Boolean {
        if (modifiedAfterMs >= 0 && lastModifiedMs < modifiedAfterMs) return false
        if (modifiedBeforeMs >= 0 && lastModifiedMs > modifiedBeforeMs) return false
        return true
    }
}

enum class SearchScope { CURRENT_ONLY, CURRENT_AND_SUBDIRS, ENTIRE_STORAGE }

enum class FileTypeFilter { ALL, FILES_ONLY, FOLDERS_ONLY, IMAGES, VIDEO, AUDIO, DOCUMENTS, ARCHIVES }
