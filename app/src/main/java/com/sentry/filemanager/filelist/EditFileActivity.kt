/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filelist

import android.os.Bundle
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import com.sentry.filemanager.app.AppActivity
import com.sentry.filemanager.file.MimeType
import com.sentry.filemanager.file.fileProviderUri
import com.sentry.filemanager.util.ParcelableArgs
import com.sentry.filemanager.util.ParcelableParceler
import com.sentry.filemanager.util.args
import com.sentry.filemanager.util.createEditIntent
import com.sentry.filemanager.util.startActivitySafe

// Use a trampoline activity so that we can have a proper icon and title.
class EditFileActivity : AppActivity() {
    private val args by args<Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivitySafe(args.path.fileProviderUri.createEditIntent(args.mimeType))
        finish()
    }

    @Parcelize
    class Args(
        val path: @WriteWith<ParcelableParceler> Path,
        val mimeType: MimeType
    ) : ParcelableArgs
}
