/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.file

import android.content.Context
import android.text.format.Formatter
import com.sentry.filemanager.R
import com.sentry.filemanager.util.getQuantityString

@JvmInline
value class FileSize(val value: Long) {

    /* @see android.text.format.Formatter#formatBytes(Resources, long, int) */
    val isHumanReadableInBytes: Boolean
        get() = value <= 900

    fun formatInBytes(context: Context): String =
        context.getQuantityString(R.plurals.size_in_bytes_format, value.toInt(), value)

    fun formatHumanReadable(context: Context): String {
        val v = value
        return when {
            v >= 1_073_741_824L -> "%.2f GB".format(v / 1_073_741_824.0)
            v >= 1_048_576L     -> "%.2f MB".format(v / 1_048_576.0)
            v >= 1_024L         -> "%.2f KB".format(v / 1_024.0)
            else                -> "$v B"
        }
    }
}

fun Long.asFileSize(): FileSize = FileSize(this)
