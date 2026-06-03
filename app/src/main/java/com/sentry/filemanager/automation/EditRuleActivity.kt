package com.sentry.filemanager.automation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sentry.filemanager.app.AppActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sentry.filemanager.R

class EditRuleActivity : AppActivity() {

    private lateinit var etName: EditText
    private lateinit var spinnerTrigger: Spinner
    private lateinit var etWatchPath: EditText
    private lateinit var rvConditions: RecyclerView
    private lateinit var rvActions: RecyclerView
    private lateinit var btnAddCondition: View
    private lateinit var btnAddAction: View
    private lateinit var btnSave: View

    private val conditions = mutableListOf<RuleCondition>()
    private val actions = mutableListOf<RuleAction>()
    private lateinit var conditionsAdapter: ConditionsAdapter
    private lateinit var actionsAdapter: ActionsAdapter

    private var existingRule: AutomationRule? = null

    companion object {
        private const val EXTRA_RULE = "rule"

        fun createIntent(context: Context, rule: AutomationRule? = null): Intent =
            Intent(context, EditRuleActivity::class.java).apply {
                if (rule != null) putExtra(EXTRA_RULE, rule)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_rule)

        existingRule = intent.getParcelableExtra(EXTRA_RULE)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (existingRule != null) "Edit Rule" else "New Rule"
        }

        etName = findViewById(R.id.et_rule_name)
        spinnerTrigger = findViewById(R.id.spinner_trigger)
        etWatchPath = findViewById(R.id.et_watch_path)
        rvConditions = findViewById(R.id.rv_conditions)
        rvActions = findViewById(R.id.rv_actions)
        btnAddCondition = findViewById(R.id.btn_add_condition)
        btnAddAction = findViewById(R.id.btn_add_action)
        btnSave = findViewById(R.id.btn_save_rule)

        setupTriggerSpinner()
        setupConditionsRecycler()
        setupActionsRecycler()

        existingRule?.let { rule ->
            etName.setText(rule.name)
            etWatchPath.setText(rule.watchPath)
            spinnerTrigger.setSelection(RuleTrigger.values().indexOf(rule.trigger))
            conditions.addAll(rule.conditions)
            actions.addAll(rule.actions)
            conditionsAdapter.notifyDataSetChanged()
            actionsAdapter.notifyDataSetChanged()
        }

        btnAddCondition.setOnClickListener { showAddConditionDialog() }
        btnAddAction.setOnClickListener { showAddActionDialog() }
        btnSave.setOnClickListener { saveRule() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    private fun setupTriggerSpinner() {
        val labels = listOf("File added", "File modified", "File deleted",
            "Daily schedule", "Weekly schedule", "Manual")
        spinnerTrigger.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, labels)
    }

    private fun setupConditionsRecycler() {
        conditionsAdapter = ConditionsAdapter(conditions) { index ->
            conditions.removeAt(index)
            conditionsAdapter.notifyDataSetChanged()
        }
        rvConditions.layoutManager = LinearLayoutManager(this)
        rvConditions.adapter = conditionsAdapter
    }

    private fun setupActionsRecycler() {
        actionsAdapter = ActionsAdapter(actions) { index ->
            actions.removeAt(index)
            actionsAdapter.notifyDataSetChanged()
        }
        rvActions.layoutManager = LinearLayoutManager(this)
        rvActions.adapter = actionsAdapter
    }

