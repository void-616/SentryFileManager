package com.sentry.filemanager.automation

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

class AutomationRulesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: RulesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automation_rules)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Automation Rules" }

        recycler = findViewById(R.id.rules_recycler)
        emptyView = findViewById(R.id.rules_empty)

        adapter = RulesAdapter(
            onToggle = { rule -> AutomationRuleStore.setRuleEnabled(this, rule.id, !rule.enabled); loadRules() },
            onDelete = { rule -> confirmDelete(rule) },
            onRun = { rule -> startActivity(RuleHistoryActivity.createIntent(this, rule.id, rule.name)) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<View>(R.id.btn_add_rule).setOnClickListener {
            startActivity(EditRuleActivity.createIntent(this))
        }

        loadRules()
    }

    override fun onResume() { super.onResume(); loadRules() }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun loadRules() {
        val rules = AutomationRuleStore.getRules(this)
        adapter.submitList(rules)
        recycler.visibility = if (rules.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDelete(rule: AutomationRule) {
        AlertDialog.Builder(this)
            .setTitle("Delete rule")
            .setMessage("Delete \"${rule.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                AutomationRuleStore.deleteRule(this, rule.id)
                loadRules()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun runManual(rule: AutomationRule) {
        Toast.makeText(this, "Select a file to run rule on — coming soon", Toast.LENGTH_SHORT).show()
    }

    inner class RulesAdapter(
        private val onToggle: (AutomationRule) -> Unit,
        private val onDelete: (AutomationRule) -> Unit,
        private val onRun: (AutomationRule) -> Unit
    ) : RecyclerView.Adapter<RulesAdapter.VH>() {

        private var rules = listOf<AutomationRule>()

        fun submitList(list: List<AutomationRule>) { rules = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.item_automation_rule, parent, false)
            return VH(v)
        }

        override fun getItemCount() = rules.size
        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(rules[position])

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.rule_name)
            val summary: TextView = v.findViewById(R.id.rule_summary)
            val status: TextView = v.findViewById(R.id.rule_status)
            val btnToggle: View = v.findViewById(R.id.btn_rule_toggle)
            val btnDelete: View = v.findViewById(R.id.btn_rule_delete)
            val btnRun: View = v.findViewById(R.id.btn_rule_run)

            fun bind(rule: AutomationRule) {
                name.text = rule.name.ifEmpty { "Unnamed rule" }
                summary.text = "${rule.trigger.name.replace('_', ' ')} → ${rule.actions.size} action(s)"
                status.text = if (rule.enabled) "ON" else "OFF"
                status.setTextColor(if (rule.enabled) 0xFF16A34A.toInt() else 0xFF6B7280.toInt())
                btnToggle.setOnClickListener { onToggle(rule) }
                btnDelete.setOnClickListener { onDelete(rule) }
                btnRun.setOnClickListener { onRun(rule) }
            }
        }
    }
}
