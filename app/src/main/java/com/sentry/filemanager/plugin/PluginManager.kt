package com.sentry.filemanager.plugin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

object PluginManager {

    private const val PLUGINS_DIR = "plugins"
    private const val SFP_EXTENSION = "sfp"

    // ── Install ───────────────────────────────────────────────────────────────

    sealed class InstallResult {
        data class Success(val plugin: InstalledPlugin) : InstallResult()
        data class Error(val reason: String) : InstallResult()
        data class PermissionRequired(
            val manifest: PluginManifest,
            val dangerous: List<PluginPermission>
        ) : InstallResult()
    }

    fun installFromFile(context: Context, sfpFile: File): InstallResult {
        if (!sfpFile.exists()) return InstallResult.Error("File not found")
        if (sfpFile.extension != SFP_EXTENSION) return InstallResult.Error("Not a .sfp file")

        return try {
            val zip = ZipFile(sfpFile)

            // Read manifest
            val manifestEntry = zip.getEntry("manifest.json")
                ?: return InstallResult.Error("Missing manifest.json")
            val manifestJson = zip.getInputStream(manifestEntry).bufferedReader().readText()
            val manifest = PluginManifest.fromJson(manifestJson)
                ?: return InstallResult.Error("Invalid manifest.json")

            // Check min app version
            if (manifest.minAppVersion > getAppVersionCode(context)) {
                return InstallResult.Error("Requires app version ${manifest.minAppVersion} or higher")
            }

            // Check for dangerous permissions
            val dangerous = manifest.permissions.filter { it.dangerous }
            if (dangerous.isNotEmpty()) {
                return InstallResult.PermissionRequired(manifest, dangerous)
            }

            // Extract and install
            extractAndInstall(context, zip, manifest, manifest.permissions)
        } catch (e: Exception) {
            InstallResult.Error(e.message ?: "Unknown error during install")
        }
    }

    fun installWithPermissions(
        context: Context,
        sfpFile: File,
        grantedPermissions: List<PluginPermission>
    ): InstallResult {
        return try {
            val zip = ZipFile(sfpFile)
            val manifestEntry = zip.getEntry("manifest.json")
                ?: return InstallResult.Error("Missing manifest.json")
            val manifestJson = zip.getInputStream(manifestEntry).bufferedReader().readText()
            val manifest = PluginManifest.fromJson(manifestJson)
                ?: return InstallResult.Error("Invalid manifest.json")
            extractAndInstall(context, zip, manifest, grantedPermissions)
        } catch (e: Exception) {
            InstallResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun extractAndInstall(
        context: Context,
        zip: ZipFile,
        manifest: PluginManifest,
        grantedPermissions: List<PluginPermission>
    ): InstallResult {
        val pluginsRoot = File(context.filesDir, PLUGINS_DIR)
        val pluginDir = File(pluginsRoot, manifest.id).also { it.mkdirs() }

        // Extract all entries
        zip.entries().asSequence().forEach { entry ->
            val outFile = File(pluginDir, entry.name)
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
            }
        }
        zip.close()

        val dexFile = File(pluginDir, "plugin.dex")
        if (!dexFile.exists()) {
            pluginDir.deleteRecursively()
            return InstallResult.Error("Missing plugin.dex")
        }

        val plugin = InstalledPlugin(
            manifest = manifest,
            pluginDir = pluginDir.absolutePath,
            dexPath = dexFile.absolutePath,
            enabled = true,
            grantedPermissions = grantedPermissions
        )

        PluginStore.savePlugin(context, plugin)
        return InstallResult.Success(plugin)
    }

    // ── Uninstall ─────────────────────────────────────────────────────────────

    fun uninstall(context: Context, id: String): Boolean {
        val plugin = PluginStore.getPlugin(context, id) ?: return false
        File(plugin.pluginDir).deleteRecursively()
        PluginStore.removePlugin(context, id)
        return true
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    fun getInstalledPlugins(context: Context): List<InstalledPlugin> =
        PluginStore.getPlugins(context)

    fun getEnabledPlugins(context: Context): List<InstalledPlugin> =
        PluginStore.getPlugins(context).filter { it.enabled }

    fun setEnabled(context: Context, id: String, enabled: Boolean) =
        PluginStore.setEnabled(context, id, enabled)

    // ── Update check ──────────────────────────────────────────────────────────

    fun getUpdateCheckUrl(plugin: InstalledPlugin): String? {
        val url = plugin.manifest.sourceUrl
        if (url.isEmpty()) return null
        // Construct GitHub releases API URL from repo URL
        return if (url.contains("github.com")) {
            url.replace("github.com", "api.github.com/repos")
                .trimEnd('/') + "/releases/latest"
        } else null
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getAppVersionCode(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: Exception) { 1 }
    }

    fun getPluginsDir(context: Context): File =
        File(context.filesDir, PLUGINS_DIR).also { it.mkdirs() }
}