    private fun showAddConditionDialog() {
        val types = ConditionType.values()
        val labels = listOf("Name contains", "Name matches glob", "Name matches regex",
            "Extension is", "Size greater than (bytes)", "Size less than (bytes)",
            "Age older than (days)")

        var selectedType = types[0]
        val typeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@EditRuleActivity,
                android.R.layout.simple_spinner_dropdown_item, labels)
            setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedType = types[pos]
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            })
        }
        val valueInput = EditText(this).apply { hint = "Value" }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(typeSpinner)
            addView(valueInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add condition")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val value = valueInput.text.toString().trim()
                if (value.isNotEmpty()) {
                    conditions.add(RuleCondition(selectedType, value))
                    conditionsAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddActionDialog() {
        val types = ActionType.values().filter { it != ActionType.SHELL_COMMAND }
        val labels = listOf("Move to folder", "Copy to folder", "Delete",
            "Rename (pattern)", "Compress to ZIP", "Extract")

        var selectedType = types[0]
        val typeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@EditRuleActivity,
                android.R.layout.simple_spinner_dropdown_item, labels)
            setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    selectedType = types[pos]
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            })
        }
        val targetInput = EditText(this).apply {
            hint = "Target path (if needed)"
        }
        val paramInput = EditText(this).apply {
            hint = "Parameter (rename pattern, etc.)"
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(typeSpinner)
            addView(targetInput)
            addView(paramInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Add action")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                actions.add(RuleAction(
                    type = selectedType,
                    targetPath = targetInput.text.toString().trim(),
                    parameter = paramInput.text.toString().trim()
                ))
                actionsAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveRule() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            etName.error = "Name required"
            return
        }
        if (actions.isEmpty()) {
            Toast.makeText(this, "Add at least one action", Toast.LENGTH_SHORT).show()
            return
        }

        val trigger = RuleTrigger.values()[spinnerTrigger.selectedItemPosition]
        val watchPath = etWatchPath.text.toString().trim()

        if (trigger in listOf(RuleTrigger.FILE_ADDED, RuleTrigger.FILE_MODIFIED,
                RuleTrigger.FILE_DELETED) && watchPath.isEmpty()) {
            etWatchPath.error = "Watch path required for file triggers"
            return
        }

        val rule = (existingRule ?: AutomationRule()).copy(
            name = name,
            trigger = trigger,
            watchPath = watchPath,
            conditions = conditions.toList(),
            actions = actions.toList()
        )

        AutomationRuleStore.saveRule(this, rule)

        // Refresh file watcher service
        startService(android.content.Intent(this, FileWatcherService::class.java).apply {
            action = FileWatcherService.ACTION_REFRESH
        })

        Toast.makeText(this, "Rule saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ── Adapters ──────────────────────────────────────────────────────────────

    class ConditionsAdapter(
        private val items: List<RuleCondition>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<ConditionsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            return VH(v)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val cond = items[position]
            holder.bind(cond, position)
        }

        inner class VH(private val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            fun bind(cond: RuleCondition, position: Int) {
                layout.removeAllViews()
                val label = cond.type.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }
                layout.addView(TextView(layout.context).apply {
                    text = "$label: ${cond.value}"
                    layoutParams = LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    textSize = 13f
                })
                layout.addView(TextView(layout.context).apply {
                    text = "✕"
                    setTextColor(0xFFEF4444.toInt())
                    setPadding(16, 0, 0, 0)
                    setOnClickListener { onRemove(position) }
                })
            }
        }
    }

    class ActionsAdapter(
        private val items: List<RuleAction>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<ActionsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            return VH(v)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], position)

        inner class VH(private val layout: LinearLayout) : RecyclerView.ViewHolder(layout) {
            fun bind(action: RuleAction, position: Int) {
                layout.removeAllViews()
                val label = action.type.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() }
                val detail = listOf(action.targetPath, action.parameter)
                    .filter { it.isNotEmpty() }.joinToString(", ")
                layout.addView(TextView(layout.context).apply {
                    text = if (detail.isEmpty()) label else "$label → $detail"
                    layoutParams = LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    textSize = 13f
                })
                layout.addView(TextView(layout.context).apply {
                    text = "✕"
                    setTextColor(0xFFEF4444.toInt())
                    setPadding(16, 0, 0, 0)
                    setOnClickListener { onRemove(position) }
                })
            }
        }
    }
}
