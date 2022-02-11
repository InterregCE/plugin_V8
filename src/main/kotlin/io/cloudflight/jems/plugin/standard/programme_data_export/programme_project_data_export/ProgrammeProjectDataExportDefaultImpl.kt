package io.cloudflight.jems.plugin.standard.programme_data_export.programme_project_data_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProjectAndCallData
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Service
open class ProgrammeProjectDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val callDataProvider: CallDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {
    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val exportationDateTime = ZonedDateTime.now()

        val projectAndCallDataList = mutableListOf<ProjectAndCallData>()
        getProjectsToExport(projectDataProvider.getAllProjectVersions()).forEach { projectVersion ->
            projectAndCallDataList.add(
                ProjectAndCallData(
                    projectDataProvider.getProjectDataForProjectId(projectVersion.projectId, projectVersion.version),
                    callDataProvider.getCallDataByProjectId(projectVersion.projectId),
                    projectVersion
                )
            )
        }

        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName = getFileName(programmeData.title, exportationDateTime, exportLanguage, dataLanguage),
            content = excelService.generateExcel(
                ProgrammeProjectDataGenerator(
                    projectAndCallDataList, programmeData, exportationDateTime,
                    exportLanguage, dataLanguage, messageSource
                ).getData()
            ),
            startTime = exportationDateTime,
            endTime = ZonedDateTime.now()
        )

    }

    fun getFileName(
        programmeTitle: String?,
        exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_project_${exportLanguage.name.toLowerCase()}_${dataLanguage.name.toLowerCase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

    fun getProjectsToExport(projectVersions: List<ProjectVersionData>) =
        projectVersions.filter {
            setOf(ApplicationStatusData.APPROVED, ApplicationStatusData.SUBMITTED).contains(it.status)
        }
            .groupBy { it.projectId }.entries.mapNotNull { it.value.maxByOrNull { projectVersion -> projectVersion.createdAt } }

    override fun getDescription(): String =
        "Standard implementation for programme project data exportation"

    override fun getKey() =
        "standard-programme-project-data-export-plugin"

    override fun getName() =
        "Standard programme project data export"

    override fun getVersion(): String =
        "1.0.1"
}