package com.sentry.filemanager.plugin

data class InstalledPlugin(
    val manifest: PluginManifest,
    val pluginDir: String,       // path to extracted plugin folder
    val dexPath: String,         // path to plugin.dex
    val enabled: Boolean = true,
    val installedAt: Long = System.currentTimeMillis(),
    val grantedPermissions: List<PluginPermission> = emptyList()
) {
    val id get() = manifest.id
    val name get() = manifest.name
    val version get() = manifest.version

    fun hasPermission(permission: PluginPermission): Boolean =
        permission in grantedPermissions
}
