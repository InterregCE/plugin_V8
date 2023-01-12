package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.PartnerControlReportCertificatePlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.common.UserSummaryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.contract.services.report.PartnerControlReportDataProvider
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.common.*
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
class PartnerControlReportCertificatePluginImpl(
    val reportPartnerDataProvider: ReportPartnerDataProvider,
    val reportControlPartnerDataProvider: PartnerControlReportDataProvider,
    val callDataProvider: CallDataProvider,
    val projectDataProvider: ProjectDataProvider,
    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
): PartnerControlReportCertificatePlugin {

    companion object {
        const val PARTNER_REPORT_DATA = "partnerReportData"
        const val PARTNER_REPORT_IDENTIFICATION = "partnerReportIdentification"
        const val PROJECT_IDENTIFICATION = "projectIdentification"
        const val PARTNER_CONTROL_WORK_OVERVIEW = "partnerControlWorkOverview"
        const val DESIGNATED_CONTROLLER = "reportDesignatedController"
        const val CERTIFICATE_GENERATION_DATE = "certificateGenerationDate"
        const val PARTNER_SUMMARY = "projectPartnerSummary"
        const val CURRENT_USER = "currentUser"
        const val PROGRAMME_LOGO = "logo"
    }

    override fun generateCertificate(
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
        val partnerControlWorkOverviewData = reportControlPartnerDataProvider.getControlWorkOverview(partnerId, reportId)
        val reportDesignatedController = reportControlPartnerDataProvider.getDesignatedController(partnerId, reportId)
        val projectPartnerSummary = projectDataProvider.getProjectPartnerSummaryData(partnerId)

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
                    "partner-control-report/partner-control-report-certificate-template",
                    Context().also {
                        it.locale = Locale.ENGLISH
                        it.setVariable(DATA_LANGUAGE, SystemLanguageData.EN)

                        it.setVariable(PARTNER_REPORT_DATA, partnerReportData)
                        it.setVariable(PARTNER_REPORT_IDENTIFICATION, partnerReportIdentification)
                        it.setVariable(PROJECT_IDENTIFICATION, projectIdentificationData)
                        it.setVariable(PARTNER_CONTROL_WORK_OVERVIEW, partnerControlWorkOverviewData)
                        it.setVariable(DESIGNATED_CONTROLLER, reportDesignatedController)
                        it.setVariable(PARTNER_SUMMARY, projectPartnerSummary)

                        it.setVariable(CALL_DATA, callDataProvider.getCallDataByProjectId(projectId))

                        it.setVariable(CLF_UTILS, TemplateUtils())
                        it.setVariable(CLF_PARTNER_UTILS, PartnerUtils())
                        it.setVariable(CLF_BUDGET_UTILS, BudgetUtils())
                        it.setVariable(CLF_PROJECT_UTILS, ProjectUtils())
                        it.setVariable(CERTIFICATE_GENERATION_DATE, creationDate )
                        it.setVariable(CURRENT_USER, currentUser )
                        it.setVariable(PROGRAMME_LOGO, logo)
                    }
                )
            )
        )
    }

    fun getFileName(
        projectIdentifier: String?, partnerRole: ProjectPartnerRoleData, partnerNumber: Int, reportNumber: Int
    ): String =
        "Control Certificate - $projectIdentifier - ${PartnerUtils().getPartnerNumber(partnerRole, partnerNumber)} " + "- R${reportNumber}.pdf"

    override fun getDescription(): String {
        return "Standard implementation for partner control report certificate file generation"
    }

    override fun getName(): String {
        return "Standard partner control report certificate generate"
    }

    override fun getKey(): String {
        return "standard-partner-control-report-certificate-generate-plugin"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }
}