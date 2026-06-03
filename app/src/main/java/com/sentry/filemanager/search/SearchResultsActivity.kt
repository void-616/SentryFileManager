package com.sentry.filemanager.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sentry.filemanager.app.AppActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class SearchResultsActivity : AppActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var tvStatus: TextView
    private lateinit var adapter: ResultsAdapter
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val EXTRA_FILTER = "search_filter"
        private const val EXTRA_START_PATH = "start_path"

        fun createIntent(context: Context, filter: SearchFilter, startPath: String): Intent =
            Intent(context, SearchResultsActivity::class.java).apply {
                putExtra(EXTRA_FILTER, filter)
                putExtra(EXTRA_START_PATH, startPath)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Search Results" }

        recycler = findViewById(R.id.results_recycler)
        emptyView = findViewById(R.id.results_empty)
        tvStatus = findViewById(R.id.results_status)

        adapter = ResultsAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val filter = intent.getParcelableExtra<SearchFilter>(EXTRA_FILTER) ?: return
        val startPath = intent.getStringExtra(EXTRA_START_PATH) ?: return

        runSearch(filter, startPath)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun runSearch(filter: SearchFilter, startPath: String) {
        tvStatus.text = "Searching..."
        tvStatus.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recycler.visibility = View.GONE

        executor.execute {
            val results = mutableListOf<File>()
            val rootDir = File(startPath)

            try {
                when (filter.scope) {
                    SearchScope.CURRENT_ONLY -> {
                        rootDir.listFiles()?.forEach { file ->
                            if (matchesFilter(filter, file)) results.add(file)
                        }
                    }
                    SearchScope.CURRENT_AND_SUBDIRS -> {
                        rootDir.walkTopDown().forEach { file ->
                            if (matchesFilter(filter, file)) results.add(file)
                        }
                    }
                    SearchScope.ENTIRE_STORAGE -> {
                        File("/sdcard").walkTopDown().forEach { file ->
                            if (matchesFilter(filter, file)) results.add(file)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread { showResults(results, filter) }
        }
    }

    private fun matchesFilter(filter: SearchFilter, file: File): Boolean {
        if (!filter.matchesName(file.name)) return false
        if (!filter.matchesSize(file.length())) return false
        if (!filter.matchesDate(file.lastModified())) return false

        val typeMatch = when (filter.fileTypeFilter) {
            FileTypeFilter.ALL -> true
            FileTypeFilter.FILES_ONLY -> file.isFile
            FileTypeFilter.FOLDERS_ONLY -> file.isDirectory
            FileTypeFilter.IMAGES -> file.isFile && file.extension.lowercase() in listOf("jpg","jpeg","png","gif","webp","bmp","heic")
            FileTypeFilter.VIDEO -> file.isFile && file.extension.lowercase() in listOf("mp4","mkv","avi","mov","webm","flv","3gp")
            FileTypeFilter.AUDIO -> file.isFile && file.extension.lowercase() in listOf("mp3","flac","aac","ogg","wav","m4a","opus")
            FileTypeFilter.DOCUMENTS -> file.isFile && file.extension.lowercase() in listOf("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","md")
            FileTypeFilter.ARCHIVES -> file.isFile && file.extension.lowercase() in listOf("zip","rar","7z","tar","gz","bz2","xz")
        }
        if (!typeMatch) return false

        if (filter.searchContent && filter.contentQuery.isNotEmpty() && file.isFile) {
            try {
                if (!file.readText().contains(filter.contentQuery, ignoreCase = true)) return false
            } catch (e: Exception) { return false }
        }

        return true
    }

    private fun showResults(results: List<File>, filter: SearchFilter) {
        adapter.submitList(results)
        tvStatus.text = "${results.size} result(s) for \"${filter.query}\""
        recycler.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    inner class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.VH>() {
        private var items = listOf<File>()
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        fun submitList(list: List<File>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_search_result, parent, false)
            return VH(v)
        }

        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.result_name)
            val tvPath: TextView = v.findViewById(R.id.result_path)
            val tvMeta: TextView = v.findViewById(R.id.result_meta)

            fun bind(file: File) {
                tvName.text = file.name
                tvPath.text = file.parent
                val size = if (file.isFile) formatSize(file.length()) else "folder"
                val date = dateFormat.format(Date(file.lastModified()))
                tvMeta.text = "$size  •  $date"
            }
        }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
    }
}
