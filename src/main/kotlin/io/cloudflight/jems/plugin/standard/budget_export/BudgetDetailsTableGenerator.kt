package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId.*
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.*
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetDetailsRow
import io.cloudflight.jems.plugin.standard.budget_export.models.PartnerInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.PeriodInfo
import io.cloudflight.jems.plugin.standard.common.*
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.Locale
import kotlin.collections.sumOf

open class BudgetDetailsTableGenerator(
    private val projectData: ProjectData,
    private val callData: CallDetailData,
    private val exportLanguage: SystemLanguageData,
    private val dataLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {

    private val isUnitTypeAndNumberOfUnitColumnsVisible =
        isUnitTypeAndNumberOfUnitColumnsVisible(projectData.lifecycleData, callData)
    private val isPricePerUnitColumnVisible = isPricePerUnitColumnVisible(projectData.lifecycleData, callData)
    private val isDescriptionColumnVisible = isDescriptionColumnVisible(projectData.lifecycleData, callData)
    private val isAwardProcedureColumnVisible = isAwardProcedureColumnVisible(projectData.lifecycleData, callData)
    private val isInvestmentColumnVisible = isInvestmentColumnVisible(projectData.lifecycleData, callData)
    private val isCommentColumnVisible = isFieldVisible(
        PARTNER_BUDGET_STAFF_COST_COMMENT, projectData.lifecycleData, callData
    )
    private val arePeriodColumnsVisible =
        isFieldVisible(PARTNER_BUDGET_PERIODS, projectData.lifecycleData, callData)

    private val isNameInOriginalLanguageVisible = shouldBeVisible(PARTNER_ORIGINAL_NAME_OF_ORGANISATION)
    private val isNameInEnglishVisible = shouldBeVisible(PARTNER_ENGLISH_NAME_OF_ORGANISATION)
    private val isCountryAndNutsVisible = shouldBeVisible(PARTNER_MAIN_ADDRESS_COUNTRY_AND_NUTS)

    private val periodNumbers = listOf(
        PREPARATION_PERIOD,
        *(1.. Math.ceil((projectData.sectionA?.duration?.toDouble() ?: 0.toDouble()).div((callData.lengthOfPeriod ?: 1))).toInt()).toList().toTypedArray(),
        CLOSURE_PERIOD
    )
    private val exportLocale = exportLanguage.toLocale()

    fun getData(): List<List<String?>> =
        mutableListOf<List<String>>().also { result ->
            val data = generateBudgetDetailsTableData()
            result.add(getHeaderRow(periodNumbers, exportLocale, messageSource))
            result.addAll(getRows(data))
            result.add(getTotalRow(data))
        }

    private fun getHeaderRow(
        periodsNumber: List<Int>, exportLocale: Locale, messageSource: MessageSource
    ): List<String> =
        mutableListOf<String>().also {
            it.addAll(
                getPartnerHeaders(
                    isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible,
                    messageSource, exportLocale
                )
            )
            it.addAll(
                getMessagesWithoutArgs(
                    messageSource,
                    exportLocale,
                    "jems.standard.budget.export.cost.category", "jems.standard.budget.export.flat.rate",
                    "jems.standard.budget.export.unit.cost"
                )
            )
            if (isUnitTypeAndNumberOfUnitColumnsVisible)
                it.addAll(
                    getMessagesWithoutArgs(
                        messageSource, exportLocale,
                        "jems.standard.budget.export.unit.type", "jems.standard.budget.export.number.of.units",
                    )
                )
            if (isPricePerUnitColumnVisible)
                it.add(getMessage("jems.standard.budget.export.price.per.unit", exportLocale, messageSource))
            if (isDescriptionColumnVisible)
                it.add(getMessage("jems.standard.budget.export.description", exportLocale, messageSource))
            if (isCommentColumnVisible)
                it.add(getMessage("jems.standard.budget.export.comment", exportLocale, messageSource))
            if (isAwardProcedureColumnVisible)
                it.add(getMessage("jems.standard.budget.export.award.procedure", exportLocale, messageSource))
            if (isInvestmentColumnVisible)
                it.add(getMessage("jems.standard.budget.export.investement", exportLocale, messageSource))
            if (arePeriodColumnsVisible) {
                it.add(
                    getMessage("jems.standard.budget.export.period.preparation", exportLocale, messageSource),
                )
                it.addAll(
                    periodsNumber.filter { it != PREPARATION_PERIOD && it != CLOSURE_PERIOD }.map {
                        getMessage(
                            "jems.standard.budget.export.period.number", exportLocale, messageSource, arrayOf(it)
                        )
                    }
                )
                it.add(getMessage("jems.standard.budget.export.period.closure", exportLocale, messageSource))
            }

            it.add(
                getMessage("jems.standard.budget.export.total", exportLocale, messageSource),
            )
        }

    private fun getRows(data: List<BudgetDetailsRow>): List<List<String>> =
        data.map { row ->
            mutableListOf<String>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible
                    )
                )
                it.add(row.costCategory)
                it.add((row.flatRate ?: "").toString())
                it.add(row.unitCost)
                if (isUnitTypeAndNumberOfUnitColumnsVisible)
                    it.addAll(listOf(row.unitType, (row.numberOfUnits ?: "").toString()))
                if (isPricePerUnitColumnVisible) it.add((row.pricePerUnit ?: "").toString())
                if (isDescriptionColumnVisible) it.add((row.description))
                if (isCommentColumnVisible) it.add((row.comments))
                if (isAwardProcedureColumnVisible) it.add((row.awardProcedure))
                if (isInvestmentColumnVisible) it.add((row.investmentNumber))
                if (arePeriodColumnsVisible) it.addAll(row.periodAmounts.map { it.toStringList() })
                it.add(row.total.toString())
            }
        }

    private fun getTotalRow(rows: List<BudgetDetailsRow>): List<String> =
        mutableListOf<String>().also {
            it.add(getMessage("jems.standard.budget.export.total", exportLanguage.toLocale(), messageSource))

            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible,
                isUnitTypeAndNumberOfUnitColumnsVisible, isUnitTypeAndNumberOfUnitColumnsVisible,
                isPricePerUnitColumnVisible, isDescriptionColumnVisible, isCommentColumnVisible,
                isAwardProcedureColumnVisible, isInvestmentColumnVisible
            ).filter { visible -> !visible }.size

            it.addAll((1..(16 - numberOfHiddenColumns)).map { "" })
            if (arePeriodColumnsVisible) {
                it.addAll(
                    rows.flatMap { it.periodAmounts }.groupBy({ it.periodNumber }, { it.periodAmount })
                        .mapValues { it.value.sumOf { it } }.values.map { it.toString() })
            }
            it.add(rows.sumOf { it.total }.toString())
        }

    private fun generateBudgetDetailsTableData(): List<BudgetDetailsRow> {
        return projectData.sectionB.partners.sortedBy { it.sortNumber }.flatMap { partner ->
            val partnerInfo = getPartnerInfo(partner)
            val workPackages = projectData.sectionC.projectWorkPackages
            listOf(
                *getStaffCostData(
                    partnerInfo, partner.budget.projectPartnerBudgetCosts.staffCosts,
                    partner.budget.projectPartnerOptions?.staffCostsFlatRate,
                    partner.budget.projectBudgetCostsCalculationResult.staffCosts
                ),
                *getTravelCostsData(
                    partnerInfo, partner.budget.projectPartnerBudgetCosts.travelCosts,
                    partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate,
                    partner.budget.projectBudgetCostsCalculationResult.travelCosts
                ),
                *getGeneralCostData(
                    "jems.standard.budget.export.cost.category.external.costs", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.externalCosts,
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_INVESTMENT),
                ),
                *getGeneralCostData(
                    "jems.standard.budget.export.cost.category.equipment.costs", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts,
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_INVESTMENT),
                ),
                *getGeneralCostData(
                    "jems.standard.budget.export.cost.category.infrastructure.costs", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts,
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_INVESTMENT),
                ),
                *getOfficeCostData(
                    partnerInfo, partner.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                    partner.budget.projectPartnerOptions?.officeAndAdministrationOnDirectCostsFlatRate
                        ?: partner.budget.projectPartnerOptions?.officeAndAdministrationOnStaffCostsFlatRate
                ),
                getPartnerLumpSumData(partner.id, partnerInfo),
                *getMultiCategoryUnitCostData(partnerInfo, partner.budget.projectPartnerBudgetCosts.unitCosts),
                *getOtherCostData(
                    partnerInfo, partner.budget.projectBudgetCostsCalculationResult.otherCosts,
                    partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate
                )
            )
        }

    }

    private fun getStaffCostData(
        partnerInfo: PartnerInfo, staffCostData: List<BudgetStaffCostEntryData>,
        flatRate: Int?, staffCostTotal: BigDecimal
    ) =
        staffCostData.map { budget ->
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "jems.standard.budget.export.cost.category.staff.costs",
                    exportLocale, messageSource
                ),
                unitCost = if (budget.unitCostId == null) "N/A" else callData.unitCosts.firstOrNull { it.id == budget.unitCostId }?.name?.getTranslationFor(
                    dataLanguage
                ) ?: "",
                unitType = if (shouldBeVisible(PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS))
                    budget.unitType.getTranslationFor(dataLanguage) else "",
                numberOfUnits = if (shouldBeVisible(PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS)
                ) budget.numberOfUnits else null,
                pricePerUnit = if (shouldBeVisible(PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT)
                ) budget.pricePerUnit else null,
                description = if (shouldBeVisible(PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION))
                    budget.description.getTranslationFor(dataLanguage) else "",
                comments = if (shouldBeVisible(PARTNER_BUDGET_STAFF_COST_COMMENT))
                    budget.comments.getTranslationFor(dataLanguage) else "",
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.budgetPeriods.sumOf { it.amount }
            )
        }.toMutableList().also {
            if (flatRate != null) {
                it.add(
                    BudgetDetailsRow(
                        partnerInfo = partnerInfo,
                        costCategory = getMessage(
                            "jems.standard.budget.export.cost.category.staff.costs.flat.rate",
                            exportLocale, messageSource
                        ),
                        flatRate = flatRate,
                        periodAmounts = if (arePeriodColumnsVisible)
                            periodNumbers.map { PeriodInfo(it, BigDecimal.ZERO) } else emptyList(),
                        total = staffCostTotal
                    )
                )
            }
        }.toTypedArray()

    private fun getTravelCostsData(
        partnerInfo: PartnerInfo, travelCostsData: List<BudgetTravelAndAccommodationCostEntryData>,
        flatRate: Int?, travelCostTotal: BigDecimal
    ) = travelCostsData.map { budget ->
        BudgetDetailsRow(
            partnerInfo = partnerInfo,
            costCategory = getMessage(
                "jems.standard.budget.export.cost.category.travel.costs",
                exportLocale, messageSource
            ),
            unitCost = if (budget.unitCostId == null) "N/A" else callData.unitCosts.firstOrNull { it.id == budget.unitCostId }?.name?.getTranslationFor(
                dataLanguage
            ) ?: "",
            unitType = if (shouldBeVisible(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS))
                budget.unitType.getTranslationFor(dataLanguage) else "",
            numberOfUnits = if (shouldBeVisible(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS))
                budget.numberOfUnits else null,
            pricePerUnit = if (shouldBeVisible(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT))
                budget.pricePerUnit else null,
            description = if (shouldBeVisible(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION))
                budget.description.getTranslationFor(dataLanguage) else "",
            periodAmounts = if (arePeriodColumnsVisible)
                getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
            total = budget.budgetPeriods.sumOf { it.amount }
        )
    }.toMutableList().also {
        if (flatRate != null) {
            it.add(
                BudgetDetailsRow(
                    partnerInfo = partnerInfo,
                    costCategory = getMessage(
                        "jems.standard.budget.export.cost.category.travel.costs.flat.rate",
                        exportLocale, messageSource
                    ),
                    flatRate = flatRate,
                    periodAmounts = if (arePeriodColumnsVisible)
                        periodNumbers.map { PeriodInfo(it, BigDecimal.ZERO) } else emptyList(),
                    total = travelCostTotal
                )
            )
        }
    }.toTypedArray()

    private fun getGeneralCostData(
        typeTranslationKey: String, partnerInfo: PartnerInfo, workPackages: List<ProjectWorkPackageData>,
        generalCostData: List<BudgetGeneralCostEntryData>,
        isUnitTypeAndNumberOfUnitsVisible: Boolean, isPricePerUnitVisible: Boolean,
        isDescriptionVisible: Boolean, isAwardProcedureVisible: Boolean, isInvestmentVisible: Boolean
    ): Array<BudgetDetailsRow> =
        generalCostData.map { budget ->
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(typeTranslationKey, exportLocale, messageSource),
                unitCost = if (budget.unitCostId == null) "N/A" else callData.unitCosts.firstOrNull { it.id == budget.unitCostId }?.name?.getTranslationFor(
                    dataLanguage
                ) ?: "",
                unitType = if (isUnitTypeAndNumberOfUnitsVisible) budget.unitType.getTranslationFor(dataLanguage) else "",
                numberOfUnits = if (isUnitTypeAndNumberOfUnitsVisible) budget.numberOfUnits else null,
                pricePerUnit = if (isPricePerUnitVisible) budget.pricePerUnit else null,
                description = if (isDescriptionVisible) budget.description.getTranslationFor(dataLanguage) else "",
                awardProcedure = if (isAwardProcedureVisible) budget.awardProcedures.getTranslationFor(dataLanguage) else "",
                investmentNumber = if (isInvestmentVisible)
                    getInvestmentNumber(budget.investmentId, workPackages) else "",
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.budgetPeriods.sumOf { it.amount }
            )
        }.toTypedArray()

    private fun getOfficeCostData(
        partnerInfo: PartnerInfo,
        total: BigDecimal?,
        flatRate: Int?
    ): Array<BudgetDetailsRow> =
        if (flatRate != null) {
            arrayOf(BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "jems.standard.budget.export.cost.category.office.costs", exportLocale, messageSource
                ),
                flatRate = flatRate,
                periodAmounts = if (arePeriodColumnsVisible) periodNumbers.map { PeriodInfo(it, BigDecimal.ZERO) }
                else emptyList(),
                total = total ?: BigDecimal.ZERO
            ))
        } else emptyArray()

    private fun getPartnerLumpSumData(partnerId: Long?, partnerInfo: PartnerInfo): BudgetDetailsRow =
        BudgetDetailsRow(
            partnerInfo = partnerInfo,
            costCategory = getMessage(
                "jems.standard.budget.export.cost.category.lump.sum.costs",
                exportLocale,
                messageSource
            ),
            periodAmounts = if (arePeriodColumnsVisible)
                periodNumbers.map { periodNumber ->
                    PeriodInfo(
                        periodNumber,
                        projectData.sectionE.projectLumpSums.filter { it.period == periodNumber }
                            .flatMap { it.lumpSumContributions }
                            .filter { it.partnerId == partnerId }
                            .sumOf { it.amount }
                    )
                } else emptyList(),
            total = projectData.sectionE.projectLumpSums.flatMap { it.lumpSumContributions }
                .filter { it.partnerId == partnerId }
                .sumOf { it.amount }
        )

    private fun getMultiCategoryUnitCostData(partnerInfo: PartnerInfo, unitCostsData: List<BudgetUnitCostEntryData>) =
        unitCostsData.map { budget ->
            val callUnitCost = callData.unitCosts.firstOrNull { it.id == budget.unitCostId }
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "jems.standard.budget.export.cost.category.unit.costs",
                    exportLocale, messageSource
                ),
                unitCost = callUnitCost?.name?.getTranslationFor(dataLanguage) ?: "",
                unitType = if (shouldBeVisible(PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS))
                    callUnitCost?.type?.getTranslationFor(dataLanguage) ?: "" else "",
                numberOfUnits = if (shouldBeVisible(PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS))
                    budget.numberOfUnits else null,
                pricePerUnit = if (shouldBeVisible(PARTNER_BUDGET_UNIT_COSTS_PRICE_PER_UNIT))
                    callUnitCost?.costPerUnit else null,
                description = if (shouldBeVisible(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION))
                    callUnitCost?.description?.getTranslationFor(dataLanguage) ?: "" else "",
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.budgetPeriods.sumOf { it.amount }
            )
        }.toTypedArray()

    private fun getOtherCostData(
        partnerInfo: PartnerInfo, otherCosts: BigDecimal, flatRate: Int?
    ): Array<BudgetDetailsRow> =
        if (flatRate != null)
            arrayOf(BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "jems.standard.budget.export.cost.category.other.costs.flat.rate",
                    exportLocale, messageSource
                ),
                flatRate = flatRate,
                periodAmounts = if (arePeriodColumnsVisible) periodNumbers.map { PeriodInfo(it, BigDecimal.ZERO) }
                else emptyList(),
                total = otherCosts
            )) else arrayOf()

    private fun getInvestmentNumber(investmentId: Long?, workPackages: List<ProjectWorkPackageData>): String =
        with(workPackages.firstOrNull { workPackage ->
            workPackage.investments.firstOrNull { it.id == investmentId } != null
        }) {
            if (this != null) "I${this.workPackageNumber}.${this.investments.firstOrNull { it.id == investmentId }?.investmentNumber}" else ""
        }

    private fun getBudgetPeriodAmounts(periodNumbers: List<Int>, budgetPeriodData: MutableSet<BudgetPeriodData>) =
        periodNumbers.map { periodNumber ->
            PeriodInfo(
                periodNumber, budgetPeriodData.firstOrNull { it.number == periodNumber }?.amount ?: BigDecimal.ZERO
            )
        }

    private fun isInvestmentColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isInvestmentSectionVisible(lifecycleData, callData) &&
                listOf(
                    PARTNER_BUDGET_EQUIPMENT_INVESTMENT, PARTNER_BUDGET_EXTERNAL_EXPERTISE_INVESTMENT,
                    PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_INVESTMENT,
                ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isUnitTypeAndNumberOfUnitColumnsVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS,
            PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS,
            PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS,
            PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS,
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isPricePerUnitColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT, PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT,
            PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT, PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT,
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isDescriptionColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION, PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION,
            PARTNER_BUDGET_EQUIPMENT_DESCRIPTION, PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION,
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isAwardProcedureColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_EQUIPMENT_AWARD_PROCEDURE, PARTNER_BUDGET_EXTERNAL_EXPERTISE_AWARD_PROCEDURE,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_AWARD_PROCEDURE
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun shouldBeVisible(fieldId: ApplicationFormFieldId) =
        isFieldVisible(fieldId, projectData.lifecycleData, callData)
}