package io.cloudflight.jems.plugin.standard.application_form_export

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ApplicationFormExportPlugin
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.application_form_export.timeplan.addLastPeriod
import io.cloudflight.jems.plugin.standard.application_form_export.timeplan.getTimeplanData
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.common.CLF_BUDGET_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_PARTNER_UTILS
import io.cloudflight.jems.plugin.standard.common.CLF_PROJECT_UTILS
import io.cloudflight.jems.plugin.standard.common.BudgetUtils
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import io.cloudflight.jems.plugin.standard.common.ProjectUtils
import io.cloudflight.jems.plugin.standard.common.CALL_DATA
import io.cloudflight.jems.plugin.standard.common.PROJECT_DATA
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.EXPORT_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


@Service
open class ApplicationFormExportDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val callDataProvider: CallDataProvider,
    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
) : ApplicationFormExportPlugin {

    companion object {
        private val p = Pattern.compile("[^\u0009\u000A\u000D\u0020-\uD7FF\uE000-\uFFFD\u10000-\u10FFF]+")
        private fun String.getRidOfInvalidAsciiCharsFromXML() = p.matcher(this).replaceAll("")
    }

    override fun export(
        projectId: Long,
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        localDateTime: LocalDateTime,
        version: String?,
        logo: String?
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
                    "application-form/application-form-export-template",
                    Context().also {
                        it.locale = exportLanguage.toLocale()
                        it.setVariable(PROJECT_DATA, projectData)
                        it.setVariable(CALL_DATA, callDataProvider.getCallDataByProjectId(projectId))
                        it.setVariable(DATA_LANGUAGE, dataLanguage)
                        it.setVariable(EXPORT_LANGUAGE, exportLanguage)
                        it.setVariable(CLF_UTILS, TemplateUtils())
                        it.setVariable(CLF_PARTNER_UTILS, PartnerUtils())
                        it.setVariable(CLF_BUDGET_UTILS, BudgetUtils())
                        it.setVariable(CLF_PROJECT_UTILS, ProjectUtils())
                        it.setVariable("timeplanData", getTimeplanData(
                            periods = projectData.sectionA?.periods?.addLastPeriod() ?: emptyList(),
                            workPackages = projectData.sectionC.projectWorkPackages,
                            results = projectData.sectionC.projectResults,
                            language = dataLanguage,
                        ))
                        it.setVariable("programmeTitle", projectData.programmeTitle)
                        it.setVariable("downloadedDateTime", localDateTime.format(
                            DateTimeFormatter.ofPattern("dd.MM.yyy, HH:mm")))
                        it.setVariable("downloadedDate", localDateTime.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        it.setVariable("version", version)
                        it.setVariable("logo", logo)
                    }
                ).getRidOfInvalidAsciiCharsFromXML()
            )
        )
    }

    fun getFileName(
        projectAcronym: String?, projectCustomIdentifier: String?, exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${projectCustomIdentifier}_${projectAcronym}_${exportLanguage.name.lowercase()}_${dataLanguage.name.lowercase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"

    override fun getDescription(): String =
        "Standard implementation for application form exportation"

    override fun getKey() =
        "standard-application-form-export-plugin"

    override fun getName() =
        "Standard application form export"

    override fun getVersion(): String =
        "1.0.22"
}
