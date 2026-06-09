package com.sentry.filemanager.plugin

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R
import com.sentry.filemanager.app.AppActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class PluginsActivity : AppActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: PluginsAdapter

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        installFromUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugins)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Plugins"
        }

        recycler = findViewById(R.id.plugins_recycler)
        emptyView = findViewById(R.id.plugins_empty)

        adapter = PluginsAdapter(
            onToggle = { plugin ->
                PluginManager.setEnabled(this, plugin.id, !plugin.enabled)
                loadPlugins()
            },
            onUninstall = { plugin -> confirmUninstall(plugin) },
            onCheckUpdate = { plugin -> checkUpdate(plugin) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<View>(R.id.btn_install_plugin).setOnClickListener {
            pickFile.launch("*/*")
        }

        loadPlugins()
    }

    override fun onResume() {
        super.onResume()
        loadPlugins()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPlugins() {
        val plugins = PluginManager.getInstalledPlugins(this)
        adapter.submitList(plugins)
        recycler.visibility = if (plugins.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (plugins.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun installFromUri(uri: Uri) {
        try {
            val tempFile = File(cacheDir, "install_temp.sfp")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            }
            when (val result = PluginManager.installFromFile(this, tempFile)) {
                is PluginManager.InstallResult.Success -> {
                    Toast.makeText(this, "Installed: ${result.plugin.name}", Toast.LENGTH_SHORT).show()
                    loadPlugins()
                }
                is PluginManager.InstallResult.Error -> {
                    Toast.makeText(this, "Install failed: ${result.reason}", Toast.LENGTH_LONG).show()
                }
                is PluginManager.InstallResult.PermissionRequired -> {
                    showPermissionDialog(tempFile, result.manifest, result.dangerous)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPermissionDialog(
        sfpFile: File,
        manifest: PluginManifest,
        dangerous: List<PluginPermission>
    ) {
        val permList = dangerous.joinToString("\n") { "• ${it.label}: ${it.description}" }
        AlertDialog.Builder(this)
            .setTitle("Permissions required")
            .setMessage("\"${manifest.name}\" requests:\n\n$permList\n\nGrant these permissions?")
            .setPositiveButton("Grant & Install") { _, _ ->
                when (val result = PluginManager.installWithPermissions(this, sfpFile, manifest.permissions)) {
                    is PluginManager.InstallResult.Success -> {
                        Toast.makeText(this, "Installed: ${result.plugin.name}", Toast.LENGTH_SHORT).show()
                        loadPlugins()
                    }
                    is PluginManager.InstallResult.Error ->
                        Toast.makeText(this, "Install failed: ${result.reason}", Toast.LENGTH_LONG).show()
                    else -> {}
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmUninstall(plugin: InstalledPlugin) {
        AlertDialog.Builder(this)
            .setTitle("Uninstall plugin")
            .setMessage("Uninstall \"${plugin.name}\"?\n\nThis will delete all plugin files.")
            .setPositiveButton("Uninstall") { _, _ ->
                PluginManager.uninstall(this, plugin.id)
                loadPlugins()
                Toast.makeText(this, "${plugin.name} uninstalled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkUpdate(plugin: InstalledPlugin) {
        val url = PluginManager.getUpdateCheckUrl(plugin)
        if (url == null) {
            Toast.makeText(this, "No update source configured for this plugin", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Checking for updates…", Toast.LENGTH_SHORT).show()
        thread {
            try {
                val response = java.net.URL(url).readText()
                val json = org.json.JSONObject(response)
                val latestTag = json.optString("tag_name", "").trimStart('v')
                val currentVersion = plugin.version.trimStart('v')
                runOnUiThread {
                    if (latestTag.isEmpty()) {
                        Toast.makeText(this, "Could not read version info", Toast.LENGTH_SHORT).show()
                    } else if (latestTag == currentVersion) {
                        Toast.makeText(this, "${plugin.name} is up to date (v$currentVersion)", Toast.LENGTH_SHORT).show()
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("Update available")
                            .setMessage("${plugin.name}\n\nInstalled: v$currentVersion\nLatest: v$latestTag\n\nVisit the plugin source to download the update.")
                            .setPositiveButton("Open source") { _, _ ->
                                if (plugin.manifest.sourceUrl.isNotEmpty()) {
                                    startActivity(android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        Uri.parse(plugin.manifest.sourceUrl)
                                    ))
                                }
                            }
                            .setNegativeButton("Dismiss", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Update check failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    inner class PluginsAdapter(
        private val onToggle: (InstalledPlugin) -> Unit,
        private val onUninstall: (InstalledPlugin) -> Unit,
        private val onCheckUpdate: (InstalledPlugin) -> Unit
    ) : RecyclerView.Adapter<PluginsAdapter.VH>() {

        private var plugins = listOf<InstalledPlugin>()

        fun submitList(list: List<InstalledPlugin>) {
            plugins = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_plugin, parent, false)
            return VH(v)
        }

        override fun getItemCount() = plugins.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(plugins[position])

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.plugin_name)
            val version: TextView = v.findViewById(R.id.plugin_version)
            val description: TextView = v.findViewById(R.id.plugin_description)
            val author: TextView = v.findViewById(R.id.plugin_author)
            val installedDate: TextView = v.findViewById(R.id.plugin_installed_date)
            val status: TextView = v.findViewById(R.id.plugin_status)
            val btnToggle: TextView = v.findViewById(R.id.btn_plugin_toggle)
            val btnUninstall: TextView = v.findViewById(R.id.btn_plugin_uninstall)
            val btnUpdate: TextView = v.findViewById(R.id.btn_plugin_update)

            fun bind(plugin: InstalledPlugin) {
                name.text = plugin.name
                version.text = "v${plugin.version}"
                description.text = plugin.manifest.description.ifEmpty { "No description" }

                // Author
                author.text = if (plugin.manifest.author.isNotEmpty())
                    "by ${plugin.manifest.author}" else ""
                author.visibility = if (plugin.manifest.author.isNotEmpty()) View.VISIBLE else View.GONE

                // Installed date
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    .format(Date(plugin.installedAt))
                installedDate.text = "Installed $dateStr"

                // Status badge
                if (plugin.enabled) {
                    status.text = "Enabled"
                    status.setTextColor(0xFF16A34A.toInt())
                } else {
                    status.text = "Disabled"
                    status.setTextColor(0xFF6B7280.toInt())
                }

                // Toggle button — contextual label
                btnToggle.text = if (plugin.enabled) "Disable" else "Enable"
                btnToggle.setTextColor(
                    if (plugin.enabled) 0xFFDC2626.toInt()
                    else 0xFF16A34A.toInt()
                )
                btnToggle.setOnClickListener { onToggle(plugin) }
                btnUninstall.setOnClickListener { onUninstall(plugin) }
                btnUpdate.setOnClickListener { onCheckUpdate(plugin) }

                // Hide update button if no source URL
                btnUpdate.visibility = if (plugin.manifest.sourceUrl.isNotEmpty())
                    View.VISIBLE else View.GONE
            }
        }
    }
}
