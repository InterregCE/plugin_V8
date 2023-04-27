package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.partner.report.PartnerControlReportExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.common.UserSummaryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.contract.services.report.PartnerControlReportDataProvider
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.CALL_DATA
import io.cloudflight.jems.plugin.standard.common.CLF_PARTNER_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_BUDGET_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_PROJECT_UTILS
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import io.cloudflight.jems.plugin.standard.common.ProjectUtils
import io.cloudflight.jems.plugin.standard.common.BudgetUtils
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.util.Locale

@Service
class PartnerControlReportExportPlugin(
    private val reportPartnerDataProvider: ReportPartnerDataProvider,
    private val reportControlPartnerDataProvider: PartnerControlReportDataProvider,
    private val callDataProvider: CallDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    private val templateEngine: ITemplateEngine
): PartnerControlReportExportPlugin {

    companion object {
        const val PARTNER_REPORT_DATA = "partnerReportData"
        const val PARTNER_REPORT_IDENTIFICATION = "partnerReportIdentification"
        const val PROJECT_IDENTIFICATION = "projectIdentification"
        const val DESIGNATED_CONTROLLER = "reportDesignatedController"
        const val REPORT_VERIFICATION = "reportVerification"
        const val REPORT_CONTROL_OVERVIEW = "reportControlOverview"
        const val REPORT_CONTROL_WORK_OVERVIEW = "reportControlWorkOverview"
        const val REPORT_CONTROL_DEDUCTION_OVERVIEW= "controlDeductionOverview"
        const val PROJECT_PARTNER_BUDGET_OPTIONS = "partnerBudgetOptions"
        const val CURRENT_USER = "currentUser"
        const val EXPORT_DATE = "exportDate"
        const val PROGRAMME_LOGO = "logo"
    }

    override fun export(
        projectId: Long,
        partnerId: Long,
        reportId: Long,
        currentUser: UserSummaryData,
        logo: String?,
        creationDate: LocalDateTime
    ): ExportResult {
        val partnerReportData = reportPartnerDataProvider.get(partnerId, reportId)
        val partnerReportIdentification = reportPartnerDataProvider.getIdentification(partnerId, reportId)
        val projectIdentificationData = projectDataProvider.getProjectIdentificationData(projectId)
        val designatedControllerData = reportControlPartnerDataProvider.getDesignatedController(partnerId, reportId)
        val reportVerification = reportControlPartnerDataProvider.getControlReportVerification(partnerId, reportId)
        val reportControlOverview  = reportControlPartnerDataProvider.getControlOverview(partnerId, reportId)
        val reportControlWorkOverview = reportControlPartnerDataProvider.getControlWorkOverview(partnerId, reportId)
        val controlDeductionOverview = reportControlPartnerDataProvider.getReportControlDeductionOverview(partnerId, reportId)
        val partnerBudgetOptions = projectDataProvider.getProjectPartnerBudgetOptions(partnerId, version = partnerReportData.version)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            fileName = getFileName(
                partnerReportData.projectIdentifier,
                partnerReportData.partnerRole,
                partnerReportData.partnerNumber,
                partnerReportData.reportNumber
            ),
            content = pdfService.generatePdfFromHtml(
                templateEngine.process(
                    "report/partner/control/partner-control-report",
                    Context().also {
                        it.locale = Locale.ENGLISH
                        it.setVariable(DATA_LANGUAGE, SystemLanguageData.EN)
                        it.setVariable(PARTNER_REPORT_DATA, partnerReportData)
                        it.setVariable(PARTNER_REPORT_IDENTIFICATION, partnerReportIdentification)
                        it.setVariable(PROJECT_IDENTIFICATION, projectIdentificationData)
                        it.setVariable(DESIGNATED_CONTROLLER, designatedControllerData)
                        it.setVariable(REPORT_VERIFICATION, reportVerification)
                        it.setVariable(REPORT_CONTROL_WORK_OVERVIEW, reportControlWorkOverview)
                        it.setVariable(REPORT_CONTROL_OVERVIEW, reportControlOverview)
                        it.setVariable(REPORT_CONTROL_DEDUCTION_OVERVIEW, controlDeductionOverview)
                        it.setVariable(PROJECT_PARTNER_BUDGET_OPTIONS, partnerBudgetOptions)

                        it.setVariable(CALL_DATA, callDataProvider.getCallDataByProjectId(projectId))
                        it.setVariable(CLF_UTILS, TemplateUtils())
                        it.setVariable(CLF_PARTNER_UTILS, PartnerUtils())
                        it.setVariable(CLF_BUDGET_UTILS, BudgetUtils())
                        it.setVariable(CLF_PROJECT_UTILS, ProjectUtils())
                        it.setVariable(EXPORT_DATE, creationDate )
                        it.setVariable(CURRENT_USER, currentUser )
                        it.setVariable(PROGRAMME_LOGO, logo)
                    }
                )
            )
        )
    }
    override fun getDescription(): String = "PDF export of partner control report"
    override fun getName(): String = "Standard partner control report export plugin"
    override fun getKey() = "standard-partner-control-report-export-plugin"
    override fun getVersion(): String = "1.0.1"

    fun getFileName(
        projectIdentifier: String?, partnerRole: ProjectPartnerRoleData, partnerNumber: Int, reportNumber: Int
    ): String =
        "Control Report - $projectIdentifier - ${PartnerUtils().getPartnerNumber(partnerRole, partnerNumber)} " + "- R${reportNumber}.pdf"
}