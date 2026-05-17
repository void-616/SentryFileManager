package com.sentry.filemanager.plugin

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

object PluginStore {

    private const val PREF_PLUGINS = "sentry_installed_plugins"

    fun getPlugins(context: Context): List<InstalledPlugin> {
        val json = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_PLUGINS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { pluginFromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
    }

    fun getPlugin(context: Context, id: String): InstalledPlugin? =
        getPlugins(context).find { it.id == id }

    fun savePlugin(context: Context, plugin: InstalledPlugin) {
        val plugins = getPlugins(context).toMutableList()
        val idx = plugins.indexOfFirst { it.id == plugin.id }
        if (idx >= 0) plugins[idx] = plugin else plugins.add(plugin)
        persist(context, plugins)
    }

    fun removePlugin(context: Context, id: String) {
        persist(context, getPlugins(context).filter { it.id != id })
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean) {
        val plugins = getPlugins(context).toMutableList()
        val idx = plugins.indexOfFirst { it.id == id }
        if (idx >= 0) plugins[idx] = plugins[idx].copy(enabled = enabled)
        persist(context, plugins)
    }

    fun grantPermissions(context: Context, id: String, permissions: List<PluginPermission>) {
        val plugins = getPlugins(context).toMutableList()
        val idx = plugins.indexOfFirst { it.id == id }
        if (idx >= 0) plugins[idx] = plugins[idx].copy(grantedPermissions = permissions)
        persist(context, plugins)
    }

    private fun persist(context: Context, plugins: List<InstalledPlugin>) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(PREF_PLUGINS, JSONArray(plugins.map { pluginToJson(it) }).toString())
            .apply()
    }

    private fun pluginToJson(p: InstalledPlugin): JSONObject = JSONObject().apply {
        put("manifest", p.manifest.let { m -> JSONObject().apply {
            put("id", m.id); put("name", m.name); put("version", m.version)
            put("minAppVersion", m.minAppVersion); put("entryPoint", m.entryPoint)
            put("permissions", JSONArray(m.permissions.map { it.name }))
            put("description", m.description); put("author", m.author)
            put("sourceUrl", m.sourceUrl)
        }})
        put("pluginDir", p.pluginDir); put("dexPath", p.dexPath)
        put("enabled", p.enabled); put("installedAt", p.installedAt)
        put("grantedPermissions", JSONArray(p.grantedPermissions.map { it.name }))
    }

    private fun pluginFromJson(o: JSONObject): InstalledPlugin? {
        return try {
            val m = o.getJSONObject("manifest")
            val manifest = PluginManifest(
                id = m.optString("id"),
                name = m.optString("name"),
                version = m.optString("version", "1.0.0"),
                minAppVersion = m.optInt("minAppVersion", 1),
                entryPoint = m.optString("entryPoint"),
                permissions = (m.optJSONArray("permissions") ?: JSONArray()).let { arr ->
                    (0 until arr.length()).mapNotNull {
                        try { PluginPermission.valueOf(arr.getString(it)) } catch (e: Exception) { null }
                    }
                },
                description = m.optString("description", ""),
                author = m.optString("author", ""),
                sourceUrl = m.optString("sourceUrl", "")
            )
            InstalledPlugin(
                manifest = manifest,
                pluginDir = o.optString("pluginDir"),
                dexPath = o.optString("dexPath"),
                enabled = o.optBoolean("enabled", true),
                installedAt = o.optLong("installedAt"),
                grantedPermissions = (o.optJSONArray("grantedPermissions") ?: JSONArray()).let { arr ->
                    (0 until arr.length()).mapNotNull {
                        try { PluginPermission.valueOf(arr.getString(it)) } catch (e: Exception) { null }
                    }
                }
            )
        } catch (e: Exception) { null }
    }
}
