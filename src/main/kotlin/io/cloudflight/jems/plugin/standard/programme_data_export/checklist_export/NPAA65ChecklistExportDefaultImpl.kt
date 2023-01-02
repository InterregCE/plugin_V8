package io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistStatusData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistTypeData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectChecklistDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.common.excel.model.BorderStyle
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.zip.ZipItem
import io.cloudflight.jems.plugin.standard.common.zip.ZipService
import io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common.Checklist
import io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common.toReportModel
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
open class NPAA65ChecklistExportDefaultImpl (
    val programmeDataProvider: ProgrammeDataProvider,
    val zipService: ZipService,
    val checklistDataProvider: ProjectChecklistDataProvider,
    val projectDataProvider: ProjectDataProvider,
    val excelService: ExcelService
): ProgrammeDataExportPlugin {

    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult {
        val programmeData = programmeDataProvider.getProgrammeData()
        val exportDateTime = ZonedDateTime.now()
        val fileName = getFileName(programmeData.title, exportDateTime, exportLanguage, dataLanguage)

        val checklistNames = mutableListOf<String>().also {
            it.add("NPA RCP checklist – Preparatory Projects – Step 1")
            it.add("RCP checklist – Preparatory Projects – Step 1")
            it.add("NPA RCP checklist Preparatory Projects Step 1")
            it.add("RCP checklist Preparatory Projects Step 1")
        }.toList()

        return export(exportLanguage, dataLanguage, fileName, null, ChecklistTypeData.APPLICATION_FORM_ASSESSMENT, true, false, checklistNames)
    }

    fun getFileName(
        programmeTitle: String?,
        exportDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_NPAA-65_RCP_checklists_" +
                "${exportDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.zip"

    override fun getDescription(): String =
        "NPAA-65 RCP checklist export"

    override fun getKey() =
        "npaa-65-checklist-export-plugin"

    override fun getName() =
        "NPAA-65 RCP checklist export"

    override fun getVersion(): String =
        "1.0.0"

    fun export(
        exportLanguage: SystemLanguageData,
        dataLanguage: SystemLanguageData,
        zipFileName: String,
        callId: Long?,
        checklistTypeData: ChecklistTypeData,
        isFinished: Boolean,
        isConsolidated: Boolean,
        checklistNames: List<String>): ExportResult {
        val exportDateTime = ZonedDateTime.now()

        var projects = getProjectsToExport(projectDataProvider.getProjectVersions(callId))

        var zipItems = projects
            .map{ project ->
                val checklists = checklistDataProvider.getChecklistsForProject(project.projectId, checklistTypeData)
                    .filter { !isFinished || it.status == ChecklistStatusData.FINISHED }
                    .filter { !isConsolidated || it.consolidated }
                    .filter { checklistNames.isEmpty() || checklistNames.any { c -> clean(c) == clean(it.name) } }

                Pair(project, checklists)
            }
            .filter { it.second.isNotEmpty() }
            .map{ project ->
                var projectDetails = projectDataProvider.getProjectDataForProjectId(project.first.projectId)

                var checklistDetails = project.second.map { checklistSummary ->
                    checklistDataProvider.getChecklistDetail(checklistSummary.id).toReportModel()
                }

                var excel = excelService.generateExcel(createExcelDocument(projectDetails, checklistDetails))

                ZipItem("${projectDetails.sectionA!!.customIdentifier}_CL${checklistDetails.first().id}_${checklistDetails.first().name}.xlsx", excel)
            }

        val zipBytes = zipService.createZipFile(zipItems)

        return ExportResult(
            contentType = "application/zip",
            fileName = zipFileName,
            content = zipBytes,
            startTime = exportDateTime,
            endTime = ZonedDateTime.now()
        )
    }

    private fun getProjectsToExport(projectVersions: List<ProjectVersionData>) =
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

    private fun createExcelDocument(projectDetails: ProjectData, checklistDetails: List<Checklist>): ExcelData {
        return ExcelData().also {
            it.addSheet("FULL RCP Checklist").also { sheet ->
                sheet.addRow(CellData("RCP Cumulated Checklists").toLabelCell())
                sheet.addRow(CellData("").toNoBorderCell())
                sheet.addRow(CellData("Project ID").toLabelCell(), CellData(projectDetails.sectionA!!.customIdentifier))
                sheet.addRow(CellData("Project Acronym").toLabelCell(), CellData(projectDetails.sectionA!!.acronym))
                sheet.addRow(CellData("").toNoBorderCell())

                val countryChecklistPairs = checklistDetails.map { cl ->
                    val countryQuestion = cl.questions
                        .filter { q -> q.textInputMetadata?.question != null }
                        .firstOrNull { q ->
                            q.textInputMetadata!!.question!!.trim() == "RCP country"
                        }

                    if (countryQuestion?.answerMetadata != null)
                        Pair((countryQuestion.answerMetadata.answer ?: countryQuestion.answerMetadata.explanation)!!.trim(), cl)
                    else
                        Pair("Undefined", cl)
                }

                sheet.addRow(mutableListOf<CellData>().also { list ->
                    list.add(CellData("Question/Country").toLabelCell())
                    list.addAll(countryChecklistPairs.map { p -> CellData(p.first).toLabelCell() })
                })

                var isAlternateRow = false

                countryChecklistPairs.first().second.questions
                    .filter { question -> question.optionsToggleMetadata != null }
                    .sortedBy { question -> question.position }
                    .forEach { question ->

                        isAlternateRow = !isAlternateRow

                        sheet.addRow(mutableListOf<CellData>().also { list ->
                            val questionCell = CellData(question.optionsToggleMetadata!!.question)
                                .toTableRowCell(isAlternateRow)
                                .toNoBottomBorderCell()

                            val answerCells = countryChecklistPairs.map { p ->
                                val answer = p.second.questions.first { q -> q.id == question.id }.answerMetadata!!

                                CellData(answer.answer ?: "")
                                    .toTableRowCell(isAlternateRow)
                                    .toNoBottomBorderCell()
                            }

                            list.add(questionCell)
                            list.addAll(answerCells)
                        })

                        sheet.addRow(mutableListOf<CellData>().also { list ->
                            val emptyCell = CellData(null)
                                .toTableRowCell(isAlternateRow)
                                .toNoTopBorderCell()

                            val justificationCells = countryChecklistPairs.map { p ->
                                val answer = p.second.questions.first { q -> q.id == question.id }.answerMetadata!!

                                CellData(answer.justification ?: "")
                                    .toTableRowCell(isAlternateRow)
                                    .toNoTopBorderCell()
                            }

                            list.add(emptyCell)
                            list.addAll(justificationCells)
                        })
                    }
            }
        }
    }

    fun clean(value: String) =
        value.trim()
            .replace("'","\"")
            .replace("“","\"")
            .replace("”","\"")
            .replace("„","\"")
            .replace("–","-")
            .replace(" ","")
            .lowercase()

    private fun CellData.toTableRowCell(isAlternateRow: Boolean) =
        backgroundColor(if (isAlternateRow) Color.LIGHT_BLUE else Color.WHITE)

    private fun CellData.toNoBorderCell() =
        border(BorderStyle.NONE)

    private fun CellData.toNoTopBorderCell() =
        borderTop(BorderStyle.NONE)

    private fun CellData.toNoBottomBorderCell() =
        borderBottom(BorderStyle.NONE)

    private fun CellData.toLabelCell() =
        backgroundColor(Color(230,230,230))
}
