package com.sentry.filemanager.util

import android.os.storage.StorageVolume
import com.sentry.filemanager.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
