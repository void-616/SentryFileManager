package com.sentry.filemanager.cleaner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R
import com.sentry.filemanager.filelist.FileListActivity
import java.io.File

class CacheCleanerActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var tvSummary: TextView
    private lateinit var btnClean: View
    private lateinit var btnAddFolder: View
    private lateinit var adapter: FolderAdapter

    private val pickFolder = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra("selected_path") ?: return@registerForActivityResult
            CacheCleanerManager.addFolder(this, path)
            loadFolders()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_cleaner)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Cache Cleaner" }

        recycler = findViewById(R.id.cleaner_recycler)
        emptyView = findViewById(R.id.cleaner_empty)
        tvSummary = findViewById(R.id.cleaner_summary)
        btnClean = findViewById(R.id.btn_clean_now)
        btnAddFolder = findViewById(R.id.btn_add_folder)

        adapter = FolderAdapter { path ->
            AlertDialog.Builder(this)
                .setTitle("Remove folder")
                .setMessage("Stop watching \"$path\" for cleanup?")
                .setPositiveButton("Remove") { _, _ ->
                    CacheCleanerManager.removeFolder(this, path)
                    loadFolders()
                }
                .setNegativeButton("Cancel", null).show()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnAddFolder.setOnClickListener {
            val intent = FileListActivity.createViewIntent(
                java8.nio.file.Paths.get("/sdcard")
            ).putExtra("pick_folder_for_cleaner", true)
            Toast.makeText(this, "Navigate to folder and use ⋮ → Add to cleaner", Toast.LENGTH_LONG).show()
        }

        btnClean.setOnClickListener { confirmClean() }

        loadFolders()
    }

    override fun onResume() { super.onResume(); loadFolders() }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadFolders() {
        val folders = CacheCleanerManager.getWatchedFolders(this)
        adapter.submitList(folders)
        recycler.visibility = if (folders.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (folders.isEmpty()) View.VISIBLE else View.GONE

        if (folders.isNotEmpty()) {
            val preview = CacheCleanerManager.previewClean(this)
            val days = CacheCleanerManager.getRetentionDays(this)
            tvSummary.text = if (preview.filesDeleted > 0)
                "${preview.filesDeleted} file(s) older than $days days — ${CacheCleanerManager.formatSize(preview.bytesFreed)} to free"
            else
                "No files older than $days days found"
            tvSummary.visibility = View.VISIBLE
        } else {
            tvSummary.visibility = View.GONE
        }
    }

    private fun confirmClean() {
        val preview = CacheCleanerManager.previewClean(this)
        if (preview.filesDeleted == 0) {
            Toast.makeText(this, "Nothing to clean", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Clean now")
            .setMessage("Delete ${preview.filesDeleted} file(s) (${CacheCleanerManager.formatSize(preview.bytesFreed)})?\n\nThis cannot be undone.")
            .setPositiveButton("Clean") { _, _ ->
                val result = CacheCleanerManager.cleanAll(this)
                Toast.makeText(this, "Cleaned ${result.filesDeleted} file(s) — ${CacheCleanerManager.formatSize(result.bytesFreed)} freed", Toast.LENGTH_LONG).show()
                loadFolders()
            }
            .setNegativeButton("Cancel", null).show()
    }

    inner class FolderAdapter(
        private val onRemove: (String) -> Unit
    ) : RecyclerView.Adapter<FolderAdapter.VH>() {

        private var folders = listOf<String>()

        fun submitList(list: List<String>) { folders = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_cleaner_folder, parent, false)
            return VH(v)
        }

        override fun getItemCount() = folders.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(folders[position])

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val path: TextView = v.findViewById(R.id.folder_path)
            val btnRemove: View = v.findViewById(R.id.btn_remove_folder)

            fun bind(folderPath: String) {
                path.text = folderPath
                btnRemove.setOnClickListener { onRemove(folderPath) }
            }
        }
    }
}
