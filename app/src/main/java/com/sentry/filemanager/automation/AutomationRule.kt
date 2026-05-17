package com.sentry.filemanager.automation
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class AutomationRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val enabled: Boolean = true,
    val trigger: RuleTrigger = RuleTrigger.FILE_ADDED,
    val watchPath: String = "",
    val conditions: List<RuleCondition> = emptyList(),
    val actions: List<RuleAction> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunAt: Long = -1L,
    val runCount: Int = 0
) : Parcelable

enum class RuleTrigger { FILE_ADDED, FILE_MODIFIED, FILE_DELETED, SCHEDULED_DAILY, SCHEDULED_WEEKLY, MANUAL }

@Parcelize
data class RuleCondition(
    val type: ConditionType = ConditionType.NAME_CONTAINS,
    val value: String = ""
) : Parcelable

enum class ConditionType { NAME_CONTAINS, NAME_MATCHES_GLOB, NAME_MATCHES_REGEX, EXTENSION_IS, SIZE_GREATER_THAN, SIZE_LESS_THAN, AGE_OLDER_THAN_DAYS }

@Parcelize
data class RuleAction(
    val type: ActionType = ActionType.MOVE,
    val targetPath: String = "",
    val parameter: String = ""
) : Parcelable

enum class ActionType { MOVE, COPY, DELETE, RENAME, COMPRESS_ZIP, EXTRACT, SHELL_COMMAND }
