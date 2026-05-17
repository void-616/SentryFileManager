package com.sentry.filemanager.automation
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import java.io.File

object AutomationEngine {
    private const val PREF_TASKER_BRIDGE = "sentry_automation_tasker_bridge"
    const val ACTION_RULE_TRIGGERED = "com.sentry.filemanager.RULE_TRIGGERED"
    const val EXTRA_RULE_ID = "rule_id"
    const val EXTRA_RULE_NAME = "rule_name"
    const val EXTRA_FILE_PATH = "file_path"
    const val EXTRA_TRIGGER = "trigger"

    fun processEvent(context: Context, trigger: RuleTrigger, filePath: String) {
        val file = File(filePath)
        AutomationRuleStore.getRules(context)
            .filter { it.enabled && it.trigger == trigger }
            .filter { it.watchPath.isEmpty() || filePath.startsWith(it.watchPath) }
            .forEach { rule ->
                if (!RuleConditionEvaluator.evaluate(rule.conditions, file)) return@forEach
                var last: RuleActionExecutor.ActionResult? = null
                for (action in rule.actions) {
                    last = RuleActionExecutor.execute(action, file)
                    if (!last.success) break
                }
                AutomationRuleStore.addLog(context, RuleRunLog(
                    ruleId = rule.id, triggerType = trigger, filePath = filePath,
                    success = last?.success ?: true, message = last?.message ?: "No actions"
                ))
                AutomationRuleStore.updateRunStats(context, rule.id)
                if (isTaskerBridgeEnabled(context)) fireTaskerIntent(context, rule, filePath, trigger)
            }
    }

    fun isTaskerBridgeEnabled(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_TASKER_BRIDGE, false)

    fun setTaskerBridgeEnabled(context: Context, enabled: Boolean) =
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(PREF_TASKER_BRIDGE, enabled).apply()

    private fun fireTaskerIntent(context: Context, rule: AutomationRule, filePath: String, trigger: RuleTrigger) {
        context.sendBroadcast(Intent(ACTION_RULE_TRIGGERED).apply {
            putExtra(EXTRA_RULE_ID, rule.id); putExtra(EXTRA_RULE_NAME, rule.name)
            putExtra(EXTRA_FILE_PATH, filePath); putExtra(EXTRA_TRIGGER, trigger.name)
        })
    }
}
