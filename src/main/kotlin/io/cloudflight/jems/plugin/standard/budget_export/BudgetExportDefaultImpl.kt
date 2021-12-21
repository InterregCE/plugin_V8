package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.export.BudgetExportPlugin
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.budget_export.models.CsvData
import io.cloudflight.jems.plugin.standard.common.csv.CsvService
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime


@Service
open class BudgetExportDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val callDataProvider: CallDataProvider,
    val csvService: CsvService,
    private val messageSource: MessageSource,
) : BudgetExportPlugin {
    override fun export(
        projectId: Long, exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData, version: String?
    ): ExportResult =
        with(CsvData()) {
            val projectData = projectDataProvider.getProjectDataForProjectId(projectId, version)
            val callData = callDataProvider.getCallDataByProjectId(projectId)
            val exportLocale = exportLanguage.toLocale()
            val exportationTime = ZonedDateTime.now()

            addRow(getTitle(projectData, version, exportationTime))
            addRow(getMessage("jems.standard.budget.export.budget.totals.header", exportLocale, messageSource))
            addRows(BudgetAndLumpTotalsTableGenerator(projectData, callData, exportLanguage, messageSource).getData())
            addEmptyRow()
            addRow(getMessage("jems.standard.budget.export.budget.details.header", exportLocale, messageSource))
            addRows(
                BudgetDetailsTableGenerator(
                    projectData, callData, exportLanguage, dataLanguage, messageSource
                ).getData()
            )

            return ExportResult(
                contentType = "text/csv",
                fileName = getFileName(
                    projectData.sectionA?.acronym, projectData.sectionA?.customIdentifier, exportationTime
                ),
                content = csvService.generateCsv(getContent())
            )
        }

    override fun getDescription(): String =
        "Standard implementation for budget exportation"

    override fun getKey() =
        "standard-budget-export-plugin"

    override fun getName() =
        "Standard budget export"

    override fun getVersion(): String =
        "1.0.2"
}
