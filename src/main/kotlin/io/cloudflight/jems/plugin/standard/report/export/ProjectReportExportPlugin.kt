package io.cloudflight.jems.plugin.standard.report.export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import org.springframework.stereotype.Service
import io.cloudflight.jems.plugin.contract.export.project.report.ProjectReportExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.report.ProjectReportDataProvider
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime


@Service
class ProjectReportExportPlugin(
    private val dataProvider: ProjectReportDataProvider,
    private val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
): ProjectReportExportPlugin {

    companion object {
        const val PROJECT_REPORT_DATA = "projectReportData"
        const val PROGRAMME_LOGO = "logo"
    }

    override fun export(
        projectId: Long,
        reportId: Long,
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        logo: String?,
        localDateTime: LocalDateTime?
    ): ExportResult {
        val projectReportData = dataProvider.get(projectId, reportId)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            content =  pdfService.generatePdfFromHtml(
                templateEngine.process(
                    "project-report/project-report-template",
                    Context().also {
                        it.locale = exportLanguage.toLocale()
                        it.setVariable(DATA_LANGUAGE, dataLanguage)
                        it.setVariable(PROJECT_REPORT_DATA, projectReportData)
                        it.setVariable(CLF_UTILS, TemplateUtils())
                        it.setVariable(PROGRAMME_LOGO, logo)
                    }
                )
            ),
            fileName = getFileName(
                projectReportData.projectIdentifier,
                projectReportData.reportNumber,
            )
        )
    }

    private fun getFileName(projectIdentifier: String?, reportNumber: Int): String =
        "Report_export_${projectIdentifier}_R${reportNumber}.pdf"

    override fun getDescription() = "Standard project report (example) export plugin"

    override fun getName() = "Project Report (Example) export"

    override fun getKey() = "standard-project-report-export-plugin"

    override fun getVersion() = "1.0.0"
}