package io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionTypeData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistStatusData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistTypeData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.services.ProjectChecklistDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.*
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import io.cloudflight.jems.plugin.standard.common.zip.ZipItem
import io.cloudflight.jems.plugin.standard.common.zip.ZipService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.regex.Pattern

@Service
open class ChecklistExport (
    val zipService: ZipService,
    val checklistDataProvider: ProjectChecklistDataProvider,
    val projectDataProvider: ProjectDataProvider,
    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
) {

    companion object {
        private val p = Pattern.compile("[^\u0009\u000A\u000D\u0020-\uD7FF\uE000-\uFFFD\u10000-\u10FFF]+")
        private fun String.getRidOfInvalidAsciiCharsFromXML() = p.matcher(this).replaceAll("")
    }

    fun export(
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        zipFileName: String,
        callId: Long?,
        checklistTypeData: ChecklistTypeData,
        isFinished: Boolean,
        isConsolidated: Boolean,
        checklistNames: List<String>): ExportResult {
        val exportDateTime = ZonedDateTime.now()

        var projects = getProjectsToExport(projectDataProvider.getProjectVersions(callId))

        var zipItems = projects
        .map{ project ->
            val checklists = checklistDataProvider.getChecklistsForProject(project.projectId, checklistTypeData)
                .filter { !isFinished || it.status == ChecklistStatusData.FINISHED }
                .filter { !isConsolidated || it.consolidated }
                .filter { checklistNames.isEmpty() || checklistNames.contains(it.name) }

            Pair(project, checklists)
        }
        .filter { it.second.isNotEmpty() }
        .flatMap{ project ->
            var projectDetails = projectDataProvider.getProjectDataForProjectId(project.first.projectId)
            var projectSectionA = projectDetails.sectionA!!
            var leadPartner = projectDetails.sectionB.partners.single { p -> p.role.isLead }

            project.second.map { checklistSummary ->
                var checklistDetail = checklistDataProvider.getChecklistDetail(checklistSummary.id).toReportModel()

                var scoreQuestions = checklistDetail.questions
                    .filter { it.type == ChecklistQuestionTypeData.SCORE }

                var maxWeightedScore = if (scoreQuestions.isEmpty())
                        BigDecimal.ZERO
                    else
                        scoreQuestions.sumOf { (it.scoreMetadata?.weight ?: BigDecimal.ZERO).multiply(checklistDetail.maxScore ?: BigDecimal.ZERO) }

                var totalWeightedScore = if (scoreQuestions.isEmpty())
                    BigDecimal.ZERO
                else
                    scoreQuestions.sumOf { it.answerMetadata?.weightedScore ?: BigDecimal.ZERO }

                var html = templateEngine.process(
                    "checklist/checklist-template",
                    Context().also {
                        it.locale = exportLanguage.toLocale()
                        it.setVariable("projectDataSectionA", projectSectionA)
                        it.setVariable("leadPartner", leadPartner)
                        it.setVariable("checklistDetail", checklistDetail)
                        it.setVariable("maxWeightedScore", maxWeightedScore)
                        it.setVariable("totalWeightedScore", totalWeightedScore)
                        it.setVariable("scoreQuestions", scoreQuestions)
                        it.setVariable(DATA_LANGUAGE, dataLanguage)
                        it.setVariable(EXPORT_LANGUAGE, exportLanguage)
                        it.setVariable(CLF_UTILS, TemplateUtils())
                    }
                ).getRidOfInvalidAsciiCharsFromXML()

                var pdf = pdfService.generatePdfFromHtml(html)

                ZipItem("${projectDetails.sectionA!!.customIdentifier}_CL${checklistSummary.id}_${checklistSummary.name}.pdf", pdf)
            }
        }

        val zipBytes = zipService.createZipFile(zipItems)

        return ExportResult(
            contentType = "application/zip",
            fileName = zipFileName,
            content = zipBytes,
            startTime = exportDateTime,
            endTime = ZonedDateTime.now()
        )
    }

    fun getProjectsToExport(projectVersions: List<ProjectVersionData>) =
        projectVersions.filterNot {
            setOf(
                ApplicationStatusData.STEP1_DRAFT,
                ApplicationStatusData.DRAFT,
                ApplicationStatusData.RETURNED_TO_APPLICANT,
                ApplicationStatusData.RETURNED_TO_APPLICANT_FOR_CONDITIONS,
                ApplicationStatusData.MODIFICATION_PRECONTRACTING,
                ApplicationStatusData.MODIFICATION_PRECONTRACTING_SUBMITTED,
                ApplicationStatusData.IN_MODIFICATION,
                ApplicationStatusData.MODIFICATION_REJECTED,
                ApplicationStatusData.MODIFICATION_SUBMITTED,
            ).contains(it.status)
        }
        .groupBy { it.projectId }.entries.mapNotNull { it.value.maxByOrNull { projectVersion -> projectVersion.createdAt } }
}
