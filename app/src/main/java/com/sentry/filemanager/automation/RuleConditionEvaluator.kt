package com.sentry.filemanager.automation
import java.io.File

object RuleConditionEvaluator {

    fun evaluate(conditions: List<RuleCondition>, file: File): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { matches(it, file) }
    }

    private fun matches(condition: RuleCondition, file: File): Boolean = try {
        when (condition.type) {
            ConditionType.NAME_CONTAINS ->
                file.name.contains(condition.value, ignoreCase = true)
            ConditionType.NAME_MATCHES_GLOB ->
                globMatches(condition.value, file.name)
            ConditionType.NAME_MATCHES_REGEX ->
                Regex(condition.value, RegexOption.IGNORE_CASE).containsMatchIn(file.name)
            ConditionType.EXTENSION_IS ->
                file.extension.equals(condition.value.trimStart('.'), ignoreCase = true)
            ConditionType.SIZE_GREATER_THAN ->
                file.length() > (condition.value.toLongOrNull() ?: 0L)
            ConditionType.SIZE_LESS_THAN ->
                file.length() < (condition.value.toLongOrNull() ?: Long.MAX_VALUE)
            ConditionType.AGE_OLDER_THAN_DAYS -> {
                val days = condition.value.toLongOrNull() ?: return false
                file.lastModified() < System.currentTimeMillis() - (days * 86400000L)
            }
        }
    } catch (e: Exception) { false }

    private fun globMatches(pattern: String, name: String): Boolean {
        val regex = buildString {
            for (c in pattern) when (c) {
                '*' -> append(".*")
                '?' -> append(".")
                '.' -> append("\\.")
                else -> append(Regex.escape(c.toString()))
            }
        }
        return Regex(regex, RegexOption.IGNORE_CASE).matches(name)
    }
}
