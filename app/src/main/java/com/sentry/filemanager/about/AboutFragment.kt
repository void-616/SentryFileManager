/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * Copyright (c) 2026 Sentry Project
 * All Rights Reserved.
 */

package com.sentry.filemanager.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sentry.filemanager.BuildConfig
import com.sentry.filemanager.R
import com.sentry.filemanager.databinding.AboutFragmentBinding
import com.sentry.filemanager.ui.LicensesDialogFragment
import com.sentry.filemanager.util.createViewIntent
import com.sentry.filemanager.util.startActivitySafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        AboutFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Version info — auto from BuildConfig
        binding.versionSummary.text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

        // Source code
        binding.gitHubLayout.setOnClickListener {
            startActivitySafe(GITHUB_URI.createViewIntent())
        }

        // Release notes
        binding.releaseNotesLayout.setOnClickListener {
            startActivitySafe(RELEASE_NOTES_URI.createViewIntent())
        }

        // Check for updates
        binding.checkUpdatesLayout.setOnClickListener {
            checkForUpdates()
        }

        // Change log
        binding.changelogLayout.setOnClickListener {
            startActivitySafe(CHANGELOG_URI.createViewIntent())
        }

        // Contact via Telegram
        binding.telegramLayout.setOnClickListener {
            startActivitySafe(TELEGRAM_URI.createViewIntent())
        }

        // Licenses
        binding.licensesLayout.setOnClickListener {
            LicensesDialogFragment.show(this)
        }

        // About Sentry File Manager — opens promo screen
        binding.aboutSentryLayout.setOnClickListener {
            startActivity(SentryAboutActivity.createIntent(requireContext()))
        }
    }

    private fun checkForUpdates() {
        val checkingToast = Toast.makeText(requireContext(), "Checking for updates...", Toast.LENGTH_SHORT)
        checkingToast.show()

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val json = URL(GITHUB_API_RELEASES).readText()
                    val latest = JSONObject(json)
                    val latestTag = latest.getString("tag_name").trimStart('v')
                    val currentVersion = BuildConfig.VERSION_NAME
                    Pair(latestTag, currentVersion)
                }

                val (latestVersion, currentVersion) = result
                if (latestVersion != currentVersion) {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Update Available")
                        .setMessage("New version v$latestVersion is available.\nYou are on v$currentVersion.")
                        .setPositiveButton("Download") { _, _ ->
                            startActivitySafe(RELEASE_NOTES_URI.createViewIntent())
                        }
                        .setNegativeButton("Later", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "You're on the latest version (v$currentVersion)", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Could not check for updates. Check your connection.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private val GITHUB_URI = Uri.parse("https://github.com/void-616/SentryFileManager")
        private val RELEASE_NOTES_URI = Uri.parse("https://github.com/void-616/SentryFileManager/releases")
        private val CHANGELOG_URI = Uri.parse("https://github.com/void-616/SentryFileManager/commits/master")
        private val TELEGRAM_URI = Uri.parse("https://t.me/SentryFileManager")
        private const val GITHUB_API_RELEASES = "https://api.github.com/repos/void-616/SentryFileManager/releases/latest"
    }
}
