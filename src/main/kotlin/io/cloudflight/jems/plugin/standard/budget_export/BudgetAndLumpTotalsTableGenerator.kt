package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetGeneralCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetStaffCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetTravelAndAccommodationCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.PartnerBudgetPerFundData
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetAndLumpSumTotalsRow
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.common.excel.model.BorderStyle
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.isFieldVisible
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import java.math.BigDecimal
import kotlin.collections.sumOf

open class BudgetAndLumpTotalsTableGenerator(
    private val projectData: ProjectData,
    private val callData: CallDetailData,
    private val exportLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {
    private val numberOfColumnsBeforeFunds = 8

    private val callSelectedFunds = callData.funds.filter { it.selected }.sortedBy { it.id }
    private val exportLocale = exportLanguage.toLocale()

    private val isNameInOriginalLanguageVisible =
        shouldBeVisible(ApplicationFormFieldId.PARTNER_ORIGINAL_NAME_OF_ORGANISATION)
    private val isNameInEnglishVisible = shouldBeVisible(ApplicationFormFieldId.PARTNER_ENGLISH_NAME_OF_ORGANISATION)
    private val isCountryAndNutsVisible = shouldBeVisible(ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_COUNTRY_AND_NUTS)
    private val arePeriodsVisible = shouldBeVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS)

    fun getDataFirst(): List<Array<CellData>> =
        mutableListOf<List<CellData>>().also {
            val data = generateBudgetAndLumpSumTotalsTableData()
            it.add(getHeaderRowFirst().also { it.last().borderRight(BorderStyle.THIN) })
            it.addAll(getRowsFirst(data).also { it.forEach { it.last().borderRight(BorderStyle.THIN) } })
            it.add(getTotalRowFirst(data).also { it.last().borderRight(BorderStyle.THIN) })
        }.map { it.toTypedArray() }

    fun getDataSecond(): List<Array<CellData>> =
        mutableListOf<List<CellData>>().also {
            val data = generateBudgetAndLumpSumTotalsTableData()
            it.add(getHeaderRowSecond().also { it.last().borderRight(BorderStyle.THIN) })
            it.addAll(getRowsSecond(data).also { it.forEach { it.last().borderRight(BorderStyle.THIN) } })
            it.add(getTotalRowSecond(data).also { it.last().borderRight(BorderStyle.THIN) })
        }.map { it.toTypedArray() }

    fun getDataThird(): List<Array<CellData>> =
        mutableListOf<List<CellData>>().also {
            val data = generateBudgetAndLumpSumTotalsTableData()
            it.add(getHeaderRowThird().also { it.last().borderRight(BorderStyle.THIN) })
            it.addAll(getRowsThird(data).also { it.forEach { it.last().borderRight(BorderStyle.THIN) } })
            it.add(getTotalRowThird(data).also { it.last().borderRight(BorderStyle.THIN) })
        }.map { it.toTypedArray() }

    private fun getHeaderRowFirst(): List<CellData> =
        mutableListOf<String>().also {
            it.addAll(
                getPartnerHeaders(
                    isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                    isCountryAndNutsVisible, messageSource, exportLocale
                )
            )
            it.addAll(callSelectedFunds.flatMap {
                val fundAbbreviation = it.abbreviation.getTranslationFor(exportLanguage)
                listOf(
                    fundAbbreviation,
                    fundAbbreviation.plus(" ").plus(getMessage(
                        "project.partner.percentage",
                        exportLocale, messageSource, arrayOf(fundAbbreviation)
                    )),
                    getMessage(
                        "export.budget.totals.fund.percentage.of.total",
                        exportLocale, messageSource
                    ).plus(" ").plus(fundAbbreviation),
                )
            })
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
                    "project.partner.public.contribution",
                    "project.partner.auto.public.contribution",
                    "project.partner.private.contribution",
                    "project.partner.total.contribution",
                    "project.partner.total.eligible.budget",
                    "project.partner.percent.total.budget",
                )
            )
        }.map { CellData(it).borderRight(BorderStyle.DOTTED).borderLeft(BorderStyle.DOTTED)
            .borderTop(BorderStyle.THIN).backgroundColor(Color.GREY) }

    private fun getHeaderRowSecond(): List<CellData> =
        mutableListOf<String>().also {
            it.addAll(
                getPartnerHeaders(
                    isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                    isCountryAndNutsVisible, messageSource, exportLocale, leaveRegions = true
                )
            )
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
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
                    "project.application.form.section.part.e.lump.sums.label",
                    "project.partner.total.eligible.budget",
                )
            )
        }.map { CellData(it).borderRight(BorderStyle.DOTTED).borderLeft(BorderStyle.DOTTED)
            .borderTop(BorderStyle.THIN).backgroundColor(Color.GREY) }

    private fun getHeaderRowThird(): List<CellData> =
        mutableListOf<String>().also {
            it.addAll(
                getPartnerHeaders(
                    isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                    isCountryAndNutsVisible, messageSource, exportLocale, leaveRegions = true
                )
            )
            if (arePeriodsVisible) {
                it.add(
                    getMessage(
                        "project.application.form.section.part.e.period.preparation", exportLocale, messageSource
                    )
                )
                it.addAll(
                    projectData.sectionA?.periods?.map {
                        getMessage(
                            "project.partner.budget.table.period",
                            exportLocale,
                            messageSource
                        ).plus(" ").plus(it.number)
                    } ?: emptyList()
                )
                it.add(
                    getMessage("project.application.form.section.part.e.period.closure", exportLocale, messageSource)
                )
            }
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
                    "project.partner.total.eligible.budget",
                )
            )
        }.map { CellData(it).borderRight(BorderStyle.DOTTED).borderLeft(BorderStyle.DOTTED)
            .borderTop(BorderStyle.THIN).backgroundColor(Color.GREY) }

    private fun getRowsFirst(data: List<BudgetAndLumpSumTotalsRow>): List<List<CellData>> =
        data.map { row ->
            mutableListOf<Any>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible
                    )
                )
                it.addAll(row.fundInfoList.flatMap { it.toStringList() })
                it.addAll(
                    listOf(
                        row.publicContribution,
                        row.automaticPublicContribution,
                        row.privateContribution,
                        row.getTotalPartnerContribution(),
                        row.totalEligibleBudget,
                        row.totalEligibleBudgetPercentage
                    )
                )
            }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
                .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.DOTTED).borderBottom(BorderStyle.DOTTED) }
        }

    private fun getRowsSecond(data: List<BudgetAndLumpSumTotalsRow>): List<List<CellData>> =
        data.map { row ->
            mutableListOf<Any>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible, leaveRegions = true
                    )
                )
                it.addAll(row.staffCostTotals.toStringList())
                it.add(row.officeCostTotals)
                it.add(row.officeCostFlatRatesTotals)
                it.addAll(row.travelCostTotals.toStringList())
                it.addAll(row.externalCostTotals.toStringList())
                it.addAll(row.equipmentCostTotals.toStringList())
                it.addAll(row.infrastructureCostTotals.toStringList())
                it.add(row.otherCosts)
                it.add(row.unitCostsCoveringMultipleCostCategories)
                it.add(row.lumpSumsCoveringMultipleCostCategories)
                it.add(row.totalEligibleBudget)
            }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
                .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.DOTTED).borderBottom(BorderStyle.DOTTED) }
        }

    private fun getRowsThird(data: List<BudgetAndLumpSumTotalsRow>): List<List<CellData>> =
        data.map { row ->
            mutableListOf<Any>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible, leaveRegions = true
                    )
                )
                if (arePeriodsVisible)
                    it.addAll(row.partnerBudgetPerPeriod.map { budgetPerPeriod -> budgetPerPeriod.totalBudgetPerPeriod })
                it.add(row.totalEligibleBudget)
            }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
                .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.DOTTED).borderBottom(BorderStyle.DOTTED) }
        }

    private fun getTotalRowFirst(rows: List<BudgetAndLumpSumTotalsRow>): List<CellData> =
        mutableListOf<Any>().also {
            it.add(getMessage("project.partner.budget.table.total", exportLocale, messageSource))
            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible
            ).filter { visible -> !visible }.size
            it.addAll((2..(numberOfColumnsBeforeFunds - numberOfHiddenColumns)).map { "" })
            it.addAll(
                mutableListOf(
                    *(1..callSelectedFunds.size).flatMap { listOf("", "", "") }.toTypedArray(),
                    rows.sumOf { it.publicContribution },
                    rows.sumOf { it.automaticPublicContribution },
                    rows.sumOf { it.privateContribution },
                    rows.sumOf { it.getTotalPartnerContribution() },
                    rows.sumOf { it.totalEligibleBudget },
                ).map { it }
            )
            it.add(BigDecimal.valueOf(100))
        }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
            .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.THIN).borderBottom(BorderStyle.THIN) }

    private fun getTotalRowSecond(rows: List<BudgetAndLumpSumTotalsRow>): List<CellData> =
        mutableListOf<Any>().also {
            it.add(getMessage("project.partner.budget.table.total", exportLocale, messageSource))
            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible
            ).filter { visible -> !visible }.size + 2
            it.addAll((2..(numberOfColumnsBeforeFunds - numberOfHiddenColumns)).map { "" })
            it.addAll(
                mutableListOf(
                    rows.sumOf { it.staffCostTotals.total },
                    rows.sumOf { it.staffCostTotals.flatRateTotal },
                    rows.sumOf { it.staffCostTotals.realCostTotal },
                    rows.sumOf { it.staffCostTotals.unitCostTotal },
                    rows.sumOf { it.officeCostTotals },
                    rows.sumOf { it.officeCostFlatRatesTotals },
                    rows.sumOf { it.travelCostTotals.total },
                    rows.sumOf { it.travelCostTotals.flatRateTotal },
                    rows.sumOf { it.travelCostTotals.realCostTotal },
                    rows.sumOf { it.travelCostTotals.unitCostTotal },
                    rows.sumOf { it.externalCostTotals.getTotal() },
                    rows.sumOf { it.externalCostTotals.realCostTotal },
                    rows.sumOf { it.externalCostTotals.unitCostTotal },
                    rows.sumOf { it.equipmentCostTotals.getTotal() },
                    rows.sumOf { it.equipmentCostTotals.realCostTotal },
                    rows.sumOf { it.equipmentCostTotals.unitCostTotal },
                    rows.sumOf { it.infrastructureCostTotals.getTotal() },
                    rows.sumOf { it.infrastructureCostTotals.realCostTotal },
                    rows.sumOf { it.infrastructureCostTotals.unitCostTotal },
                    rows.sumOf { it.otherCosts },
                    rows.sumOf { it.unitCostsCoveringMultipleCostCategories },
                    rows.sumOf { it.lumpSumsCoveringMultipleCostCategories },
                    rows.sumOf { it.totalEligibleBudget },
                )
            )
        }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
            .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.THIN).borderBottom(BorderStyle.THIN) }

    private fun getTotalRowThird(rows: List<BudgetAndLumpSumTotalsRow>): List<CellData> =
        mutableListOf<Any>().also {
            it.add(getMessage("project.partner.budget.table.total", exportLocale, messageSource))
            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible
            ).filter { visible -> !visible }.size + 2
            it.addAll((2..(numberOfColumnsBeforeFunds - numberOfHiddenColumns)).map { "" })
            if (arePeriodsVisible)
                it.addAll(projectData.sectionD.projectPartnerBudgetPerPeriodData.totals.dropLast(1))
            it.add(rows.sumOf { it.totalEligibleBudget })
        }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
            .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.THIN).borderBottom(BorderStyle.THIN) }

    private fun generateBudgetAndLumpSumTotalsTableData(): List<BudgetAndLumpSumTotalsRow> =
        projectData.sectionB.partners.sortedBy { it.sortNumber }.map { partner ->
            val projectPartnerBudgetPerFundData =
                projectData.sectionD.projectPartnerBudgetPerFundData.first { it.partner?.id == partner.id }
            val projectPartnerBudgetPerPeriodData = projectData.sectionD.projectPartnerBudgetPerPeriodData
            BudgetAndLumpSumTotalsRow(
                partnerInfo = getPartnerInfo(partner, exportLocale, messageSource),
                fundInfoList = getFoundInfoList(projectPartnerBudgetPerFundData.budgetPerFund),
                publicContribution = projectPartnerBudgetPerFundData.publicContribution ?: BigDecimal.ZERO,
                automaticPublicContribution = projectPartnerBudgetPerFundData.autoPublicContribution ?: BigDecimal.ZERO,
                privateContribution = projectPartnerBudgetPerFundData.privateContribution ?: BigDecimal.ZERO,
                totalEligibleBudget = projectPartnerBudgetPerFundData.totalEligibleBudget ?: BigDecimal.ZERO,
                totalEligibleBudgetPercentage = projectPartnerBudgetPerFundData.percentageOfTotalEligibleBudget
                    ?: BigDecimal.ZERO,
                staffCostTotals = getStaffCostTotals(
                    partner.budget.projectPartnerBudgetCosts.staffCosts,
                    partner.budget.projectBudgetCostsCalculationResult.staffCosts,
                    partner.budget.projectPartnerOptions?.staffCostsFlatRate
                ),
                officeCostTotals = partner.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                officeCostFlatRatesTotals = partner.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                travelCostTotals = getTravelCostTotals(
                    partner.budget.projectPartnerBudgetCosts.travelCosts,
                    partner.budget.projectBudgetCostsCalculationResult.travelCosts,
                    partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate
                ),
                externalCostTotals = getGeneralBudgetCostTotalsFor(partner.budget.projectPartnerBudgetCosts.externalCosts),
                equipmentCostTotals = getGeneralBudgetCostTotalsFor(partner.budget.projectPartnerBudgetCosts.equipmentCosts),
                infrastructureCostTotals = getGeneralBudgetCostTotalsFor(partner.budget.projectPartnerBudgetCosts.infrastructureCosts),
                otherCosts = partner.budget.projectBudgetCostsCalculationResult.otherCosts,
                unitCostsCoveringMultipleCostCategories = partner.budget.projectPartnerBudgetCosts.unitCosts
                    .sumOf { it.rowSum ?: BigDecimal.ZERO },
                lumpSumsCoveringMultipleCostCategories = projectData.sectionE.projectLumpSums.flatMap { it.lumpSumContributions }
                    .filter { it.partnerId == partner.id }.sumOf { it.amount },
                partnerBudgetPerPeriod = projectPartnerBudgetPerPeriodData.partnersBudgetPerPeriod.firstOrNull { it.partner.id == partner.id }?.periodBudgets ?: emptyList()
            )
        }

    private fun getGeneralBudgetCostTotalsFor(budgetCosts: List<BudgetGeneralCostEntryData>): GeneralBudgetTotalCostInfo =
        GeneralBudgetTotalCostInfo(
            realCostTotal = budgetCosts.filter { it.unitCostId == null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO },
            unitCostTotal = budgetCosts.filter { it.unitCostId != null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO }
        )

    private fun getStaffCostTotals(
        staffCosts: List<BudgetStaffCostEntryData>, staffCostTotal: BigDecimal, flatRate: Int?
    ): BudgetTotalCostInfo =
        BudgetTotalCostInfo(
            total = staffCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else staffCostTotal,
            realCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId == null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId != null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
        )

    private fun getTravelCostTotals(
        travelCosts: List<BudgetTravelAndAccommodationCostEntryData>, travelCostTotal: BigDecimal, flatRate: Int?
    ): BudgetTotalCostInfo =
        BudgetTotalCostInfo(
            total = travelCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else travelCostTotal,
            realCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId == null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId != null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
        )

    private fun getFoundInfoList(partnerBudgetPerFundData: Set<PartnerBudgetPerFundData>) =
        callSelectedFunds.map { fund ->
            partnerBudgetPerFundData.first { it.fund == fund }.let {
                FundInfo(
                    fundAmount = it.value,
                    fundPercentage = it.percentage,
                    percentageOfTotal = it.percentageOfTotal
                )
            }
        }

    fun shouldBeVisible(fieldId: ApplicationFormFieldId) =
        isFieldVisible(fieldId, projectData.lifecycleData, callData)
}