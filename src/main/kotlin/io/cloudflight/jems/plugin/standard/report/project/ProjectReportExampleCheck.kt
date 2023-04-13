package io.cloudflight.jems.plugin.standard.report.project

import io.cloudflight.jems.plugin.contract.pre_condition_check.ReportProjectCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.report.ProjectReportDataProvider
import org.springframework.stereotype.Service

const val REPORT_MESSAGES_PREFIX = "project.report.check"

@Service
open class ProjectReportExampleCheck(
    val dataProvider: ProjectReportDataProvider,
) : ReportProjectCheckPlugin {

    override fun check(projectId: Long, reportId: Long): PreConditionCheckResult {
        val messages = mutableListOf<PreConditionCheckMessage>()

        messages.add(checkIdentificationTab(data = dataProvider.get(projectId, reportId = reportId)))

        return PreConditionCheckResult(
            messages = messages,
            isSubmissionAllowed = messages.none { it.messageType == MessageType.ERROR },
        )
    }

    override fun getDescription(): String =
        "Example checks for project report submission"

    override fun getKey() =
        "project-report-example-check"

    override fun getName() =
        "Project Report (Example) Check"

    override fun getVersion(): String =
        "0.0.1"

}
