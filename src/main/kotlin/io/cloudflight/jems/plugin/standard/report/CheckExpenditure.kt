package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.models.report.partner.expenditure.ProjectPartnerReportExpenditureCostData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildErrorPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildPreConditionCheckMessage

private const val SECTION_MESSAGES_PREFIX = "$REPORT_MESSAGES_PREFIX.expenditure"
private const val SECTION_ERROR_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.error"

fun checkExpenditureTab(data: List<ProjectPartnerReportExpenditureCostData>): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "project.application.partner.report.expenditures.title",
        messageArgs = emptyMap(),

        checkIfThereIsAtLeastOneExpenditure(data),
    )
}

private fun checkIfThereIsAtLeastOneExpenditure(data: List<ProjectPartnerReportExpenditureCostData>) =
    when {
        data.isEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_ERROR_MESSAGES_PREFIX.empty")
        else -> null
    }
