/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.provider.archive

import android.content.Context
import java8.nio.file.Path
import com.sentry.filemanager.fileaction.ArchivePasswordDialogActivity
import com.sentry.filemanager.fileaction.ArchivePasswordDialogFragment
import com.sentry.filemanager.provider.common.UserAction
import com.sentry.filemanager.provider.common.UserActionRequiredException
import com.sentry.filemanager.util.createIntent
import com.sentry.filemanager.util.putArgs
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ArchivePasswordRequiredException(
    private val file: Path,
    reason: String?
) :
    UserActionRequiredException(file.toString(), null, reason) {

    override fun getUserAction(continuation: Continuation<Boolean>, context: Context): UserAction {
        return UserAction(
            ArchivePasswordDialogActivity::class.createIntent().putArgs(
                ArchivePasswordDialogFragment.Args(file) { continuation.resume(it) }
            ), ArchivePasswordDialogFragment.getTitle(context),
            ArchivePasswordDialogFragment.getMessage(file, context)
        )
    }
}
