/*
 * Copyright (c) 2026 eZee + Claude
 * SentryOS Project
 */

package com.sentry.filemanager.tabs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TabEntry(
    val id: String,           // unique ID for fragment tag
    var label: String,        // display name (editable)
    var path: String? = null  // last known path for restoration
) : Parcelable
