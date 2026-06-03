/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */
package com.sentry.filemanager.terminal

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sentry.filemanager.R
import com.sentry.filemanager.tabs.TabsActivity

object Terminal {
    fun open(path: String, context: Context) {
        val activity = context as? AppCompatActivity ?: return
        val fragment = TerminalFragment.newInstance(path)
        val containerId = if (activity is TabsActivity) R.id.tab_fragment_container else android.R.id.content
        activity.supportFragmentManager.commit {
            setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            replace(containerId, fragment, "terminal")
            addToBackStack("terminal")
        }
    }
}
