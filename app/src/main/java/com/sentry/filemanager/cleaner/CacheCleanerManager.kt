package com.sentry.filemanager.cleaner

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import java.io.File

object CacheCleanerManager {

    private const val PREF_WATCH_FOLDERS = "sentry_cleaner_folders"
    private const val PREF_RETENTION_DAYS = "sentry_cleaner_retention_days"
    private const val DEFAULT_RETENTION_DAYS = 7

    // ── Folder list ───────────────────────────────────────────────────────────

    fun getWatchedFolders(context: Context): List<String> {
        val json = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_WATCH_FOLDERS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }

    fun addFolder(context: Context, path: String) {
        val folders = getWatchedFolders(context).toMutableList()
        if (path !in folders) {
            folders.add(path)
            saveFolders(context, folders)
        }
    }

    fun removeFolder(context: Context, path: String) {
        saveFolders(context, getWatchedFolders(context).filter { it != path })
    }

    private fun saveFolders(context: Context, folders: List<String>) {
        val arr = JSONArray(folders)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(PREF_WATCH_FOLDERS, arr.toString()).apply()
    }

    // ── Retention ─────────────────────────────────────────────────────────────

    fun getRetentionDays(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(PREF_RETENTION_DAYS, DEFAULT_RETENTION_DAYS)

    fun setRetentionDays(context: Context, days: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putInt(PREF_RETENTION_DAYS, days).apply()
    }

    // ── Clean ─────────────────────────────────────────────────────────────────

    data class CleanResult(val filesDeleted: Int, val bytesFreed: Long)

    fun cleanAll(context: Context): CleanResult {
        val retentionDays = getRetentionDays(context)
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        var filesDeleted = 0
        var bytesFreed = 0L

        getWatchedFolders(context).forEach { folderPath ->
            val folder = File(folderPath)
            if (!folder.exists() || !folder.isDirectory) return@forEach
            folder.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff) {
                    val size = file.length()
                    if (file.delete()) {
                        filesDeleted++
                        bytesFreed += size
                    }
                }
            }
        }

        return CleanResult(filesDeleted, bytesFreed)
    }

    fun previewClean(context: Context): CleanResult {
        val retentionDays = getRetentionDays(context)
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        var filesFound = 0
        var bytesTotal = 0L

        getWatchedFolders(context).forEach { folderPath ->
            val folder = File(folderPath)
            if (!folder.exists() || !folder.isDirectory) return@forEach
            folder.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff) {
                    filesFound++
                    bytesTotal += file.length()
                }
            }
        }

        return CleanResult(filesFound, bytesTotal)
    }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "${bytes} B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024 * 1024))} GB"
    }
}
