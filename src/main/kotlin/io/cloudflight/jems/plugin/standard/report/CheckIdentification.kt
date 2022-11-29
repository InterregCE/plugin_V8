package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.report.partner.identification.ProjectPartnerReportIdentificationData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildErrorPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildInfoPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildPreConditionCheckMessage
import java.time.LocalDate

private const val SECTION_MESSAGES_PREFIX = "$REPORT_MESSAGES_PREFIX.identification"
private const val SECTION_ERROR_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.error"
private const val SECTION_INFO_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.info"

fun checkIdentificationTab(data: ProjectPartnerReportIdentificationData): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "project.application.partner.report.tab.report.identification",
        messageArgs = emptyMap(),

        checkIfDateIsFilledIn(data.startDate, "start"),
        checkIfDateIsFilledIn(data.endDate, "end"),

        checkAndWarnIfSummaryIsEmpty(data.summary),
    )
}

private fun checkIfDateIsFilledIn(date: LocalDate?, fieldName: String) =
    when (date) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_ERROR_MESSAGES_PREFIX.$fieldName.not.provided")
        else -> buildInfoPreConditionCheckMessage("project.application.partner.report.$fieldName.date")
    }

private fun checkAndWarnIfSummaryIsEmpty(summary: Set<InputTranslationData>) =
    when {
        summary.firstOrNull { it.language == SystemLanguageData.EN }?.translation.isNullOrBlank() ->
            buildInfoPreConditionCheckMessage("$SECTION_INFO_MESSAGES_PREFIX.summary.in.english.not.provided")
        else -> null
    }
