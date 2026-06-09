/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

@file:Suppress("DEPRECATION")
package com.sentry.filemanager.fileproperties


import android.os.AsyncTask
import java8.nio.file.Path
import com.sentry.filemanager.file.FileItem
import com.sentry.filemanager.file.loadFileItem
import com.sentry.filemanager.util.Failure
import com.sentry.filemanager.util.Loading
import com.sentry.filemanager.util.Stateful
import com.sentry.filemanager.util.Success
import com.sentry.filemanager.util.valueCompat

class FileLiveData private constructor(
    path: Path,
    file: FileItem?
) : PathObserverLiveData<Stateful<FileItem>>(path) {
    constructor(path: Path) : this(path, null)

    constructor(file: FileItem) : this(file.path, file)

    init {
        if (file != null) {
            value = Success(file)
        } else {
            loadValue()
        }
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val file = path.loadFileItem()
                Success(file)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
