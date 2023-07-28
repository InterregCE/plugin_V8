package io.cloudflight.jems.plugin.standard.checklist.service

import io.cloudflight.jems.plugin.config.PLUGIN_DEFAULT_TEMPLATE_ENGINE
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionTypeData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistTypeData
import io.cloudflight.jems.plugin.contract.services.ProjectChecklistDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.checklist.model.Checklist
import io.cloudflight.jems.plugin.standard.common.getRidOfInvalidAsciiCharsFromXML
import io.cloudflight.jems.plugin.standard.common.pdf.PdfService
import io.cloudflight.jems.plugin.standard.common.template.CLF_UTILS
import io.cloudflight.jems.plugin.standard.common.template.TemplateUtils
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal

@Service
class ChecklistExportServiceImpl(
    val checklistDataProvider: ProjectChecklistDataProvider,
    val projectDataProvider: ProjectDataProvider,
    val pdfService: PdfService,
    @Qualifier(PLUGIN_DEFAULT_TEMPLATE_ENGINE)
    val templateEngine: ITemplateEngine
) : ChecklistExportService {

    override fun exportPdfByChecklistId(projectId: Long, checklistId: Long, exportLanguage: SystemLanguageData): Pair<String, ByteArray> {
        val checklistDetail = checklistDataProvider.getChecklistDetail(checklistId).toReportModel()
        val projectIdentification = projectDataProvider.getProjectIdentificationData(projectId)

        val scoreQuestions = checklistDetail.questions
            .filter { it.type == ChecklistQuestionTypeData.SCORE }

        val totalWeightedScore = scoreQuestions.sumOf { it.answerMetadata?.weightedScore ?: BigDecimal.ZERO }
        val maxWeightedScore = scoreQuestions.sumOf { (it.scoreMetadata?.weight ?: BigDecimal.ZERO).multiply(checklistDetail.maxScore ?: BigDecimal.ZERO) }

        val pdf = pdfService.generatePdfFromHtml(
            templateEngine.process(
                "checklist/checklist-export-template.html",
                Context().also {
                    it.locale = exportLanguage.toLocale()
                    it.setVariable("projectIdentification", projectIdentification)
                    it.setVariable("checklistDetail", checklistDetail)
                    it.setVariable("totalWeightedScore", totalWeightedScore)
                    it.setVariable("maxWeightedScore", maxWeightedScore)
                    it.setVariable("scoreQuestions", scoreQuestions)
                    it.setVariable(CLF_UTILS, TemplateUtils())
                }
            ).getRidOfInvalidAsciiCharsFromXML()
        )

        return getFileName(checklistDetail) to pdf
    }

    private fun ChecklistTypeData.toSimpleName(): String = when (this) {
        ChecklistTypeData.APPLICATION_FORM_ASSESSMENT -> "assessment-decision-checklist"
        ChecklistTypeData.CONTRACTING -> "contracting-checklist"
        ChecklistTypeData.CONTROL -> "control-checklist"
        ChecklistTypeData.VERIFICATION -> "verification-checklist"
    }

    private fun getFileName(checklist: Checklist): String =
        "${checklist.name.replace(' ', '-')}_${checklist.type.toSimpleName()}_${checklist.id}.pdf"

}