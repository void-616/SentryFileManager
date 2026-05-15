/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.filelist

import java8.nio.file.Path
import com.sentry.filemanager.file.MimeType
import com.sentry.filemanager.file.isSupportedArchive
import com.sentry.filemanager.provider.archive.archiveFile
import com.sentry.filemanager.provider.archive.isArchivePath
import com.sentry.filemanager.provider.document.isDocumentPath
import com.sentry.filemanager.provider.document.resolver.DocumentResolver
import com.sentry.filemanager.provider.linux.isLinuxPath

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

val Path.isLocalPath: Boolean
    get() =
        isLinuxPath || (isDocumentPath && DocumentResolver.isLocal(this as DocumentResolver.Path))

val Path.isRemotePath: Boolean
    get() = !isLocalPath
