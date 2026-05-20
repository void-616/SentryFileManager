package com.sentry.filemanager.automation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R
import java.text.SimpleDateFormat
import java.util.*

class RuleHistoryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var tvRuleName: TextView
    private lateinit var adapter: HistoryAdapter
    private var ruleId: String = ""
    private var ruleName: String = ""

    companion object {
        private const val EXTRA_RULE_ID = "rule_id"
        private const val EXTRA_RULE_NAME = "rule_name"

        fun createIntent(context: Context, ruleId: String, ruleName: String): Intent =
            Intent(context, RuleHistoryActivity::class.java).apply {
                putExtra(EXTRA_RULE_ID, ruleId)
                putExtra(EXTRA_RULE_NAME, ruleName)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_history)

        ruleId = intent.getStringExtra(EXTRA_RULE_ID) ?: ""
        ruleName = intent.getStringExtra(EXTRA_RULE_NAME) ?: "Rule"

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Run History"
        }

        recycler = findViewById(R.id.history_recycler)
        emptyView = findViewById(R.id.history_empty)
        tvRuleName = findViewById(R.id.tv_rule_name)
        tvRuleName.text = ruleName

        findViewById<View>(R.id.btn_clear_history).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear history")
                .setMessage("Delete all logs for \"$ruleName\"?")
                .setPositiveButton("Clear") { _, _ ->
                    AutomationRuleStore.clearLogs(this, ruleId)
                    loadLogs()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null).show()
        }

        adapter = HistoryAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        loadLogs()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadLogs() {
        val logs = AutomationRuleStore.getLogs(this, ruleId)
        adapter.submitList(logs)
        recycler.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
    }

    inner class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {
        private var logs = listOf<RuleRunLog>()
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)

        fun submitList(list: List<RuleRunLog>) { logs = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_rule_history, parent, false)
            return VH(v)
        }

        override fun getItemCount() = logs.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(logs[position])

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTime: TextView = v.findViewById(R.id.history_time)
            val tvFile: TextView = v.findViewById(R.id.history_file)
            val tvResult: TextView = v.findViewById(R.id.history_result)
            val tvMessage: TextView = v.findViewById(R.id.history_message)

            fun bind(log: RuleRunLog) {
                tvTime.text = dateFormat.format(Date(log.timestamp))
                tvFile.text = log.filePath
                tvResult.text = if (log.success) "✓ Success" else "✗ Failed"
                tvResult.setTextColor(if (log.success) 0xFF16A34A.toInt() else 0xFFDC2626.toInt())
                tvMessage.text = log.message
                tvMessage.visibility = if (log.message.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
}
