package com.sentry.filemanager.automation

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeeklyAutomationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val rules = AutomationRuleStore.getRules(applicationContext)
                .filter { it.enabled && it.trigger == RuleTrigger.SCHEDULED_WEEKLY }

            rules.forEach { rule ->
                rule.actions.forEach { action ->
                    val result = RuleActionExecutor.execute(action, java.io.File(rule.watchPath.ifEmpty { "/" }))
                    AutomationRuleStore.addLog(
                        applicationContext,
                        RuleRunLog(
                            ruleId = rule.id,
                            triggerType = RuleTrigger.SCHEDULED_WEEKLY,
                            filePath = rule.watchPath,
                            success = result.success,
                            message = result.message
                        )
                    )
                }
                AutomationRuleStore.updateRunStats(applicationContext, rule.id)

                if (AutomationEngine.isTaskerBridgeEnabled(applicationContext)) {
                    applicationContext.sendBroadcast(
                        android.content.Intent(AutomationEngine.ACTION_RULE_TRIGGERED).apply {
                            putExtra(AutomationEngine.EXTRA_RULE_ID, rule.id)
                            putExtra(AutomationEngine.EXTRA_RULE_NAME, rule.name)
                            putExtra(AutomationEngine.EXTRA_TRIGGER, RuleTrigger.SCHEDULED_WEEKLY.name)
                        }
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
