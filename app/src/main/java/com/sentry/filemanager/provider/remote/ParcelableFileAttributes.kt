/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.FileAttribute
import com.sentry.filemanager.compat.readSerializableCompat
import com.sentry.filemanager.provider.common.PosixFileMode
import com.sentry.filemanager.provider.common.PosixFileModeBit
import com.sentry.filemanager.provider.common.toAttribute
import com.sentry.filemanager.util.toEnumSet
import java.io.Serializable

class ParcelableFileAttributes(val value: Array<out FileAttribute<*>>) : Parcelable {
    private constructor(source: Parcel) : this(
        source.readSerializableCompat<List<Set<PosixFileModeBit>>>()!!
            .map { it.toAttribute() }
            .toTypedArray()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        //noinspection unchecked
        val modes = value.map {
            when (val mode = PosixFileMode.fromAttribute(it)) {
                is Serializable -> mode
                else -> mode.toEnumSet()
            }
        }
        dest.writeSerializable(modes as Serializable)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableFileAttributes> {
            override fun createFromParcel(source: Parcel): ParcelableFileAttributes =
                ParcelableFileAttributes(source)

            override fun newArray(size: Int): Array<ParcelableFileAttributes?> = arrayOfNulls(size)
        }
    }
}

fun Array<out FileAttribute<*>>.toParcelable(): ParcelableFileAttributes =
    ParcelableFileAttributes(this)
