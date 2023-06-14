package io.cloudflight.jems.plugin.standard.checklist.service

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData

interface ChecklistExportService {

    fun exportPdfByChecklistId(projectId: Long, checklistId: Long, exportLanguage: SystemLanguageData): Pair<String, ByteArray>
}