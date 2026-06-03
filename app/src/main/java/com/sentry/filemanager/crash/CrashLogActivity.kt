/*
 * Copyright (c) 2026 Sentry Project
 * SentryOS Project
 */

package com.sentry.filemanager.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sentry.filemanager.app.AppActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R

class CrashLogActivity : AppActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: CrashLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_log)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Crash Logs"
        }

        recyclerView = findViewById(R.id.crash_log_recycler)
        emptyView = findViewById(R.id.crash_log_empty)

        adapter = CrashLogAdapter(
            onCopy = { copyToClipboard(it) },
            onShare = { shareLog(it) },
            onDelete = { deleteLog(it) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<View>(R.id.btn_clear_all).setOnClickListener { confirmClearAll() }
        findViewById<View>(R.id.btn_export_all).setOnClickListener { exportAll() }

        loadLogs()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadLogs() {
        val logs = CrashLogger.getLogs()
        adapter.submitList(logs)
        recyclerView.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun copyToClipboard(entry: CrashLogEntry) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", entry.fullText))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareLog(entry: CrashLogEntry) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, entry.fullText)
            putExtra(Intent.EXTRA_SUBJECT, "SentryFM Crash Report — ${entry.timestamp}")
        }, "Share crash log"))
    }

    private fun deleteLog(entry: CrashLogEntry) {
        CrashLogger.deleteLog(entry.fileName)
        loadLogs()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle("Clear all logs")
            .setMessage("Delete all ${CrashLogger.getLogCount()} crash logs?")
            .setPositiveButton("Delete all") { _, _ ->
                CrashLogger.deleteAllLogs()
                loadLogs()
                Toast.makeText(this, "All logs cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportAll() {
        val logs = CrashLogger.exportAllLogs()
        if (logs.isBlank()) { Toast.makeText(this, "No logs to export", Toast.LENGTH_SHORT).show(); return }
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, logs)
            putExtra(Intent.EXTRA_SUBJECT, "SentryFM Crash Logs Export")
        }, "Export all logs"))
    }
}
