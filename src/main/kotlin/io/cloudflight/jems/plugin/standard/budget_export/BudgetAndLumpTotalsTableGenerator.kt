package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.*
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetAndLumpSumTotalsRow
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.percentageTo
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.common.percentageDown
import io.cloudflight.jems.plugin.standard.common.isFieldVisible
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.collections.sumOf

open class BudgetAndLumpTotalsTableGenerator(
    private val projectData: ProjectData,
    private val callData: CallDetailData,
    private val exportLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {
    private val callSelectedFunds = callData.funds.filter { it.selected }.sortedBy { it.id }
    private val exportLocale = exportLanguage.toLocale()

    private val isNameInOriginalLanguageVisible =
        shouldBeVisible(ApplicationFormFieldId.PARTNER_ORIGINAL_NAME_OF_ORGANISATION)
    private val isNameInEnglishVisible = shouldBeVisible(ApplicationFormFieldId.PARTNER_ENGLISH_NAME_OF_ORGANISATION)
    private val isCountryAndNutsVisible = shouldBeVisible(ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_COUNTRY_AND_NUTS)

    fun getData() =
        mutableListOf<List<String?>>().also {
            val data = generateBudgetAndLumpSumTotalsTableData()
            it.add(getHeaderRow())
            it.addAll(getRows(data))
            it.add(getTotalRow(data))
        }

    private fun getHeaderRow(): List<String> =
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
                    getMessage(
                        "jems.standard.budget.export.budget.totals.fund.rate.percentage",
                        exportLocale, messageSource, arrayOf(fundAbbreviation)
                    ),
                )
            })
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource, exportLocale,
                    "jems.standard.budget.export.budget.totals.public.contribution",
                    "jems.standard.budget.export.budget.totals.automatic.public.contribution",
                    "jems.standard.budget.export.budget.totals.private.contribution",
                    "jems.standard.budget.export.budget.totals.total.partner.contribution",
                    "jems.standard.budget.export.budget.totals.total.eligible.budget",
                    "jems.standard.budget.export.budget.totals.percent.of.total.eligible.budget",
                    "jems.standard.budget.export.budget.totals.staff.costs.total",
                    "jems.standard.budget.export.budget.totals.staff.costs.flat.rate",
                    "jems.standard.budget.export.budget.totals.staff.costs.real.cost",
                    "jems.standard.budget.export.budget.totals.staff.costs.unit.cost",
                    "jems.standard.budget.export.budget.totals.office.and.administration.total",
                    "jems.standard.budget.export.budget.totals.office.and.administration.flat.rate",
                    "jems.standard.budget.export.budget.totals.travel.total",
                    "jems.standard.budget.export.budget.totals.travel.flat.rate",
                    "jems.standard.budget.export.budget.totals.travel.real.cost",
                    "jems.standard.budget.export.budget.totals.travel.unit.cost",
                    "jems.standard.budget.export.budget.totals.external.total",
                    "jems.standard.budget.export.budget.totals.external.real.cost",
                    "jems.standard.budget.export.budget.totals.external.unit.cost",
                    "jems.standard.budget.export.budget.totals.equipment.total",
                    "jems.standard.budget.export.budget.totals.equipment.real.cost",
                    "jems.standard.budget.export.budget.totals.equipment.unit.cost",
                    "jems.standard.budget.export.budget.totals.infrastructure.total",
                    "jems.standard.budget.export.budget.totals.infrastructure.real.cost",
                    "jems.standard.budget.export.budget.totals.infrastructure.unit.cost",
                    "jems.standard.budget.export.budget.totals.other.costs",
                    "jems.standard.budget.export.budget.totals.unit.costs.covering.several.cost.categories",
                    "jems.standard.budget.export.budget.totals.lump.sums.covering.several.cost.categories",
                )
            )

        }

    private fun getRows(data: List<BudgetAndLumpSumTotalsRow>): List<List<String?>> =
        data.map { row ->
            mutableListOf<String>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible
                    )
                )
                it.addAll(row.fundInfoList.flatMap { it.toStringList() })
                it.addAll(
                    listOf(
                        row.publicContribution.toString(),
                        row.automaticPublicContribution.toString(),
                        row.privateContribution.toString(),
                        row.getTotalPartnerContribution().toString(),
                        row.totalEligibleBudget.toString(),
                        row.totalEligibleBudgetPercentage.toString()
                    )
                )
                it.addAll(row.staffCostTotals.toStringList())
                it.add(row.officeCostTotals.toString())
                it.add(row.officeCostFlatRatesTotals.toString())
                it.addAll(row.travelCostTotals.toStringList())
                it.addAll(row.externalCostTotals.toStringList())
                it.addAll(row.equipmentCostTotals.toStringList())
                it.addAll(row.infrastructureCostTotals.toStringList())
                it.add(row.otherCosts.toString())
                it.add(row.unitCostsCoveringMultipleCostCategories.toString())
                it.add(row.lumpSumsCoveringMultipleCostCategories.toString())
            }
        }

    private fun getTotalRow(rows: List<BudgetAndLumpSumTotalsRow>): List<String> =
        mutableListOf<String>().also {
            it.add(getMessage("jems.standard.budget.export.total", exportLocale, messageSource))
            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible
            ).filter { visible -> !visible }.size
            it.addAll((1..(6 - numberOfHiddenColumns)).map { "" })
            it.addAll(
                listOf(
                    *(1..callSelectedFunds.size).flatMap { listOf("", "") }.toTypedArray(),
                    rows.sumOf { it.publicContribution },
                    rows.sumOf { it.automaticPublicContribution },
                    rows.sumOf { it.privateContribution },
                    rows.sumOf { it.getTotalPartnerContribution() },
                    rows.sumOf { it.totalEligibleBudget },
                    "100",
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
                ).map { it.toString() }
            )

        }

    private fun generateBudgetAndLumpSumTotalsTableData(): List<BudgetAndLumpSumTotalsRow> {
        val partnersTotalEligibleBudget = projectData.sectionB.partners.sumOf { it.budget.projectBudgetCostsCalculationResult.totalCosts }
        return projectData.sectionB.partners.sortedBy { it.sortNumber }.map { partner ->
            BudgetAndLumpSumTotalsRow(
                partnerInfo = getPartnerInfo(partner),
                fundInfoList = getFoundInfoList(partner.budget),
                publicContribution = partner.budget.projectPartnerCoFinancing.partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Public }
                    .sumOf { it.amount ?: BigDecimal.ZERO },
                automaticPublicContribution = partner.budget.projectPartnerCoFinancing.partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.AutomaticPublic }
                    .sumOf { it.amount ?: BigDecimal.ZERO },
                privateContribution = partner.budget.projectPartnerCoFinancing.partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Private }
                    .sumOf { it.amount ?: BigDecimal.ZERO },
                totalEligibleBudget = partner.budget.projectBudgetCostsCalculationResult.totalCosts,
                totalEligibleBudgetPercentage = partner.budget.projectBudgetCostsCalculationResult.totalCosts.percentageTo(partnersTotalEligibleBudget, RoundingMode.HALF_UP),
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
                unitCostsCoveringMultipleCostCategories = partner.budget.projectPartnerBudgetCosts.unitCosts.flatMap { it.budgetPeriods }
                    .sumOf { it.amount },
                lumpSumsCoveringMultipleCostCategories = projectData.sectionE.projectLumpSums.flatMap { it.lumpSumContributions }
                    .filter { it.partnerId == partner.id }.sumOf { it.amount }
            )
        }
        }

    private fun getGeneralBudgetCostTotalsFor(budgetCosts: List<BudgetGeneralCostEntryData>): GeneralBudgetTotalCostInfo =
        GeneralBudgetTotalCostInfo(
            realCostTotal = budgetCosts.filter { it.unitCostId == null }.flatMap { it.budgetPeriods }
                .sumOf { it.amount },
            unitCostTotal = budgetCosts.filter { it.unitCostId != null }.flatMap { it.budgetPeriods }
                .sumOf { it.amount }
        )

    private fun getStaffCostTotals(
        staffCosts: List<BudgetStaffCostEntryData>, staffCostTotal: BigDecimal, flatRate: Int?
    ): BudgetTotalCostInfo =
        BudgetTotalCostInfo(
            total = staffCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else staffCostTotal,
            realCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId == null }
                .flatMap { it.budgetPeriods }.sumOf { it.amount } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId != null }
                .flatMap { it.budgetPeriods }.sumOf { it.amount } else BigDecimal.ZERO
        )

    private fun getTravelCostTotals(
        travelCosts: List<BudgetTravelAndAccommodationCostEntryData>, travelCostTotal: BigDecimal, flatRate: Int?
    ): BudgetTotalCostInfo =
        BudgetTotalCostInfo(
            total = travelCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else travelCostTotal,
            realCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId == null }
                .flatMap { it.budgetPeriods }.sumOf { it.amount } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId != null }
                .flatMap { it.budgetPeriods }.sumOf { it.amount } else BigDecimal.ZERO
        )

    private fun getFoundInfoList(budget: PartnerBudgetData) =
        callSelectedFunds.map { fund ->
            val percentage =
                budget.projectPartnerCoFinancing.finances.firstOrNull { fund.type == it.fund?.type }?.percentage
            FundInfo(
                fundAmount = if (percentage == null) BigDecimal.ZERO
                else budget.projectBudgetCostsCalculationResult.totalCosts.percentageDown(percentage),
                fundPercentage = percentage ?: BigDecimal.ZERO
            )
        }

    private fun shouldBeVisible(fieldId: ApplicationFormFieldId) =
        isFieldVisible(fieldId, projectData.lifecycleData, callData)
}