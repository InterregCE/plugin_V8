package io.cloudflight.jems.plugin.standard.programme_data_export.programme_project_data_export

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundData
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import java.util.Locale

internal fun getHeaderRow(
    maximalPeriodNumbers: List<Int>, programmeFunds: List<ProgrammeFundData>,
    exportLanguage: SystemLanguageData, messageSource: MessageSource
): Array<CellData> {
    val exportLocale = exportLanguage.toLocale()
    return listOf(
        *getCallDataHeaders(exportLocale, messageSource),
        *getProjectDataHeaders(maximalPeriodNumbers, programmeFunds, exportLocale, exportLanguage, messageSource),
        *getProjectAssessmentDataHeaders(exportLocale, messageSource)
    ).toTypedArray()
}

private fun getCallDataHeaders(exportLocale: Locale, messageSource: MessageSource) =
    listOf(
        getMessage("call.table.column.name.id", exportLocale, messageSource).toCallCellData().borderLeft(),
        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "call.detail.field.name",
            "call.detail.field.start",
            "call.detail.field.end.step1",
            "call.detail.field.end",
            "call.detail.field.lengthOfPeriod",
        ).map { it.toCallCellData() }.toTypedArray()
    ).toTypedArray()

private fun getProjectDataHeaders(
    maximalPeriodNumbers: List<Int>, programmeFunds: List<ProgrammeFundData>,
    exportLocale: Locale, exportLanguage: SystemLanguageData, messageSource: MessageSource
) =
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
            "project.application.project.duration.title",
            "project.application.form.field.project.periodcount"
        ).map { it.toProjectCellData() }.toTypedArray(),

        *programmeFunds.flatMap { fund ->
            val abbreviation = fund.abbreviation.getTranslationFor(exportLanguage)
            listOf(
                abbreviation.toProjectCellData(),
                abbreviation.plus(" ")
                    .plus(getMessage("project.partner.percentage", exportLocale, messageSource, arrayOf(fund)))
                    .toProjectCellData(),
                getMessage("export.budget.totals.fund.percentage.of.total", exportLocale, messageSource).plus(" ")
                    .plus(abbreviation)
                    .toProjectCellData()
            )
        }.toTypedArray(),

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
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
            "project.partner.budget.lumpSum"
        ).map { it.toProjectCellData() }.toTypedArray(),

        *with(getMessage("project.partner.budget.table.period", exportLocale, messageSource)) {
            maximalPeriodNumbers.map { periodNumber ->
                when (periodNumber) {
                    PREPARATION_PERIOD -> getMessage("lump.sum.phase.preparation", exportLocale, messageSource)
                    CLOSURE_PERIOD -> getMessage("lump.sum.phase.closure", exportLocale, messageSource)
                    else -> this.plus(" ").plus(periodNumber)
                }.toProjectCellData()
            }.toTypedArray()
        },

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.organization.original.name.label",
            "project.organization.english.name.label"
        ).map { prefixWithLP(it).toProjectCellData() }.toTypedArray(),

        *with(
            prefixWithLP(getMessage("project.partner.main-address.header", exportLocale, messageSource)).plus(" - ")
        ) {
            listOf(
                this.plus(getMessage("project.partner.main-address.country", exportLocale, messageSource))
                    .toProjectCellData(),
                this.plus(getMessage("project.partner.main-address.region2", exportLocale, messageSource))
                    .toProjectCellData(),
                this.plus(getMessage("project.partner.main-address.region3", exportLocale, messageSource))
                    .toProjectCellData()
            ).toTypedArray()
        },

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.table.column.name.step.one.submission.date",
            "project.table.column.name.submission",
            "project.table.column.name.resubmission"
        ).map { it.toProjectCellData() }.toTypedArray(),
    ).toTypedArray()

private fun getProjectAssessmentDataHeaders(exportLocale: Locale, messageSource: MessageSource) =
    listOf(
        *with("1 - ") {
            getMessagesWithoutArgs(
                messageSource, exportLocale,
                "project.assessment.eligibilityDecision.dialog.title",
                "project.assessment.eligibilityDecision.dialog.field.date",
                "project.assessment.fundingDecision.dialog.title",
                "project.assessment.fundingDecision.dialog.field.date",
                "project.assessment.eligibilityCheck.dialog.title",
                "project.assessment.eligibilityCheck.dialog.field.note",
                "project.assessment.qualityCheck.dialog.title",
                "project.assessment.qualityCheck.dialog.field.note"
            ).map { this.plus(it).toAssessmentCellData() }
                .toTypedArray()
        },

        *getMessagesWithoutArgs(
            messageSource, exportLocale,
            "project.assessment.eligibilityDecision.dialog.title",
            "project.assessment.eligibilityDecision.dialog.field.date",
            "project.assessment.fundingDecision.dialog.title",
            "project.assessment.fundingDecision.dialog.field.date",
            "project.assessment.eligibilityCheck.dialog.title",
            "project.assessment.eligibilityCheck.dialog.field.note",
            "project.assessment.qualityCheck.dialog.title",
        ).map { "2 - ".plus(it).toAssessmentCellData() }.toTypedArray(),

        getMessage("project.assessment.qualityCheck.dialog.field.note", exportLocale, messageSource)
            .toAssessmentCellData(),
        getMessage("project.detail.table.contracted", exportLocale, messageSource)
            .toAssessmentCellData().borderRight()
    ).toTypedArray()

    private fun prefixWithLP(text: String) =
        "LP - ".plus(text)
