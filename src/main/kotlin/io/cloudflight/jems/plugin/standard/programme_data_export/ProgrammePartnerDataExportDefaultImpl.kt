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
open class ProgrammePartnerDataExportDefaultImpl(
    private val excelService: ExcelService,
    private val programmeDataProvider: ProgrammeDataProvider,
    private val messageSource: MessageSource
) : ProgrammeDataExportPlugin {
    override fun export(exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData): ExportResult =
        with(ExcelData()) {
            val programmeData = programmeDataProvider.getProgrammeData()
            val exportationDateTime = ZonedDateTime.now()
            val exportLanguageLocale = exportLanguage.toLocale()
            this.addSheet("Partner Data").also { sheet ->
                sheet.addRow(CellData(getFileTitle(programmeData.title, exportationDateTime)))
                sheet.addRow(
                    *getCallDataHeaders(exportLanguageLocale),
                    *getProjectDataHeaders(exportLanguageLocale),
                    *getProjectBudgetDataHeaders(exportLanguageLocale),
                    *getPartnerDataHeaders(exportLanguageLocale)
                ).borderTop()
                sheet.addRow(
                    CellData("").backgroundColor(Color.LIGHT_GREEN).borderLeft().borderLeft(),
                    CellData("").backgroundColor(Color.LIGHT_GREEN),
                    *(3..20).toList().map { CellData("").backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),
                    *(21..51).toList().map { CellData("").backgroundColor(Color.LIGHT_ORANGE) }.toTypedArray(),
                    *(49..79).toList().map { CellData("").backgroundColor(Color.LIGHT_RED) }
                        .also { it.last().borderRight() }
                        .toTypedArray(),
                ).borderBottom()
            }

            ExportResult(
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileName = getFileName(programmeData.title, exportationDateTime, exportLanguage, dataLanguage),
                content = excelService.generateExcel(this)
            )
        }

    fun getCallDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            CellData(
                getMessage("call.table.column.name.id", exportLanguageLocale, messageSource)
            ).backgroundColor(Color.LIGHT_GREEN).borderLeft(),
            CellData(getMessage("call.detail.field.name", exportLanguageLocale, messageSource))
                .backgroundColor(Color.LIGHT_GREEN),
        ).toTypedArray()

    fun getProjectDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.application.form.field.project.id",
                "project.application.form.field.project.acronym",
                "project.application.form.field.project.title",
                "project.versions.select.label",
                "project.application.form.field.project.priority",
                "project.application.form.field.project.objective",
                "project.table.column.name.status"
            ).map { CellData(it).backgroundColor(Color.LIGHT_BLUE) }.toTypedArray(),

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.application.form.partner.table.number",
                "project.application.form.partner.table.name",
                "project.partner.name.label",
                "project.organization.original.name.label",
                "project.organization.english.name.label",
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
        ).toTypedArray()

    fun getProjectBudgetDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.partner.public.contribution",
                "project.partner.auto.public.contribution",
                "project.partner.private.contribution",
                "project.partner.total.contribution",
                "project.partner.total.eligible.budget",
                "project.partner.percent.total.budget",
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
                "lump.sum.phase.preparation",
            ).map { CellData(it).backgroundColor(Color.LIGHT_ORANGE) }.toTypedArray(),

            *with(getMessage("project.partner.budget.table.period", exportLanguageLocale, messageSource)) {
                listOf("1").map { periodNumber ->
                    CellData(this.plus(" ").plus(periodNumber)).backgroundColor(Color.LIGHT_ORANGE)
                }.toTypedArray()
            },

            CellData(getMessage("lump.sum.phase.closure", exportLanguageLocale, messageSource)).backgroundColor(
                Color.LIGHT_ORANGE
            )
        ).toTypedArray()

    fun getPartnerDataHeaders(exportLanguageLocale: Locale) =
        listOf(
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.organization.department.label",
                "project.partner.type",
                "project.partner.subType",
                "project.partner.legal.status",
                "project.partner.nace",
                "project.partner.vat",
                "project.partner.recoverVat.intro.text",
                "project.partner.other.identifier.number",
                "project.partner.other.identifier.description",
                "project.partner.pic",
                "project.partner.state.aid.check.result",
                "project.partner.state.aid.scheme",
            ).map { CellData(it).backgroundColor(Color.LIGHT_RED) }.toTypedArray(),

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
                    ).backgroundColor(Color.LIGHT_RED),

                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.region3", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_RED),

                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.street", exportLanguageLocale, messageSource
                            )
                        ).plus(" - ")
                            .plus(
                                getMessage(
                                    "project.partner.main-address.housenumber",
                                    exportLanguageLocale,
                                    messageSource
                                )
                            )
                    ).backgroundColor(Color.LIGHT_RED),
                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.postalcode", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_RED),
                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.city", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_RED),
                    CellData(
                        this.plus(
                            getMessage(
                                "project.partner.main-address.homepage", exportLanguageLocale, messageSource
                            )
                        )
                    ).backgroundColor(Color.LIGHT_RED),
                ).toTypedArray()
            },

            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.partner.secondary-address.country",
                "project.partner.secondary-address.region3",
            ).map { CellData(it).backgroundColor(Color.LIGHT_RED) }.toTypedArray(),
            CellData(
                getMessage(
                    "project.partner.secondary-address.street", exportLanguageLocale, messageSource
                ).plus(" - ")
                    .plus(
                        getMessage(
                            "project.partner.secondary-address.housenumber", exportLanguageLocale, messageSource
                        )
                    )
            ).backgroundColor(Color.LIGHT_RED),
            *getMessagesWithoutArgs(
                messageSource, exportLanguageLocale,
                "project.partner.secondary-address.postalcode",
                "project.partner.secondary-address.city",
                "project.partner.representative.title",
                "project.partner.representative.first.name",
                "project.partner.representative.last.name",
                "project.partner.contact.title",
                "project.partner.contact.first.name",
                "project.partner.contact.last.name",
                "project.partner.contact.email",
            ).map { CellData(it).backgroundColor(Color.LIGHT_RED) }.toTypedArray(),
            CellData(
                getMessage(
                    "project.partner.contact.telephone",
                    exportLanguageLocale,
                    messageSource
                )
            ).backgroundColor(Color.LIGHT_RED).borderRight()
        ).toTypedArray()

    fun getFileName(
        programmeTitle: String?, exportationDateTime: ZonedDateTime,
        exportLanguage: SystemLanguageData, dataLanguage: SystemLanguageData
    ) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle}_partner_${
            exportLanguage.name.toLowerCase(Locale.getDefault())
        }_${dataLanguage.name.toLowerCase(Locale.getDefault())}_" +
                "${exportationDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.xlsx"

    fun getFileTitle(programmeTitle: String?, exportationDateTime: ZonedDateTime) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle} - partner data - ${
            exportationDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm:ss"))
        }"

    override fun getDescription(): String =
        "Standard implementation for programme partner data exportation"

    override fun getKey() =
        "standard-programme-partner-data-export-plugin"

    override fun getName() =
        "Standard programme partner data export"

    override fun getVersion(): String =
        "1.0.0"
}