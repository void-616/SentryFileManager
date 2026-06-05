/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.sentry.filemanager.R
import com.sentry.filemanager.app.AppActivity

class SentryAboutActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sentry_about)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = ""
        }

        val features = listOf(
            Triple("🛡️", "Secure & Private", "Your files stay on your device. No tracking. No ads."),
            Triple("⚡", "Fast & Efficient", "Optimized performance for every operation."),
            Triple("🧩", "Extensible", "Powerful plugin architecture for endless possibilities."),
            Triple(">_", "Advanced", "Tabs, automation, terminal and more for power users."),
            Triple("<>", "Open Source", "Built with transparency and community in mind.")
        )

        val featureViews = listOf(
            R.id.feature1, R.id.feature2, R.id.feature3, R.id.feature4, R.id.feature5
        )

        featureViews.zip(features).forEach { (viewId, feature) ->
            val view = findViewById<android.view.View>(viewId)
            view.findViewById<TextView>(R.id.feature_icon).text = feature.first
            view.findViewById<TextView>(R.id.feature_title).text = feature.second
            view.findViewById<TextView>(R.id.feature_desc).text = feature.third
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, SentryAboutActivity::class.java)
    }
}
