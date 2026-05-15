/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.navigation

import com.sentry.filemanager.settings.Settings
import com.sentry.filemanager.util.removeFirst
import com.sentry.filemanager.util.valueCompat

object BookmarkDirectories {
    fun add(bookmarkDirectory: BookmarkDirectory) {
        val bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.valueCompat.toMutableList()
            .apply { add(bookmarkDirectory) }
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories)
    }

    fun move(fromPosition: Int, toPosition: Int) {
        val bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.valueCompat.toMutableList()
            .apply { add(toPosition, removeAt(fromPosition)) }
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories)
    }

    fun replace(bookmarkDirectory: BookmarkDirectory) {
        val bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.valueCompat.toMutableList()
            .apply { this[indexOfFirst { it.id == bookmarkDirectory.id }] = bookmarkDirectory }
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories)
    }

    fun remove(bookmarkDirectory: BookmarkDirectory) {
        val bookmarkDirectories = Settings.BOOKMARK_DIRECTORIES.valueCompat.toMutableList()
            .apply { removeFirst { it.id == bookmarkDirectory.id } }
        Settings.BOOKMARK_DIRECTORIES.putValue(bookmarkDirectories)
    }
}
