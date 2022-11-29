package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.pre_condition_check.ReportPartnerCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import org.springframework.stereotype.Service

const val REPORT_MESSAGES_PREFIX = "report.check"

@Service
open class ReportPartnerExampleCheck(
    val dataProvider: ReportPartnerDataProvider,
) : ReportPartnerCheckPlugin {

    override fun check(partnerId: Long, reportId: Long): PreConditionCheckResult {
        val messages = mutableListOf<PreConditionCheckMessage>()

        messages.add(checkIdentificationTab(data = dataProvider.getIdentification(partnerId, reportId = reportId)))
        messages.add(checkExpenditureTab(data = dataProvider.getExpenditureCosts(partnerId, reportId = reportId)))

        return PreConditionCheckResult(
            messages = messages,
            isSubmissionAllowed = messages.none { it.messageType == MessageType.ERROR },
        )
    }

    override fun getDescription(): String =
        "Example checks for partner report submission"

    override fun getKey() =
        "report-example-check"

    override fun getName() =
        "Report (Example) Check"

    override fun getVersion(): String =
        "0.0.1"

}
