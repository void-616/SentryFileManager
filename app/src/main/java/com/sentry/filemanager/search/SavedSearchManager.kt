/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.search

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

object SavedSearchManager {

    private const val PREF_SAVED_SEARCHES = "sentry_saved_searches"
    private const val MAX_SAVED = 20

    fun getSavedSearches(context: Context): List<SearchFilter> {
        val json = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_SAVED_SEARCHES, "[]") ?: "[]"
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
    }

    fun saveSearch(context: Context, filter: SearchFilter) {
        val current = getSavedSearches(context).toMutableList()
        current.removeAll { it.savedName == filter.savedName }
        current.add(0, filter)
        if (current.size > MAX_SAVED) current.removeAt(current.lastIndex)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(PREF_SAVED_SEARCHES, JSONArray(current.map { toJson(it) }).toString()).apply()
    }

    fun deleteSearch(context: Context, name: String) {
        val current = getSavedSearches(context).toMutableList()
        current.removeAll { it.savedName == name }
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(PREF_SAVED_SEARCHES, JSONArray(current.map { toJson(it) }).toString()).apply()
    }

    private fun toJson(f: SearchFilter): JSONObject = JSONObject().apply {
        put("query", f.query); put("useRegex", f.useRegex); put("caseSensitive", f.caseSensitive)
        put("scope", f.scope.name); put("fileTypeFilter", f.fileTypeFilter.name)
        put("minSizeBytes", f.minSizeBytes); put("maxSizeBytes", f.maxSizeBytes)
        put("modifiedAfterMs", f.modifiedAfterMs); put("modifiedBeforeMs", f.modifiedBeforeMs)
        put("searchContent", f.searchContent); put("contentQuery", f.contentQuery)
        put("savedName", f.savedName)
    }

    private fun fromJson(o: JSONObject): SearchFilter = SearchFilter(
        query = o.optString("query", ""),
        useRegex = o.optBoolean("useRegex", false),
        caseSensitive = o.optBoolean("caseSensitive", false),
        scope = SearchScope.valueOf(o.optString("scope", SearchScope.CURRENT_AND_SUBDIRS.name)),
        fileTypeFilter = FileTypeFilter.valueOf(o.optString("fileTypeFilter", FileTypeFilter.ALL.name)),
        minSizeBytes = o.optLong("minSizeBytes", -1L),
        maxSizeBytes = o.optLong("maxSizeBytes", -1L),
        modifiedAfterMs = o.optLong("modifiedAfterMs", -1L),
        modifiedBeforeMs = o.optLong("modifiedBeforeMs", -1L),
        searchContent = o.optBoolean("searchContent", false),
        contentQuery = o.optString("contentQuery", ""),
        savedName = o.optString("savedName", "")
    )
}
