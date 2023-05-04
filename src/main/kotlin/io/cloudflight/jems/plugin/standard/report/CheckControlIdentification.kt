package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.models.common.UserSummaryData
import io.cloudflight.jems.plugin.contract.models.report.partner.control.ReportDesignatedControllerData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildErrorPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildInfoPreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.pre_condition_check.buildPreConditionCheckMessage

private const val SECTION_MESSAGES_PREFIX = "$CONTROL_REPORT_MESSAGES_PREFIX.identification"
private const val SECTION_ERROR_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.error"
private const val SECTION_INFO_MESSAGES_PREFIX = "$SECTION_MESSAGES_PREFIX.info"
fun checkControlIdentificationTab(data: ReportDesignatedControllerData): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "project.application.partner.control.report.tab.report.identification",
        messageArgs = emptyMap(),
        checkIfControllerUserIsAssigned(data.controllerUser)
    )
}

private fun checkIfControllerUserIsAssigned(controllerUser: UserSummaryData?) =
    when (controllerUser) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_ERROR_MESSAGES_PREFIX.controlUser.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_INFO_MESSAGES_PREFIX.controlUser")
    }