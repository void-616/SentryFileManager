/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

@file:Suppress("DEPRECATION")
package com.sentry.filemanager.fileproperties.video


import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.util.Size
import java.time.Duration
import java8.nio.file.Path
import com.sentry.filemanager.compat.use
import com.sentry.filemanager.fileproperties.PathObserverLiveData
import com.sentry.filemanager.fileproperties.date
import com.sentry.filemanager.fileproperties.extractMetadataNotBlank
import com.sentry.filemanager.fileproperties.location
import com.sentry.filemanager.util.Failure
import com.sentry.filemanager.util.Loading
import com.sentry.filemanager.util.Stateful
import com.sentry.filemanager.util.Success
import com.sentry.filemanager.util.setDataSource
import com.sentry.filemanager.util.valueCompat

class VideoInfoLiveData(path: Path) : PathObserverLiveData<Stateful<VideoInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val videoInfo = MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(path)
                    val title = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_TITLE
                    )
                    val width = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                    )?.toIntOrNull()
                    val height = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                    )?.toIntOrNull()
                    val dimensions = if (width != null && height != null) {
                        Size(width, height)
                    } else {
                        null
                    }
                    val duration = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )?.toLongOrNull()?.let { Duration.ofMillis(it) }
                    val date = retriever.date
                    val location = retriever.location
                    val bitRate = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_BITRATE
                    )?.toLongOrNull()
                    VideoInfo(title, dimensions, duration, date, location, bitRate)
                }
                Success(videoInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
