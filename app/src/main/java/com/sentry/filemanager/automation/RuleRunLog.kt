package com.sentry.filemanager.automation

data class RuleRunLog(
    val ruleId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerType: RuleTrigger,
    val filePath: String,
    val success: Boolean,
    val message: String = ""
)
