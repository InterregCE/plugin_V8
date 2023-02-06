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
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.stream.Collectors


@Service
open class ProgrammeProjectDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val callDataProvider: CallDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {

    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammeProjectDataExportDefaultImpl::class.java)
    }

    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val exportationDateTime = ZonedDateTime.now()
        val failedProjectIds = Collections.synchronizedSet(HashSet<Long>())

        val projectAndCallDataList = getProjectsToExport(projectDataProvider.getAllProjectVersions()).parallelStream().map { projectVersion ->
            runCatching {
                return@map ProjectAndCallData(
                    projectDataProvider.getProjectDataForProjectId(
                        projectVersion.projectId,
                        projectVersion.version
                    ),
                    callDataProvider.getCallDataByProjectId(projectVersion.projectId),
                    projectVersion
                )
            }.onFailure {
                logger.warn("Failed to fetch data for project with id=${projectVersion.projectId}", it)
                failedProjectIds.add(projectVersion.projectId)
                return@map null
            }.getOrNull()
        }.filter { it != null }.map { it!! }.collect(Collectors.toList())

        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName = getFileName(programmeData.title, exportationDateTime, exportLanguage, dataLanguage),
            content = excelService.generateExcel(
                ProgrammeProjectDataGenerator(
                    projectAndCallDataList, failedProjectIds, programmeData, exportationDateTime,
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
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_project_${exportLanguage.name.lowercase()}_${dataLanguage.name.lowercase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

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

    override fun getDescription(): String =
        "Standard implementation for programme project data exportation"

    override fun getKey() =
        "standard-programme-project-data-export-plugin"

    override fun getName() =
        "Standard programme project data export"

    override fun getVersion(): String =
        "1.0.7"
}