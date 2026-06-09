/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.sentry.filemanager.util

import android.net.wifi.WifiManager
import android.os.PowerManager
import com.sentry.filemanager.app.powerManager
import com.sentry.filemanager.app.wifiManager

class WakeWifiLock(tag: String) {
    private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
        .apply { setReferenceCounted(false) }
    private val wifiLock =
        wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, tag)
            .apply { setReferenceCounted(false) }

    var isAcquired: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                wakeLock.acquire()
                wifiLock.acquire()
            } else {
                wifiLock.release()
                wakeLock.release()
            }
            field = value
        }
}
