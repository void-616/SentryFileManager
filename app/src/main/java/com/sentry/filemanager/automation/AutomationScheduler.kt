package com.sentry.filemanager.automation

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object AutomationScheduler {

    private const val DAILY_WORK_NAME = "sentry_automation_daily"
    private const val WEEKLY_WORK_NAME = "sentry_automation_weekly"

    fun scheduleAll(context: Context) {
        val rules = AutomationRuleStore.getRules(context)
        val hasDaily = rules.any { it.enabled && it.trigger == RuleTrigger.SCHEDULED_DAILY }
        val hasWeekly = rules.any { it.enabled && it.trigger == RuleTrigger.SCHEDULED_WEEKLY }

        if (hasDaily) scheduleDaily(context) else cancelDaily(context)
        if (hasWeekly) scheduleWeekly(context) else cancelWeekly(context)
    }

    private fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyAutomationWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleWeekly(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeeklyAutomationWorker>(7, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun cancelDaily(context: Context) =
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_WORK_NAME)

    private fun cancelWeekly(context: Context) =
        WorkManager.getInstance(context).cancelUniqueWork(WEEKLY_WORK_NAME)
}
