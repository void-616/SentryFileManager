/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.dualpane

import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager

object DualPaneManager {

    private const val PREF_DUAL_PANE_ENABLED = "sentry_dual_pane_enabled"
    private const val PREF_DUAL_PANE_PORTRAIT = "sentry_dual_pane_portrait"
    private const val PREF_SPLIT_RATIO = "sentry_dual_pane_ratio"
    private const val MIN_WIDTH_DP = 600

    fun isDualPaneEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val isLandscape = context.resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        val portraitEnabled = prefs.getBoolean(PREF_DUAL_PANE_PORTRAIT, false)
        val manualEnabled = prefs.getBoolean(PREF_DUAL_PANE_ENABLED, true)

        if (!manualEnabled) return false

        val screenWidthDp = context.resources.configuration.screenWidthDp
        val wideEnough = screenWidthDp >= MIN_WIDTH_DP

        return wideEnough && (isLandscape || portraitEnabled)
    }

    fun setDualPaneEnabled(context: Context, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(PREF_DUAL_PANE_ENABLED, enabled).apply()
    }

    fun setPortraitEnabled(context: Context, enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(PREF_DUAL_PANE_PORTRAIT, enabled).apply()
    }

    fun saveSplitRatio(context: Context, ratio: Float) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putFloat(PREF_SPLIT_RATIO, ratio).apply()
    }

    fun getSplitRatio(context: Context): Float {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getFloat(PREF_SPLIT_RATIO, 0.5f)
    }
}
