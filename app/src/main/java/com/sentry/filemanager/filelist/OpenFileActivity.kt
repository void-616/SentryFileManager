/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filelist

import android.content.Intent
import android.os.Bundle
import java8.nio.file.Path
import com.sentry.filemanager.app.AppActivity
import com.sentry.filemanager.app.application
import com.sentry.filemanager.file.MimeType
import com.sentry.filemanager.file.asMimeTypeOrNull
import com.sentry.filemanager.file.fileProviderUri
import com.sentry.filemanager.filejob.FileJobService
import com.sentry.filemanager.provider.archive.isArchivePath
import com.sentry.filemanager.util.createViewIntent
import com.sentry.filemanager.util.extraPath
import com.sentry.filemanager.util.startActivitySafe

class OpenFileActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val path = intent.extraPath
        val mimeType = intent.type?.asMimeTypeOrNull()
        if (path != null && mimeType != null) {
            openFile(path, mimeType)
        }
        finish()
    }

    private fun openFile(path: Path, mimeType: MimeType) {
        if (path.isArchivePath) {
            FileJobService.open(path, mimeType, false, this)
        } else {
            val intent = path.fileProviderUri.createViewIntent(mimeType)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .apply { extraPath = path }
            startActivitySafe(intent)
        }
    }

    companion object {
        private const val ACTION_OPEN_FILE = "com.sentry.filemanager.intent.action.OPEN_FILE"

        fun createIntent(path: Path, mimeType: MimeType): Intent =
            Intent(ACTION_OPEN_FILE)
                .setPackage(application.packageName)
                .setType(mimeType.value)
                .apply { extraPath = path }
    }
}
