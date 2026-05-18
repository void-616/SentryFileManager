package com.sentry.filemanager.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.sentry.filemanager.R
import com.sentry.filemanager.crash.CrashLogActivity
import com.sentry.filemanager.crash.CrashLogger
import com.sentry.filemanager.theme.custom.CustomThemeHelper
import com.sentry.filemanager.theme.custom.ThemeColor
import com.sentry.filemanager.theme.night.NightMode
import com.sentry.filemanager.theme.night.NightModeHelper
import com.sentry.filemanager.ui.PreferenceFragmentCompat
import com.sentry.filemanager.automation.AutomationEngine

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var localePreference: LocalePreference

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        localePreference = preferenceScreen.findPreference(getString(R.string.pref_key_locale))!!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            localePreference.setApplicationLocalesPre33 = { locales ->
                val activity = requireActivity() as SettingsActivity
                activity.setApplicationLocalesPre33(locales)
            }
        }

        // ── SentryFM preference handlers ─────────────────────────────────────

        preferenceScreen.findPreference<androidx.preference.Preference>("sentry_cache_cleaner")
            ?.setOnPreferenceClickListener {
                startActivity(android.content.Intent(requireContext(), com.sentry.filemanager.cleaner.CacheCleanerActivity::class.java))
                true
            }

        preferenceScreen.findPreference<androidx.preference.Preference>("sentry_crash_logs")
            ?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), CrashLogActivity::class.java))
                true
            }

        preferenceScreen.findPreference<androidx.preference.ListPreference>("sentry_crash_log_retention")
            ?.setOnPreferenceChangeListener { _, newValue ->
                val days = (newValue as? String)?.toIntOrNull() ?: 7
                CrashLogger.retentionDays = days
                true
            }

        preferenceScreen.findPreference<androidx.preference.SwitchPreferenceCompat>("sentry_automation_tasker_bridge")
            ?.setOnPreferenceChangeListener { _, newValue ->
                AutomationEngine.setTaskerBridgeEnabled(requireContext(), newValue as Boolean)
                true
            }

        preferenceScreen.findPreference<androidx.preference.Preference>("sentry_automation_rules")
            ?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), com.sentry.filemanager.automation.AutomationRulesActivity::class.java))
                true
            }

        preferenceScreen.findPreference<androidx.preference.Preference>("sentry_plugins")
            ?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), com.sentry.filemanager.plugin.PluginsActivity::class.java))
                true
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Settings.THEME_COLOR.observe(viewLifecycleOwner, this::onThemeColorChanged)
        Settings.MATERIAL_DESIGN_3.observe(viewLifecycleOwner, this::onMaterialDesign3Changed)
        Settings.NIGHT_MODE.observe(viewLifecycleOwner, this::onNightModeChanged)
        Settings.BLACK_NIGHT_MODE.observe(viewLifecycleOwner, this::onBlackNightModeChanged)
    }

    private fun onThemeColorChanged(themeColor: ThemeColor) { CustomThemeHelper.sync() }
    private fun onMaterialDesign3Changed(isMaterialDesign3: Boolean) { CustomThemeHelper.sync() }
    private fun onNightModeChanged(nightMode: NightMode) { NightModeHelper.sync() }
    private fun onBlackNightModeChanged(blackNightMode: Boolean) { CustomThemeHelper.sync() }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localePreference.notifyChanged()
        }
    }
}
