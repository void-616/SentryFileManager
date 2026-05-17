package com.sentry.filemanager.automation
import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

object AutomationRuleStore {
    private const val PREF_RULES = "sentry_automation_rules"
    private const val PREF_LOGS = "sentry_automation_logs"
    private const val MAX_LOGS = 200

    fun getRules(context: Context): List<AutomationRule> {
        val json = prefs(context).getString(PREF_RULES, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { ruleFromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) { emptyList() }
    }

    fun saveRule(context: Context, rule: AutomationRule) {
        val rules = getRules(context).toMutableList()
        val idx = rules.indexOfFirst { it.id == rule.id }
        if (idx >= 0) rules[idx] = rule else rules.add(rule)
        persistRules(context, rules)
    }

    fun deleteRule(context: Context, id: String) =
        persistRules(context, getRules(context).filter { it.id != id })

    fun setRuleEnabled(context: Context, id: String, enabled: Boolean) {
        val rules = getRules(context).toMutableList()
        val idx = rules.indexOfFirst { it.id == id }
        if (idx >= 0) rules[idx] = rules[idx].copy(enabled = enabled)
        persistRules(context, rules)
    }

    fun updateRunStats(context: Context, id: String) {
        val rules = getRules(context).toMutableList()
        val idx = rules.indexOfFirst { it.id == id }
        if (idx >= 0) rules[idx] = rules[idx].copy(
            lastRunAt = System.currentTimeMillis(),
            runCount = rules[idx].runCount + 1
        )
        persistRules(context, rules)
    }

    fun getLogs(context: Context, ruleId: String? = null): List<RuleRunLog> {
        val json = prefs(context).getString(PREF_LOGS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { logFromJson(arr.getJSONObject(it)) }
                .filter { ruleId == null || it.ruleId == ruleId }
        } catch (e: Exception) { emptyList() }
    }

    fun addLog(context: Context, log: RuleRunLog) {
        val logs = getLogs(context).toMutableList()
        logs.add(0, log)
        if (logs.size > MAX_LOGS) logs.removeAt(logs.lastIndex)
        prefs(context).edit()
            .putString(PREF_LOGS, JSONArray(logs.map { logToJson(it) }).toString()).apply()
    }

    fun clearLogs(context: Context, ruleId: String? = null) {
        val logs = if (ruleId != null) getLogs(context).filter { it.ruleId != ruleId } else emptyList()
        prefs(context).edit()
            .putString(PREF_LOGS, JSONArray(logs.map { logToJson(it) }).toString()).apply()
    }

    fun exportRulesJson(context: Context): String =
        JSONArray(getRules(context).map { ruleToJson(it) }).toString(2)

    fun importRulesJson(context: Context, json: String): Int {
        return try {
            val arr = JSONArray(json)
            val rules = (0 until arr.length()).map { ruleFromJson(arr.getJSONObject(it)) }
            val existing = getRules(context).toMutableList()
            rules.forEach { rule ->
                val idx = existing.indexOfFirst { it.id == rule.id }
                if (idx >= 0) existing[idx] = rule else existing.add(rule)
            }
            persistRules(context, existing)
            rules.size
        } catch (e: Exception) { -1 }
    }

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    private fun persistRules(context: Context, rules: List<AutomationRule>) =
        prefs(context).edit()
            .putString(PREF_RULES, JSONArray(rules.map { ruleToJson(it) }).toString()).apply()

    private fun ruleToJson(r: AutomationRule): JSONObject = JSONObject().apply {
        put("id", r.id); put("name", r.name); put("enabled", r.enabled)
        put("trigger", r.trigger.name); put("watchPath", r.watchPath)
        put("conditions", JSONArray(r.conditions.map { condToJson(it) }))
        put("actions", JSONArray(r.actions.map { actionToJson(it) }))
        put("createdAt", r.createdAt); put("lastRunAt", r.lastRunAt); put("runCount", r.runCount)
    }

    private fun ruleFromJson(o: JSONObject): AutomationRule = AutomationRule(
        id = o.optString("id"), name = o.optString("name"),
        enabled = o.optBoolean("enabled", true),
        trigger = RuleTrigger.valueOf(o.optString("trigger", RuleTrigger.FILE_ADDED.name)),
        watchPath = o.optString("watchPath"),
        conditions = (o.optJSONArray("conditions") ?: JSONArray()).let { arr ->
            (0 until arr.length()).map { condFromJson(arr.getJSONObject(it)) } },
        actions = (o.optJSONArray("actions") ?: JSONArray()).let { arr ->
            (0 until arr.length()).map { actionFromJson(arr.getJSONObject(it)) } },
        createdAt = o.optLong("createdAt"), lastRunAt = o.optLong("lastRunAt", -1L),
        runCount = o.optInt("runCount", 0)
    )

    private fun condToJson(c: RuleCondition): JSONObject = JSONObject().apply {
        put("type", c.type.name); put("value", c.value) }
    private fun condFromJson(o: JSONObject): RuleCondition = RuleCondition(
        ConditionType.valueOf(o.optString("type", ConditionType.NAME_CONTAINS.name)), o.optString("value"))
    private fun actionToJson(a: RuleAction): JSONObject = JSONObject().apply {
        put("type", a.type.name); put("targetPath", a.targetPath); put("parameter", a.parameter) }
    private fun actionFromJson(o: JSONObject): RuleAction = RuleAction(
        ActionType.valueOf(o.optString("type", ActionType.MOVE.name)),
        o.optString("targetPath"), o.optString("parameter"))
    private fun logToJson(l: RuleRunLog): JSONObject = JSONObject().apply {
        put("ruleId", l.ruleId); put("timestamp", l.timestamp)
        put("triggerType", l.triggerType.name); put("filePath", l.filePath)
        put("success", l.success); put("message", l.message) }
    private fun logFromJson(o: JSONObject): RuleRunLog = RuleRunLog(
        o.optString("ruleId"), o.optLong("timestamp"),
        RuleTrigger.valueOf(o.optString("triggerType", RuleTrigger.FILE_ADDED.name)),
        o.optString("filePath"), o.optBoolean("success", true), o.optString("message"))
}
