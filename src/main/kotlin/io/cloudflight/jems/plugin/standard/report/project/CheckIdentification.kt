package io.cloudflight.jems.plugin.standard.report.project

import io.cloudflight.jems.plugin.contract.models.report.project.identification.ProjectReportData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildErrorPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildInfoPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildPreConditionCheckMessage
import java.time.LocalDate

private const val SECTION_MESSAGES_PREFIX = "$REPORT_MESSAGES_PREFIX.identification"
private const val SECTION_ERROR_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.error"

fun checkIdentificationTab(data: ProjectReportData): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "project.application.project.report.tab.report.identification",
        messageArgs = emptyMap(),
        checkIfDeadlinePassed(data.reportingDate)
    )
}

private fun checkIfDeadlinePassed(date: LocalDate?): PreConditionCheckMessage {
    return if(LocalDate.now() > date) {
        buildErrorPreConditionCheckMessage("$SECTION_ERROR_MESSAGES_PREFIX.reportingDate.deadline.exceeded")
    } else {
        buildInfoPreConditionCheckMessage("project.application.project.report.reportingDate.date")
    }
}

