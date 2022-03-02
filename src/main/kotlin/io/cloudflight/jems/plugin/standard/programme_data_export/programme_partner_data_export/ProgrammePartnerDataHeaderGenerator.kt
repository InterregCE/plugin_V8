package io.cloudflight.jems.plugin.standard.programme_data_export.programme_partner_data_export

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import org.springframework.context.MessageSource
import java.util.Locale

internal fun getHeaderRow(
    maximalPeriodNumbers: List<Int>, programmeInfoData: ProgrammeInfoData,
    exportLocale: Locale, messageSource: MessageSource, exportLanguage: SystemLanguageData
): Array<CellData> =
    listOf(
        *getCallDataHeaders(exportLocale, messageSource),
        *getProjectDataHeaders(exportLocale, messageSource),
        *getPartnerBudgetDataHeaders(
            maximalPeriodNumbers,
            programmeInfoData,
            exportLocale,
            messageSource,
            exportLanguage
        ),
        *getPartnerDataHeaders(exportLocale, messageSource)
    ).toTypedArray()

private fun getCallDataHeaders(exportLocale: Locale, messageSource: MessageSource) =
    listOf(
        getMessage("call.table.column.name.id", exportLocale, messageSource).toCallCellData().borderLeft(),
        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "call.detail.field.name",
        ).map { it.toCallCellData() }.toTypedArray()
    ).toTypedArray()

private fun getProjectDataHeaders(exportLocale: Locale, messageSource: MessageSource) =
    listOf(
        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.application.form.field.project.id",
            "project.application.form.field.project.acronym",
            "project.application.form.field.project.title",
            "project.versions.select.label",
            "project.table.column.name.status",
            "project.application.form.field.project.priority",
            "project.application.form.field.project.objective",
        ).map { it.toProjectCellData() }.toTypedArray()

    ).toTypedArray()

fun getPartnerBudgetDataHeaders(
    maximalPeriods: List<Int>, programmeInfoData: ProgrammeInfoData,
    exportLocale: Locale, messageSource: MessageSource, exportLanguage: SystemLanguageData
) =
    listOf(
        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.application.form.partner.table.number",
            "project.application.form.partner.table.status",
            "project.application.form.partner.table.role",
            "project.partner.name.label",
            "project.organization.original.name.label",
            "project.organization.english.name.label"
        ).map { it.toPartnerBudgetData() }
            .toTypedArray(),

        *with(
            getMessage(
                "project.partner.main-address.header", exportLocale, messageSource
            ).plus(" - ")
        ) {
            listOf(
                this.plus(getMessage("project.partner.main-address.country", exportLocale, messageSource))
                    .toPartnerBudgetData(),
                this.plus(getMessage("project.partner.main-address.region2", exportLocale, messageSource))
                    .toPartnerBudgetData(),
                this.plus(getMessage("project.partner.main-address.region3", exportLocale, messageSource))
                    .toPartnerBudgetData()
            ).toTypedArray()
        },

        *programmeInfoData.funds.flatMap { fund ->
            val fundAbbreviation = fund.abbreviation.getTranslationFor(exportLanguage)
            listOf(
                fundAbbreviation.toPartnerBudgetData(),
                fundAbbreviation.plus(" ")
                    .plus(
                        getMessage(
                            "project.partner.percentage",
                            exportLocale,
                            messageSource,
                            arrayOf(fundAbbreviation)
                        )
                    )
                    .toPartnerBudgetData(),
                getMessage("export.budget.totals.fund.percentage.of.total", exportLocale, messageSource)
                    .plus(" ").plus(fundAbbreviation)
                    .toPartnerBudgetData()
            )
        }.toTypedArray(),

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
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
            "project.partner.budget.lumpSum"
        ).map { it.toPartnerBudgetData() }
            .toTypedArray(),

        *with(getMessage("project.partner.budget.table.period", exportLocale, messageSource)) {
            maximalPeriods.map { periodNumber ->
                when (periodNumber) {
                    PREPARATION_PERIOD -> getMessage("lump.sum.phase.preparation", exportLocale, messageSource)
                    CLOSURE_PERIOD -> getMessage("lump.sum.phase.closure", exportLocale, messageSource)
                    else -> this.plus(" ").plus(periodNumber)
                }.toPartnerBudgetData()
            }.toTypedArray()
        },

        ).toTypedArray()

private fun getPartnerDataHeaders(exportLocale: Locale, messageSource: MessageSource) =
    listOf(
        *getMessagesWithoutArgs(
            messageSource, exportLocale,
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
        ).map { it.toPartnerCellData() }
            .toTypedArray(),

        *with(
            getMessage("project.partner.main-address.header", exportLocale, messageSource).plus(" - ")
        ) {
            listOf(
                this.plus(getMessage("project.partner.main-address.country", exportLocale, messageSource))
                    .toPartnerCellData(),
                this.plus(
                    getMessage("project.partner.main-address.region3", exportLocale, messageSource)
                )
                    .toPartnerCellData(),
                this.plus(
                    getMessage("project.partner.main-address.street", exportLocale, messageSource)
                ).plus(" - ")
                    .plus(
                        getMessage(
                            "project.partner.main-address.housenumber",
                            exportLocale,
                            messageSource
                        )
                    )
                    .toPartnerCellData(),
                this.plus(
                    getMessage(
                        "project.partner.main-address.postalcode", exportLocale, messageSource
                    )
                ).toPartnerCellData(),
                this.plus(
                    getMessage(
                        "project.partner.main-address.city", exportLocale, messageSource
                    )
                ).toPartnerCellData(),
                this.plus(
                    getMessage(
                        "project.partner.main-address.homepage", exportLocale, messageSource
                    )
                ).toPartnerCellData()
            ).toTypedArray()
        },

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.partner.secondary-address.country",
            "project.partner.secondary-address.region3"
        ).map { it.toPartnerCellData() }
            .toTypedArray(),

        *listOf(
            getMessage("project.partner.secondary-address.street", exportLocale, messageSource)
                .plus(" - ")
                .plus(
                    getMessage(
                        "project.partner.secondary-address.housenumber",
                        exportLocale,
                        messageSource
                    )
                ).toPartnerCellData()
        ).toTypedArray(),

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.partner.secondary-address.postalcode",
            "project.partner.secondary-address.city",
            "project.partner.representative.title",
            "project.partner.representative.first.name",
            "project.partner.representative.last.name",
            "project.partner.contact.title",
            "project.partner.contact.first.name",
            "project.partner.contact.last.name",
            "project.partner.contact.email"
        )
            .map {
                it.toPartnerCellData()
            }
            .toTypedArray(),

        getMessage("project.partner.contact.telephone", exportLocale, messageSource)
            .toPartnerCellData().borderRight()

    ).toTypedArray()
