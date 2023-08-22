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
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.Locale

@Service
class ProgrammePartnerDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val projectDataProvider: ProjectDataProvider,
    private val callDataProvider: CallDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammePartnerDataExportDefaultImpl::class.java)

        private const val optionCallId = "call_id"
    }

    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData, pluginOptions: String): ExportResult {

        val programmeData = programmeDataProvider.getProgrammeData()
        val exportationDateTime = ZonedDateTime.now()
        val failedProjectIds = Collections.synchronizedSet(HashSet<Long>())
        val pluginOptionsMap = getOptionsMap(pluginOptions)

        val projectVersions = getProjectsVersionsToExport(pluginOptionsMap)

        val partnersToExport = projectVersions.parallelStream().flatMap { projectVersion ->
                runCatching {
                    val projectData = projectDataProvider.getProjectDataForProjectId(projectVersion.projectId, projectVersion.version)
                    val callData = callDataProvider.getCallDataByProjectId(projectVersion.projectId)
                    return@flatMap projectData.sectionB.partners.stream().map { partner ->
                        ProjectAndCallAndPartnerData(
                            projectVersion,
                            projectData,
                            callData,
                            partner
                        )
                    }
                }.onFailure {
                    logger.warn("Failed to fetch data for project with id=${projectVersion.projectId}", it)
                    failedProjectIds.add(projectVersion.projectId)
                    return@flatMap Stream.empty()
                }.getOrNull()
            }.collect(Collectors.toList())

        return ExportResult(
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName = getFileName(programmeData.title, exportationDateTime, exportLanguage, dataLanguage),
            content = excelService.generateExcel(
                ProgrammePartnerDataGenerator(
                    partnersToExport, programmeData, failedProjectIds, exportationDateTime,
                    exportLanguage, dataLanguage, messageSource
                ).getData()
            ),
            startTime = exportationDateTime,
            endTime = ZonedDateTime.now()
        )
    }

    override fun getDescription(): String =
        "Standard implementation for programme partner data exportation " +
                "\n This plugin exports all partner data from Jems. " +
                "Optionally, you can export only data of a specific call via plugin parameter (e.g. 'call_id:2')"
    override fun getKey() =
        "standard-programme-partner-data-export-plugin"

    override fun getName() =
        "Standard programme partner data export"

    override fun getVersion(): String =
        "1.1.2"

    private fun getFileName(
        programmeTitle: String?, exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_partner_${
            exportLanguage.name.lowercase(Locale.getDefault())
        }_${dataLanguage.name.lowercase(Locale.getDefault())}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

    private fun List<ProjectVersionData>.filterAllowedStatuses() =
        this.filter {
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

    private fun getProjectsVersionsToExport(pluginOptionsMap: Map<String, String>): List<ProjectVersionData> {
        return if (pluginOptionsMap.containsKey(optionCallId)) {
            val callId = pluginOptionsMap[optionCallId]?.trim()?.toLong() ?: 0L
            val projectIds = projectDataProvider.getProjectIdsByCallIdIn(setOf(callId)).toSet()
            projectDataProvider.getAllProjectVersionsByProjectIdIn(projectIds).filterAllowedStatuses()
        } else {
            projectDataProvider.getAllProjectVersions().filterAllowedStatuses()
        }
    }
    private fun getOptionsMap(pluginOptions: String): Map<String, String>  {
        return if (pluginOptions.isNotEmpty())
            pluginOptions.split(",")
                .map { it.trim() }.map { it.split(":") }
                .associateBy({ it[0] }, valueTransform = { it[1] })
        else emptyMap()
    }


}
