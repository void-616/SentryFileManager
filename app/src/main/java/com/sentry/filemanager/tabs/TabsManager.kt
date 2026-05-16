/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.tabs

import java.util.UUID

object TabsManager {

    const val MAX_TABS = 10

    fun createTab(label: String = "New Tab", path: String? = null): TabEntry {
        return TabEntry(
            id = "tab_${UUID.randomUUID().toString().take(8)}",
            label = label,
            path = path
        )
    }

    fun defaultLabel(index: Int): String = "Tab ${index + 1}"
}
