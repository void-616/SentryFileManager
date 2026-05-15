/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java8.nio.file.Path
import com.sentry.filemanager.filelist.FileSortOptions.By
import com.sentry.filemanager.filelist.FileSortOptions.Order
import com.sentry.filemanager.settings.PathSettings
import com.sentry.filemanager.settings.SettingLiveData
import com.sentry.filemanager.settings.Settings
import com.sentry.filemanager.util.valueCompat

class FileSortOptionsLiveData(pathLiveData: LiveData<Path>) : MediatorLiveData<FileSortOptions>() {
    private lateinit var pathSortOptionsLiveData: SettingLiveData<FileSortOptions?>

    private fun loadValue() {
        if (!this::pathSortOptionsLiveData.isInitialized) {
            // Not yet initialized.
            return
        }
        val value = pathSortOptionsLiveData.value ?: Settings.FILE_LIST_SORT_OPTIONS.valueCompat
        if (this.value != value) {
            this.value = value
        }
    }

    fun putBy(by: By) {
        putValue(valueCompat.copy(by = by))
    }

    fun putOrder(order: Order) {
        putValue(valueCompat.copy(order = order))
    }

    fun putIsDirectoriesFirst(isDirectoriesFirst: Boolean) {
        putValue(valueCompat.copy(isDirectoriesFirst = isDirectoriesFirst))
    }

    private fun putValue(value: FileSortOptions) {
        if (pathSortOptionsLiveData.value != null) {
            pathSortOptionsLiveData.putValue(value)
        } else {
            Settings.FILE_LIST_SORT_OPTIONS.putValue(value)
        }
    }

    init {
        addSource(Settings.FILE_LIST_SORT_OPTIONS) { loadValue() }
        addSource(pathLiveData) { path: Path ->
            if (this::pathSortOptionsLiveData.isInitialized) {
                removeSource(pathSortOptionsLiveData)
            }
            pathSortOptionsLiveData = PathSettings.getFileListSortOptions(path)
            addSource(pathSortOptionsLiveData) { loadValue() }
        }
    }
}
