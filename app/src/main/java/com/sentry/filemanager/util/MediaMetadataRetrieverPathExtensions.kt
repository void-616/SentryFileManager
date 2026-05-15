/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.util

import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.Path
import com.sentry.filemanager.provider.common.newByteChannel
import com.sentry.filemanager.provider.document.isDocumentPath
import com.sentry.filemanager.provider.document.resolver.DocumentResolver
import com.sentry.filemanager.provider.ftp.isFtpPath
import com.sentry.filemanager.provider.linux.isLinuxPath
import java.io.IOException
import java.nio.ByteBuffer

val Path.isMediaMetadataRetrieverCompatible: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        !isFtpPath
    } else {
        isLinuxPath || isDocumentPath
    }

fun MediaMetadataRetriever.setDataSource(path: Path) {
    when {
        path.isLinuxPath -> setDataSource(path.toFile().path)
        path.isDocumentPath ->
            DocumentResolver.openParcelFileDescriptor(path as DocumentResolver.Path, "r")
                .use { pfd -> setDataSource(pfd.fileDescriptor) }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            val channel = try {
                path.newByteChannel()
            } catch (e: IOException) {
                throw IllegalArgumentException(e)
            }
            setDataSource(PathMediaDataSource(channel))
        }
        else -> throw IllegalArgumentException(path.toString())
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class PathMediaDataSource(private val channel: SeekableByteChannel) : MediaDataSource() {
    @Throws(IOException::class)
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        channel.position(position)
        return channel.read(ByteBuffer.wrap(buffer, offset, size))
    }

    @Throws(IOException::class)
    override fun getSize(): Long {
        return channel.size()
    }

    @Throws(IOException::class)
    override fun close() {
        channel.close()
    }
}
