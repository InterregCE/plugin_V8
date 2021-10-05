package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.budget_export.BudgetExportPlugin
import io.cloudflight.jems.plugin.contract.budget_export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.CsvService
import org.springframework.stereotype.Service


@Service
open class BudgetExportDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val csvService: CsvService
) : BudgetExportPlugin {
    override fun export(
        projectId: Long, exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData, version: String?
    ): ExportResult =
        with(mutableListOf<List<String?>>()) {
            val projectData = projectDataProvider.getProjectDataForProjectId(projectId, version)
            add(listOf("Budget and Lump sum Totals"))
            add(
                listOf(
                    "Partner number",
                    "Partner abbreviation",
                    "Partner original language",
                    "Country",
                    "NUTS 3",
                    "NUTS 2"
                )
            )
            projectData.sectionB.partners.forEach {
                val address = it.addresses.firstOrNull { it.type === ProjectPartnerAddressTypeData.Department }
                add(
                    listOf(
                        it.sortNumber.toString(),
                        it.abbreviation,
                        it.nameInOriginalLanguage,
                        address?.country,
                        address?.nutsRegion3,
                        address?.nutsRegion2
                    )
                )
            }

            ExportResult(
                contentType = "text/csv",
                fileName = "${projectData.sectionA?.acronym}($projectId)_Budget_Lumpsum.xls",
                content = csvService.generateCsv(this)
            )
        }


    override fun getDescription(): String =
        "Standard implementation for budget exportation"

    override fun getKey() =
        "standard-budget-export-plugin"

    override fun getName() =
        "Standard budget export"

    override fun getVersion(): String =
        "1.0.0"
}
