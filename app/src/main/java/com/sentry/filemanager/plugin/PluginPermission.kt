package com.sentry.filemanager.plugin

enum class PluginPermission(val label: String, val description: String, val dangerous: Boolean) {
    READ_STORAGE("Read storage", "Read files on device storage", false),
    WRITE_STORAGE("Write storage", "Write and modify files on device storage", false),
    NETWORK("Network", "Make network requests", false),
    SHELL("Shell commands", "Run shell commands on the device", true),
    ROOT("Root access", "Request root shell access", true),
    INTER_PLUGIN("Plugin communication", "Communicate with other installed plugins", false)
}
