package io.cloudflight.jems.plugin.standard.checklist

import com.google.gson.Gson
import io.cloudflight.jems.plugin.contract.export.checklist.ChecklistExportPlugin
import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.standard.checklist.service.ChecklistExportService
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
open class ChecklistExportDefaultImpl(
    val checklistExportService: ChecklistExportService,
) : ChecklistExportPlugin {

    override fun export(projectId: Long, checklistId: Long, exportLanguage: SystemLanguageData): ExportResult {
        val result = checklistExportService.exportPdfByChecklistId(projectId = projectId, checklistId = checklistId, exportLanguage = exportLanguage)

        return ExportResult(
            contentType = MediaType.APPLICATION_PDF_VALUE,
            fileName = result.first,
            content = result.second
        )
    }

    override fun getDescription(): String =
        "Standard implementation for checklist exportation"

    override fun getKey() =
        "standard-checklist-export-plugin"

    override fun getName() =
        "Standard checklist export"

    override fun getVersion(): String =
        "1.0.1"
}
