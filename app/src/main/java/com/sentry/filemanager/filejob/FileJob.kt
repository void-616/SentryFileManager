/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filejob

import android.os.Handler
import android.os.Looper
import com.sentry.filemanager.util.showToast
import java.io.IOException
import java.io.InterruptedIOException
import java.util.Random

abstract class FileJob {
    val id = Random().nextInt()

    internal lateinit var service: FileJobService
        private set

    fun runOn(service: FileJobService) {
        this.service = service
        try {
            run()
            // TODO: Toast
        } catch (e: InterruptedIOException) {
            // TODO
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            service.showToast(e.toString())
        } finally {
            service.notificationManager.cancel(id)
            Handler(Looper.getMainLooper()).post {
                FileJobProgressLiveData.postValue(null)
                FileJobProgressActivity.userDismissed = false
            }
        }
    }

    @Throws(IOException::class)
    protected abstract fun run()
}
