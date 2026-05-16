/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.crash

import android.content.Context
import android.os.Build
import com.sentry.filemanager.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    private const val LOG_DIR = "crash_logs"
    private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val FILE_DATE_FORMAT = "yyyyMMdd_HHmmss"
    private const val SEPARATOR = "─────────────────────────────────────────"

    var retentionDays: Int = 7

    private lateinit var logDir: File

    fun initialize(context: Context) {
        logDir = File(context.filesDir, LOG_DIR).also { it.mkdirs() }
        cleanOldLogs()
        installUncaughtExceptionHandler()
    }

    private fun installUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashLog(throwable, thread.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun writeCrashLog(throwable: Throwable, threadName: String = "unknown") {
        if (!::logDir.isInitialized) return
        val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        val fileTimestamp = SimpleDateFormat(FILE_DATE_FORMAT, Locale.US).format(Date())
        val stackTrace = getStackTrace(throwable)

        val report = buildString {
            appendLine("SentryFM Crash Report")
            appendLine(SEPARATOR)
            appendLine("Time:       $timestamp")
            appendLine("Version:    ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Android:    ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Device:     ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread:     $threadName")
            appendLine()
            appendLine("Exception:  ${throwable.javaClass.name}")
            appendLine("Message:    ${throwable.message ?: "No message"}")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace)
            appendLine(SEPARATOR)
        }

        val file = File(logDir, "crash_${fileTimestamp}.txt")
        file.writeText(report)
    }

    fun getLogs(): List<CrashLogEntry> {
        if (!::logDir.isInitialized) return emptyList()
        return logDir.listFiles { f -> f.extension == "txt" }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { file ->
                try {
                    val content = file.readText()
                    CrashLogEntry(
                        fileName = file.name,
                        timestamp = extractField(content, "Time:"),
                        exceptionType = extractField(content, "Exception:"),
                        message = extractField(content, "Message:"),
                        fullText = content
                    )
                } catch (e: Exception) { null }
            } ?: emptyList()
    }

    fun getLogCount(): Int {
        if (!::logDir.isInitialized) return 0
        return logDir.listFiles { f -> f.extension == "txt" }?.size ?: 0
    }

    fun deleteLog(fileName: String): Boolean {
        if (!::logDir.isInitialized) return false
        return File(logDir, fileName).delete()
    }

    fun deleteAllLogs(): Int {
        if (!::logDir.isInitialized) return 0
        val files = logDir.listFiles { f -> f.extension == "txt" } ?: return 0
        var count = 0
        files.forEach { if (it.delete()) count++ }
        return count
    }

    fun exportAllLogs(): String = getLogs().joinToString("\n\n") { it.fullText }

    private fun cleanOldLogs() {
        if (!::logDir.isInitialized) return
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        logDir.listFiles { f -> f.extension == "txt" && f.lastModified() < cutoff }
            ?.forEach { it.delete() }
    }

    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    private fun extractField(content: String, label: String): String {
        return content.lines()
            .firstOrNull { it.trimStart().startsWith(label) }
            ?.substringAfter(label)?.trim() ?: "Unknown"
    }
}
