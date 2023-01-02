package io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistTypeData
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common.ChecklistExport
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
open class NPAA44ChecklistExportDefaultImpl (
    val programmeDataProvider: ProgrammeDataProvider,
    val checklistExport: ChecklistExport
): ProgrammeDataExportPlugin {

    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val exportDateTime = ZonedDateTime.now()
        val fileName = getFileName(programmeData.title, exportDateTime, exportLanguage, dataLanguage)

        val checklistNames = mutableListOf<String>().also {
            it.add("Quality Assessment Summary - Main projects")
            it.add("NPA Eligibility checklist – Main project")
            it.add("NPA Admissibility checklist – Main project")
            it.add("Quality Assessment checklist - Main project")
        }.toList()

        return checklistExport.export(exportLanguage, dataLanguage, fileName, 1, ChecklistTypeData.APPLICATION_FORM_ASSESSMENT, true, false, checklistNames)
    }

    fun getFileName(
        programmeTitle: String?,
        exportDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_NPAA-44_checklists_${exportLanguage.name.lowercase()}_${dataLanguage.name.lowercase()}_" +
                "${exportDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.zip"

    override fun getDescription(): String =
        "NPAA-44 checklist export"

    override fun getKey() =
        "npaa-44-checklist-export-plugin"

    override fun getName() =
        "NPAA-44 checklist export"

    override fun getVersion(): String =
        "1.0.0"
}
