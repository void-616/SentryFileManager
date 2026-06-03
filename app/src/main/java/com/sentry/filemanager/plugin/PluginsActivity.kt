package com.sentry.filemanager.plugin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sentry.filemanager.app.AppActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R
import java.io.File
import java.io.FileOutputStream

class PluginsActivity : AppActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: PluginsAdapter

    private val pickSfp = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        installFromUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugins)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Plugins" }

        recycler = findViewById(R.id.plugins_recycler)
        emptyView = findViewById(R.id.plugins_empty)

        adapter = PluginsAdapter(
            onToggle = { plugin ->
                PluginManager.setEnabled(this, plugin.id, !plugin.enabled)
                loadPlugins()
            },
            onUninstall = { plugin -> confirmUninstall(plugin) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<View>(R.id.btn_install_plugin).setOnClickListener {
            pickSfp.launch("*/*")
        }

        loadPlugins()
    }

    override fun onResume() { super.onResume(); loadPlugins() }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
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
            .setTitle("Permission required")
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
            .setMessage("Uninstall \"${plugin.name}\"?")
            .setPositiveButton("Uninstall") { _, _ ->
                PluginManager.uninstall(this, plugin.id)
                loadPlugins()
                Toast.makeText(this, "Uninstalled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null).show()
    }

    inner class PluginsAdapter(
        private val onToggle: (InstalledPlugin) -> Unit,
        private val onUninstall: (InstalledPlugin) -> Unit
    ) : RecyclerView.Adapter<PluginsAdapter.VH>() {

        private var plugins = listOf<InstalledPlugin>()

        fun submitList(list: List<InstalledPlugin>) { plugins = list; notifyDataSetChanged() }

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
            val status: TextView = v.findViewById(R.id.plugin_status)
            val btnToggle: View = v.findViewById(R.id.btn_plugin_toggle)
            val btnUninstall: View = v.findViewById(R.id.btn_plugin_uninstall)

            fun bind(plugin: InstalledPlugin) {
                name.text = plugin.name
                version.text = "v${plugin.version}"
                description.text = plugin.manifest.description.ifEmpty { "No description" }
                status.text = if (plugin.enabled) "Enabled" else "Disabled"
                status.setTextColor(if (plugin.enabled) 0xFF16A34A.toInt() else 0xFF6B7280.toInt())
                btnToggle.setOnClickListener { onToggle(plugin) }
                btnUninstall.setOnClickListener { onUninstall(plugin) }
            }
        }
    }
}
