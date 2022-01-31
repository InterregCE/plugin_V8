package io.cloudflight.jems.plugin.standard.programme_data_export

import io.cloudflight.jems.plugin.contract.export.ExportResult
import io.cloudflight.jems.plugin.contract.export.ProgrammeDataExportPlugin
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.standard.common.excel.ExcelService
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Service
open class ProgrammeProjectDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {
    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult =
        with(ExcelData()) {
            val programmeData = programmeDataProvider.getProgrammeData()
            val exportationDateTime = ZonedDateTime.now()
            val exportLanguageLocale = exportLanguage.toLocale()

            this.addSheet("Project data").also { sheet ->
                sheet.addRow(CellData(getFileTitle(programmeData.title, exportationDateTime)))
                sheet.addRow(
                    *getCallDataHeaders(exportLanguageLocale),
                    *getProjectDataHeaders(exportLanguageLocale),
                    *getProjectAssessmentDataHeaders(exportLanguageLocale)
                ).borderTop()
                sheet.addRow(
                    CellData("").backgroundColor(Color.LIGHT_GREEN).borderLeft(),
                    *(2..5).toList().map { CellData("").backgroundColor(Color.LIGHT_GREEN) }.toTypedArray(),
                    *(6..54).toList().map { CellData("").backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),
                    *(55..70).toList().map { CellData("").backgroundColor(Color.LIGHT_ORANGE) }
                        .also { it.last().borderRight() }
                        .toTypedArray(),
                ).borderBottom()
            }

            ExportResult(
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileName = getFileName(programmeData.title, ZonedDateTime.now(), exportLanguage, dataLanguage),
                content = excelService.generateExcel(this)
            )
        }

    fun getCallDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            CellData(getMessage("call.table.column.name.id", exportLanguageLocale, messageSource))
                .backgroundColor(Color.LIGHT_GREEN).borderLeft(),
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "call.detail.field.name",
                "call.detail.field.start",
                "call.detail.field.end.step1",
                "call.detail.field.end",
            ).map { CellData(it).backgroundColor(Color.LIGHT_GREEN) }.toTypedArray()
        ).toTypedArray()

    fun getProjectDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.application.form.field.project.id",
                "project.application.form.field.project.acronym",
                "project.application.form.field.project.title",
                "project.versions.select.label",
                "project.table.column.name.status",
                "project.application.form.field.project.priority",
                "project.application.form.field.project.objective",
                "project.application.project.duration.title"
            ).map { CellData(it).backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),

            *listOf("ERDF").flatMap { fund ->
                listOf(
                    CellData(fund).backgroundColor(Color.LIGHT_BLUE),
                    CellData(
                        fund.plus(" ").plus(
                            getMessage(
                                "project.partner.percentage", exportLanguageLocale, messageSource, arrayOf(fund)
                            )
                        ),
                    ).backgroundColor(Color.LIGHT_BLUE),
                    CellData(
                        getMessage(
                            "export.budget.totals.fund.percentage.of.total", exportLanguageLocale, messageSource
                        ).plus(" ").plus(fund)
                    ).backgroundColor(Color.LIGHT_BLUE)
                )
            }.toTypedArray(),

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.partner.public.contribution",
                "project.partner.auto.public.contribution",
                "project.partner.private.contribution",
                "project.partner.total.contribution",
                "project.partner.total.eligible.budget",
                "export.budget.totals.staff.costs.total",
                "project.partner.budget.staff.costs.flat.rate.header",
                "export.budget.totals.staff.costs.real.cost",
                "export.budget.totals.staff.costs.unit.cost",
                "export.budget.totals.office.and.administration.total",
                "export.budget.totals.office.and.administration.flat.rate",
                "export.budget.totals.travel.total",
                "project.partner.budget.travel.and.accommodation.flat.rate.header",
                "export.budget.totals.travel.real.cost",
                "export.budget.totals.travel.unit.cost",
                "export.budget.totals.external.total",
                "export.budget.totals.external.real.cost",
                "export.budget.totals.external.unit.cost",
                "export.budget.totals.equipment.total",
                "export.budget.totals.equipment.real.cost",
                "export.budget.totals.equipment.unit.cost",
                "export.budget.totals.infrastructure.total",
                "export.budget.totals.infrastructure.real.cost",
                "export.budget.totals.infrastructure.unit.cost",
                "project.partner.budget.other",
                "project.partner.budget.unitcosts",
                "project.partner.budget.lumpSum",
                "lump.sum.phase.preparation"
            ).map { CellData(it).backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),

            *with(getMessage("project.partner.budget.table.period", exportLanguageLocale, messageSource)) {
                listOf("1").map { periodNumber ->
                    CellData(this.plus(" ").plus(periodNumber)).backgroundColor(Color.LIGHT_BLUE)
                }.toTypedArray()
            },

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "lump.sum.phase.closure",
                "project.organization.original.name.label",
                "project.organization.english.name.label"
            ).map { CellData(it).backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),

            *with(
                getMessage(
                    "project.partner.main-address.header", exportLanguageLocale, messageSource
                ).plus(" - ")
            ) {
                listOf(
                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.country", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_BLUE),

                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.region2", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_BLUE),

                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.region3", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_BLUE)
                ).toTypedArray()
            },

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.table.column.name.step.one.submission.date",
                "project.table.column.name.submission",
                "project.table.column.name.resubmission"
            ).map { CellData(it).backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),
        ).toTypedArray()

    fun getProjectAssessmentDataHeaders(exportLanguageLocale: Locale) =
        listOf(

            *with("1 - ") {
                getMessagesWithoutArgs(
                    messageSource, exportLanguageLocale,
                    "project.assessment.eligibilityDecision.dialog.title",
                    "project.assessment.eligibilityDecision.dialog.field.date",
                    "project.assessment.fundingDecision.dialog.title",
                    "project.assessment.fundingDecision.dialog.field.date",
                    "project.assessment.eligibilityCheck.dialog.title",
                    "project.assessment.eligibilityCheck.dialog.field.note",
                    "project.assessment.qualityCheck.dialog.title",
                    "project.assessment.qualityCheck.dialog.field.note"
                ).map { CellData(this.plus(it)).backgroundColor(Color.LIGHT_ORANGE) }
                    .toTypedArray()
            },

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.assessment.eligibilityDecision.dialog.title",
                "project.assessment.eligibilityDecision.dialog.field.date",
                "project.assessment.fundingDecision.dialog.title",
                "project.assessment.fundingDecision.dialog.field.date",
                "project.assessment.eligibilityCheck.dialog.title",
                "project.assessment.eligibilityCheck.dialog.field.note",
                "project.assessment.qualityCheck.dialog.title",
            ).map { CellData("2 - ".plus(it)).backgroundColor(Color.LIGHT_ORANGE) }.toTypedArray(),
            CellData(
                getMessage(
                    "project.assessment.qualityCheck.dialog.field.note", exportLanguageLocale, messageSource
                )
            ).backgroundColor(Color.LIGHT_ORANGE).borderRight()
        ).toTypedArray()

    fun getFileName(
        programmeTitle: String?,
        exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ): String =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_project_${exportLanguage.name.toLowerCase()}_${dataLanguage.name.toLowerCase()}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

    fun getFileTitle(programmeTitle: String?, exportationDateTime: ZonedDateTime) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle} - project data - ${
            exportationDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm:ss"))
        }"

    override fun getDescription(): String =
        "Standard implementation for programme project data exportation"

    override fun getKey() =
        "standard-programme-project-data-export-plugin"

    override fun getName() =
        "Standard programme project data export"

    override fun getVersion(): String =
        "1.0.0"
}