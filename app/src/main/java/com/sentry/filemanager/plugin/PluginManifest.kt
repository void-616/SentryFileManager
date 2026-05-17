package com.sentry.filemanager.plugin

import org.json.JSONObject

data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val minAppVersion: Int,
    val entryPoint: String,
    val permissions: List<PluginPermission>,
    val description: String = "",
    val author: String = "",
    val sourceUrl: String = ""
) {
    companion object {
        fun fromJson(json: String): PluginManifest? {
            return try {
                val o = JSONObject(json)
                PluginManifest(
                    id = o.optString("id").ifEmpty { return null },
                    name = o.optString("name").ifEmpty { return null },
                    version = o.optString("version", "1.0.0"),
                    minAppVersion = o.optInt("min_app_version", 1),
                    entryPoint = o.optString("entry_point").ifEmpty { return null },
                    permissions = (o.optJSONArray("permissions") ?: org.json.JSONArray()).let { arr ->
                        (0 until arr.length()).mapNotNull {
                            try { PluginPermission.valueOf(arr.getString(it)) } catch (e: Exception) { null }
                        }
                    },
                    description = o.optString("description", ""),
                    author = o.optString("author", ""),
                    sourceUrl = o.optString("source_url", "")
                )
            } catch (e: Exception) { null }
        }
    }
}
