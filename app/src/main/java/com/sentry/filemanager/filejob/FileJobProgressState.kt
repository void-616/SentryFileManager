/*
 * Copyright (c) 2024 Sentry Project
 * All Rights Reserved.
 */
package com.sentry.filemanager.filejob

data class FileJobProgressEvent(
    val jobId: Int,
    val title: String,
    val currentFile: String,
    val transferredSize: Long,
    val totalSize: Long,
    val percent: Int,
    val transferredFiles: Int,
    val totalFiles: Int,
    val logLines: List<String>,
    val isFinished: Boolean = false
)
