package io.cloudflight.jems.plugin.standard.programme_data_export.programme_partner_data_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProjectAndCallAndPartnerData
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class ProgrammePartnerDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val callDataProvider: CallDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {

    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val exportationDateTime = ZonedDateTime.now()

        val partnersToExport = mutableListOf<ProjectAndCallAndPartnerData>()
        getProjectVersionsToExport(projectDataProvider.getAllProjectVersions()).map { projectVersion ->
            val projectData =
                projectDataProvider.getProjectDataForProjectId(projectVersion.projectId, projectVersion.version)
            val callData = callDataProvider.getCallDataByProjectId(projectVersion.projectId)
            projectData.sectionB.partners.forEach { partner ->
                partnersToExport.add(
                    ProjectAndCallAndPartnerData(
                        projectVersion,
                        projectData,
                        callData,
                        partner
                    )
                )
            }
        }

        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName = getFileName(programmeData.title, exportationDateTime, exportLanguage, dataLanguage),
            content = excelService.generateExcel(
                ProgrammePartnerDataGenerator(
                    partnersToExport, programmeData, exportationDateTime,
                    exportLanguage, dataLanguage, messageSource
                ).getData()
            ),
            startTime = exportationDateTime,
            endTime = ZonedDateTime.now()
        )
    }

    private fun getProjectVersionsToExport(projectVersions: List<ProjectVersionData>) =
        projectVersions.filter {
            it.status !in setOf(
                ApplicationStatusData.STEP1_DRAFT,
                ApplicationStatusData.DRAFT,
                ApplicationStatusData.RETURNED_TO_APPLICANT,
                ApplicationStatusData.RETURNED_TO_APPLICANT_FOR_CONDITIONS,
                ApplicationStatusData.MODIFICATION_PRECONTRACTING,
                ApplicationStatusData.MODIFICATION_PRECONTRACTING_SUBMITTED,
                ApplicationStatusData.IN_MODIFICATION,
                ApplicationStatusData.MODIFICATION_REJECTED,
                ApplicationStatusData.MODIFICATION_SUBMITTED
            )
        }
            .groupBy { it.projectId }.entries.mapNotNull { it.value.maxByOrNull { projectVersion -> projectVersion.createdAt } }

    fun getFileName(
        programmeTitle: String?, exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_partner_${
            exportLanguage.name.toLowerCase(Locale.getDefault())
        }_${dataLanguage.name.toLowerCase(Locale.getDefault())}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

    override fun getDescription(): String =
        "Standard implementation for programme partner data exportation"

    override fun getKey() =
        "standard-programme-partner-data-export-plugin"

    override fun getName() =
        "Standard programme partner data export"

    override fun getVersion(): String =
        "1.0.1"
}
