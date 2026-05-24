/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.crash

data class CrashLogEntry(
    val fileName: String,
    val timestamp: String,
    val exceptionType: String,
    val message: String,
    val fullText: String
)
