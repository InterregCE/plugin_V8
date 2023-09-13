package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.project.report.ProjectReportVerificationCertificatePlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.common.UserSummaryData
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.contract.services.report.ProjectReportDataProvider
import io.cloudflight.jems.plugin.contract.services.report.ProjectReportVerificationDataProvider
import io.cloudflight.jems.plugin.standard.common.BudgetUtils
import io.cloudflight.jems.plugin.standard.common.CLF_BUDGET_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_PARTNER_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_PROJECT_UTILS
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import io.cloudflight.jems.plugin.standard.common.ProjectUtils
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
class ProjectReportVerificationCertificatePluginImpl(
    val projectDataProvider: ProjectDataProvider,
    val projectReportDataProvider: ProjectReportDataProvider,
    val projectReportVerificationDataProvider: ProjectReportVerificationDataProvider,

    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE) val templateEngine: ITemplateEngine,
): ProjectReportVerificationCertificatePlugin {


    companion object {
        const val PROJECT_IDENTIFICATION = "projectIdentification"
        const val PROJECT_REPORT_IDENTIFICATION = "projectReportIdentification"

        const val CERTIFICATE_GENERATION_DATE = "certificateGenerationDate"
        const val CURRENT_USER = "currentUser"
        const val PROGRAMME_LOGO = "logo"
    }

    override fun generateCertificate(
        projectId: Long,
        reportId: Long,
        currentUser: UserSummaryData,
        logo: String?,
        creationDate: LocalDateTime,
    ): ExportResult {
        val projectIdentificationData = projectDataProvider.getProjectIdentificationData(projectId)
        val projectReportData = projectReportDataProvider.get(projectId, reportId)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            fileName = getFileName(
                projectIdentifier = projectIdentificationData.customIdentifier,
                reportNumber = projectReportData.reportNumber
            ),
            content = pdfService.generatePdfFromHtml(
                templateEngine.process(
                    "report/project/verification/project-report-verification-certificate-template.html",
                    Context().also {
                        it.locale = Locale.ENGLISH
                        it.setVariable(DATA_LANGUAGE, SystemLanguageData.EN)

                        it.setVariable(PROJECT_IDENTIFICATION, projectIdentificationData)
                        it.setVariable(PROJECT_REPORT_IDENTIFICATION, projectReportData)

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
        projectIdentifier: String?, reportNumber: Int
    ): String =
        "Verification Certificate - $projectIdentifier - PR$reportNumber.pdf"

    override fun getDescription(): String {
        return "Standard implementation for project report verification certificate file generation"
    }

    override fun getName(): String {
        return "Verification certificate"
    }

    override fun getKey(): String {
        return "standard-project-report-verification-certificate-generate-plugin"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }
}

