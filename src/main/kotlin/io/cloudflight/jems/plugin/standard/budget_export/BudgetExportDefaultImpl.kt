package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.export.BudgetExportPlugin
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.common.excel.model.BorderStyle
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
open class BudgetExportDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val callDataProvider: CallDataProvider,
    val excelService: ExcelService,
    private val messageSource: MessageSource,
) : BudgetExportPlugin {
    override fun export(
        projectId: Long, exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData, version: String?
    ): ExportResult {
        val projectData = projectDataProvider.getProjectDataForProjectId(projectId, version)
        val callData = callDataProvider.getCallDataByProjectId(projectId)
        val exportLocale = exportLanguage.toLocale()
        val exportationTime = ZonedDateTime.now()

        val excelData = ExcelData()
        val sheet = excelData.addSheet("Project_budget_${projectData.sectionA?.customIdentifier}_${projectData.sectionA?.acronym}")

        val data = BudgetAndLumpTotalsTableGenerator(projectData, callData, exportLanguage, messageSource)

        sheet.addRow(CellData(getTitle(projectData, version, exportationTime)).removeBorders())
        sheet.addRow(CellData(getMessage("project.application.form.section.part.d", exportLocale, messageSource)))
        sheet.addRow(CellData(getMessage("project.application.form.section.part.d.subsection.one", exportLocale, messageSource)))
        sheet.addRows(data.getDataFirst())

        sheet.addRow(CellData(null).removeBorders())
        sheet.addRow(CellData(getMessage("project.application.form.section.part.d.subsection.two", exportLocale, messageSource)))
        sheet.addRows(data.getDataSecond())

        if (data.shouldBeVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS)) {
            sheet.addRow(CellData(null).removeBorders())
            sheet.addRow(CellData(getMessage("project.application.form.section.part.d.subsection.three", exportLocale, messageSource)))
            sheet.addRows(data.getDataThird())
        }

        val sheetPartner = excelData.addSheet("Partner_budget_${projectData.sectionA?.customIdentifier}_${projectData.sectionA?.acronym}")

        sheetPartner.addRow(CellData(getTitle(projectData, version, exportationTime)).removeBorders())
        sheetPartner.addRow(CellData(getMessage("project.partner.budget.overview.header", exportLocale, messageSource)))
        sheetPartner.addRows(BudgetDetailsTableGenerator(projectData, callData, exportLanguage, dataLanguage, messageSource).getData())

        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName = getFileName(
                projectData.sectionA?.acronym, projectData.sectionA?.customIdentifier, exportationTime
            ),
            content = excelService.generateExcel(excelData)
        )
    }

    private fun CellData.removeBorders() = borderBottom(BorderStyle.NONE)
        .borderTop(BorderStyle.NONE)
        .borderLeft(BorderStyle.NONE)
        .borderRight(BorderStyle.NONE)

    override fun getDescription(): String =
        "Standard implementation for budget exportation"

    override fun getKey() =
        "standard-budget-export-plugin"

    override fun getName() =
        "Standard budget export"

    override fun getVersion(): String =
        "1.0.12"
}
