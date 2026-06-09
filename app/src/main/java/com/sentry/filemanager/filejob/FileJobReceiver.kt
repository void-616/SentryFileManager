/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filejob

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sentry.filemanager.app.application

class FileJobReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            ACTION_CANCEL -> {
                val jobId = intent.getIntExtra(EXTRA_JOB_ID, 0)
                FileJobService.cancelJob(jobId)
            }
            ACTION_SHOW -> {
                FileJobProgressActivity.userDismissed = false
                FileJobProgressActivity.start(context)
            }
            else -> throw IllegalArgumentException(action)
        }
    }

    companion object {
        private const val ACTION_CANCEL = "cancel"
        private const val ACTION_SHOW = "show"

        private const val EXTRA_JOB_ID = "jobId"

        fun createIntent(jobId: Int): Intent =
            Intent(application, FileJobReceiver::class.java)
                .setAction(ACTION_CANCEL)
                .putExtra(EXTRA_JOB_ID, jobId)

        fun createShowIntent(context: Context): Intent =
            Intent(context, FileJobReceiver::class.java)
                .setAction(ACTION_SHOW)
    }
}
