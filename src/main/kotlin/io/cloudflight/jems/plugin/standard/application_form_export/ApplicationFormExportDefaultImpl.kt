package io.cloudflight.jems.plugin.standard.application_form_export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ApplicationFormExportPlugin
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.*
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
open class ApplicationFormExportDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val callDataProvider: CallDataProvider,
    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
) : ApplicationFormExportPlugin {
    override fun export(
        projectId: Long, exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData, version: String?
    ): ExportResult {
        val projectData = projectDataProvider.getProjectDataForProjectId(projectId, version)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            fileName = getFileName(
                projectData.sectionA?.acronym, projectData.sectionA?.customIdentifier,
                ZonedDateTime.now(), exportLanguage, dataLanguage
            ),
            content = pdfService.generatePdfFromHtml(
                templateEngine.process(
                    "application-form-export-template",
                    Context().also {
                        it.locale = exportLanguage.toLocale()
                        it.setVariable(PROJECT_DATA, projectData)
                        it.setVariable(CALL_DATA, callDataProvider.getCallDataByProjectId(projectId))
                        it.setVariable(DATA_LANGUAGE, dataLanguage)
                        it.setVariable(EXPORT_LANGUAGE, exportLanguage)
                        it.setVariable(CLF_UTILS, TemplateUtils())
                    }
                )
            )
        )
    }

    fun getFileName(
        projectAcronym: String?, projectCustomIdentifier: String?, exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${projectCustomIdentifier}_${projectAcronym}_${exportLanguage.name.toLowerCase()}_${dataLanguage.name.toLowerCase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyMd_Hmss"))}.pdf"

    override fun getDescription(): String =
        "Standard implementation for application form exportation"

    override fun getKey() =
        "standard-application-form-export-plugin"

    override fun getName() =
        "Standard application form export"

    override fun getVersion(): String =
        "1.0.0"
}