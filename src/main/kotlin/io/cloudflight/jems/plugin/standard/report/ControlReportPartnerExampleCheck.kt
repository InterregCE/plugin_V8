package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.pre_condition_check.ControlReportPartnerCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.report.PartnerControlReportDataProvider
import org.springframework.stereotype.Service

const val CONTROL_REPORT_MESSAGES_PREFIX = "control.report.check"

@Service
open class ControlReportPartnerExampleCheck(
    val dataProvider: PartnerControlReportDataProvider,
) : ControlReportPartnerCheckPlugin {

    override fun check(partnerId: Long, reportId: Long): PreConditionCheckResult {
        val messages = mutableListOf<PreConditionCheckMessage>()

        messages.add(checkControlIdentificationTab(data = dataProvider.getDesignatedController(partnerId, reportId = reportId)))

        return PreConditionCheckResult(
            messages = messages,
            isSubmissionAllowed = messages.none { it.messageType == MessageType.ERROR },
        )
    }

    override fun getDescription(): String =
        "Example checks for partner control report submission"

    override fun getKey() =
        "control-report-example-check"

    override fun getName() =
        "Control Report (Example) Check"

    override fun getVersion(): String =
        "0.0.1"

}