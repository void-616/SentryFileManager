/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import coil.key.Keyer
import coil.request.Options
import me.zhanghai.android.appiconloader.AppIconLoader
import com.sentry.filemanager.R
import com.sentry.filemanager.compat.longVersionCodeCompat
import com.sentry.filemanager.util.getDimensionPixelSize
import java.io.Closeable

class AppIconApplicationInfoKeyer : Keyer<ApplicationInfo> {
    override fun key(data: ApplicationInfo, options: Options): String =
        AppIconLoader.getIconKey(data, data.longVersionCodeCompat, options.context)
}

class AppIconApplicationInfoFetcherFactory(
    context: Context
) : AppIconFetcher.Factory<ApplicationInfo>(
    // This is used by PrincipalListAdapter.
    context.getDimensionPixelSize(R.dimen.icon_size), context
) {
    override fun getApplicationInfo(data: ApplicationInfo): Pair<ApplicationInfo, Closeable?> =
        data to null
}
