package io.cloudflight.jems.plugin.standard.budget_export

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId.*
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.*
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.ProjectPartnerBudgetPerPeriodData
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetDetailsRow
import io.cloudflight.jems.plugin.standard.budget_export.models.PartnerInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.PeriodInfo
import io.cloudflight.jems.plugin.standard.common.excel.model.BorderStyle
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getMessagesWithoutArgs
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.isFieldVisible
import io.cloudflight.jems.plugin.standard.common.isInvestmentSectionVisible
import io.cloudflight.jems.plugin.standard.common.toLocale
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.Locale
import kotlin.collections.sumOf

open class BudgetDetailsTableGenerator(
    private val projectData: ProjectData,
    private val callData: CallDetailData,
    private val exportLanguage: SystemLanguageData,
    private val dataLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {

    private val numberOfColumnsBeforePeriods = 19
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
        *(1..Math.ceil((projectData.sectionA?.duration?.toDouble() ?: 0.toDouble()).div((callData.lengthOfPeriod ?: 1)))
            .toInt()).toList().toTypedArray(),
        CLOSURE_PERIOD
    )
    private val exportLocale = exportLanguage.toLocale()

    fun getData(): List<Array<CellData>> =
        mutableListOf<List<CellData>>().also { result ->
            val data = generateBudgetDetailsTableData()
            result.add(getHeaderRow(periodNumbers, exportLocale, messageSource).also { it.last().borderRight(BorderStyle.THIN) })
            result.addAll(getRows(data).also { it.forEach { it.last().borderRight(BorderStyle.THIN) } })
            result.add(getTotalRow(data).also { it.last().borderRight(BorderStyle.THIN) })
        }.map { it.toTypedArray() }

    private fun getHeaderRow(
        periodsNumber: List<Int>, exportLocale: Locale, messageSource: MessageSource
    ): List<CellData> =
        mutableListOf<String>().also {
            it.addAll(
                getPartnerHeaders(
                    isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible,
                    messageSource, exportLocale
                )
            )
            it.add(getMessage("export.cost.category", exportLocale, messageSource))

            if (isDescriptionColumnVisible)
                it.add(getMessage("project.partner.budget.table.description", exportLocale, messageSource))
            if (isCommentColumnVisible)
                it.add(getMessage("project.partner.budget.table.comments", exportLocale, messageSource))
            if (isAwardProcedureColumnVisible)
                it.add(getMessage("project.partner.budget.table.award.procedures", exportLocale, messageSource))
            if (isInvestmentColumnVisible)
                it.add(getMessage("project.partner.budget.table.investment", exportLocale, messageSource))

            it.add(
                getMessage(
                    "project.application.form.section.part.e.lump.sums.column.title",
                    exportLocale,
                    messageSource
                )
            )

            it.addAll(
                getMessagesWithoutArgs(
                    messageSource,
                    exportLocale,
                    "export.flat.rate",
                    "project.partner.budget.unitCosts"
                )
            )
            if (isUnitTypeAndNumberOfUnitColumnsVisible)
                it.addAll(
                    getMessagesWithoutArgs(
                        messageSource, exportLocale,
                        "project.partner.budget.table.unit.type", "project.partner.budget.table.number.of.units",
                    )
                )
            if (isPricePerUnitColumnVisible)
                it.add(getMessage("project.partner.budget.table.price.per.unit", exportLocale, messageSource))

            if (arePeriodColumnsVisible) {
                it.add(
                    getMessage("project.application.form.section.part.e.period.preparation", exportLocale, messageSource),
                )
                it.addAll(
                    periodsNumber.filter { it != PREPARATION_PERIOD && it != CLOSURE_PERIOD }.map {
                        getMessage(
                            "project.application.form.section.part.e.period.label", exportLocale, messageSource
                        ).plus(" ").plus(it)
                    }
                )
                it.add(getMessage("project.application.form.section.part.e.period.closure", exportLocale, messageSource))
            }

            it.add(
                getMessage("project.partner.budget.table.total", exportLocale, messageSource),
            )
        }.map { CellData(it).borderRight(BorderStyle.DOTTED).borderLeft(BorderStyle.DOTTED)
            .borderTop(BorderStyle.THIN).backgroundColor(Color.GREY) }

    private fun getRows(data: List<BudgetDetailsRow>): List<List<CellData>> =
        data.map { row ->
            mutableListOf<Any>().also {
                it.addAll(
                    row.partnerInfo.toStringList(
                        isNameInOriginalLanguageVisible, isNameInEnglishVisible, isCountryAndNutsVisible
                    )
                )
                it.add(row.costCategory)
                if (isDescriptionColumnVisible) it.add((row.description))
                if (isCommentColumnVisible) it.add((row.comments))
                if (isAwardProcedureColumnVisible) it.add((row.awardProcedure))
                if (isInvestmentColumnVisible) it.add((row.investmentNumber))
                it.add(row.lumpSumName)
                it.add(row.flatRate ?: 0)
                it.add(row.unitCost)
                if (isUnitTypeAndNumberOfUnitColumnsVisible) {
                    it.add(row.unitType)
                    it.add(row.numberOfUnits ?: ZERO)
                }
                if (isPricePerUnitColumnVisible) it.add(row.pricePerUnit ?: ZERO)

                if (arePeriodColumnsVisible) it.addAll(row.periodAmounts.map { it.periodAmount ?: ZERO })
                it.add(row.total)
            }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
                .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.DOTTED).borderBottom(BorderStyle.DOTTED) }
        }

    private fun getTotalRow(rows: List<BudgetDetailsRow>): List<CellData> =
        mutableListOf<Any>().also {
            it.add(getMessage("project.partner.budget.table.total", exportLanguage.toLocale(), messageSource))

            val numberOfHiddenColumns = listOf(
                isNameInOriginalLanguageVisible, isNameInEnglishVisible,
                isCountryAndNutsVisible, isCountryAndNutsVisible, isCountryAndNutsVisible,
                isUnitTypeAndNumberOfUnitColumnsVisible, isUnitTypeAndNumberOfUnitColumnsVisible,
                isPricePerUnitColumnVisible, isDescriptionColumnVisible, isCommentColumnVisible,
                isAwardProcedureColumnVisible, isInvestmentColumnVisible
            ).filter { visible -> !visible }.size

            it.addAll((2..(numberOfColumnsBeforePeriods - numberOfHiddenColumns)).map { "" })
            if (arePeriodColumnsVisible) {
                it.addAll(
                    rows.flatMap { it.periodAmounts }.groupBy({ it.periodNumber }, { it.periodAmount })
                        .mapValues { it.value.sumOf { it ?: ZERO } }.values)
            }
            it.add(rows.sumOf { it.total })
        }.map { CellData(if (it is BigDecimal) it.setScale(2) else it).borderRight(BorderStyle.DOTTED)
            .borderLeft(BorderStyle.DOTTED).borderTop(BorderStyle.THIN).borderBottom(BorderStyle.THIN) }

    private fun generateBudgetDetailsTableData(): List<BudgetDetailsRow> {
        return projectData.sectionB.partners.sortedBy { it.sortNumber }.flatMap { partner ->
            val partnerInfo = getPartnerInfo(partner, exportLocale, messageSource)
            val workPackages = projectData.sectionC.projectWorkPackages
            val budgetPerPeriod =
                projectData.sectionD.projectPartnerBudgetPerPeriodData.partnersBudgetPerPeriod.firstOrNull { it.partner.id == partner.id }
            listOf(
                *getStaffCostData(
                    partnerInfo, partner.budget.projectPartnerBudgetCosts.staffCosts, budgetPerPeriod,
                    partner.budget.projectPartnerOptions?.staffCostsFlatRate,
                    partner.budget.projectBudgetCostsCalculationResult.staffCosts
                ),
                *getTravelCostsData(
                    partnerInfo, partner.budget.projectPartnerBudgetCosts.travelCosts, budgetPerPeriod,
                    partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate,
                    partner.budget.projectBudgetCostsCalculationResult.travelCosts
                ),
                *getGeneralCostData(
                    "project.partner.budget.external", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.externalCosts,
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_EXTERNAL_EXPERTISE_INVESTMENT),
                ),
                *getGeneralCostData(
                    "project.partner.budget.equipment", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts,
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_EQUIPMENT_INVESTMENT),
                ),
                *getGeneralCostData(
                    "project.partner.budget.infrastructure", partnerInfo, workPackages,
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts,
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_AWARD_PROCEDURE),
                    shouldBeVisible(PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_INVESTMENT),
                ),
                *getOfficeCostData(
                    partnerInfo, budgetPerPeriod, partner.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                    partner.budget.projectPartnerOptions?.officeAndAdministrationOnDirectCostsFlatRate
                        ?: partner.budget.projectPartnerOptions?.officeAndAdministrationOnStaffCostsFlatRate
                ),
                *getPartnerLumpSumData(partner.id, partnerInfo),
                *getMultiCategoryUnitCostData(partnerInfo, partner.budget.projectPartnerBudgetCosts.unitCosts),
                *getOtherCostData(
                    partnerInfo, partner.budget.projectBudgetCostsCalculationResult.otherCosts,
                    budgetPerPeriod, partner.budget.projectPartnerOptions?.otherCostsOnStaffCostsFlatRate
                )
            )
        }

    }

    private fun getStaffCostData(
        partnerInfo: PartnerInfo, staffCostData: List<BudgetStaffCostEntryData>,
        budgetPerPeriod: ProjectPartnerBudgetPerPeriodData?,
        flatRate: Int?, staffCostTotal: BigDecimal
    ) =
        staffCostData.map { budget ->
            val unitCost = (callData.unitCosts.plus(projectData.sectionE.projectDefinedUnitCosts)).firstOrNull { it.id == budget.unitCostId }
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "project.partner.budget.staff",
                    exportLocale, messageSource
                ),
                unitCost = if (budget.unitCostId == null) "N/A" else unitCost?.name?.getTranslationFor(
                    dataLanguage
                ) ?: "",
                unitType = getValueBasedOnFieldVisibility(
                    PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS,
                    if (budget.unitCostId == null) budget.unitType.getTranslationFor(dataLanguage) else unitCost?.type?.getTranslationFor(
                        dataLanguage
                    ) ?: "", ""
                ),
                numberOfUnits = getValueBasedOnFieldVisibility(PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS, budget.numberOfUnits, null),
                pricePerUnit = getValueBasedOnFieldVisibility(
                    PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT,
                    if (budget.unitCostId == null) budget.pricePerUnit else unitCost?.costPerUnit, null
                ),
                description =getValueBasedOnFieldVisibility(PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION,
                    if (budget.unitCostId == null) budget.description.getTranslationFor(dataLanguage)
                    else unitCost?.description?.getTranslationFor(dataLanguage) ?: "", ""),
                comments = getValueBasedOnFieldVisibility(
                    PARTNER_BUDGET_STAFF_COST_COMMENT, budget.comments.getTranslationFor(dataLanguage), ""
                ),
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.rowSum ?: BigDecimal.ZERO
            )
        }.toMutableList().also {
            if (flatRate != null) {
                it.add(
                    BudgetDetailsRow(
                        partnerInfo = partnerInfo,
                        costCategory = getMessage(
                            "project.partner.budget.staff.costs.flat.rate.header",
                            exportLocale, messageSource
                        ),
                        flatRate = flatRate,
                        periodAmounts = if (arePeriodColumnsVisible)
                            periodNumbers.map { periodNumber ->
                                PeriodInfo(
                                    periodNumber,
                                    budgetPerPeriod?.periodBudgets?.firstOrNull { it.periodNumber == periodNumber }?.budgetPerPeriodDetail?.staffCosts
                                        ?: BigDecimal.ZERO
                                )
                            }
                        else emptyList(),
                        total = staffCostTotal
                    )
                )
            }
        }.toTypedArray()

    private fun getTravelCostsData(
        partnerInfo: PartnerInfo, travelCostsData: List<BudgetTravelAndAccommodationCostEntryData>,
        budgetPerPeriod: ProjectPartnerBudgetPerPeriodData?,
        flatRate: Int?, travelCostTotal: BigDecimal
    ) = travelCostsData.map { budget ->
        val unitCost = (callData.unitCosts.plus(projectData.sectionE.projectDefinedUnitCosts)).firstOrNull { it.id == budget.unitCostId }
        BudgetDetailsRow(
            partnerInfo = partnerInfo,
            costCategory = getMessage(
                "project.partner.budget.travel",
                exportLocale, messageSource
            ),
            unitCost = if (budget.unitCostId == null) "N/A" else unitCost?.name?.getTranslationFor(
                dataLanguage
            ) ?: "",
            unitType = getValueBasedOnFieldVisibility(
                PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS,
                if (budget.unitCostId == null) budget.unitType.getTranslationFor(dataLanguage)
                else unitCost?.type?.getTranslationFor(dataLanguage) ?: "", ""
            ),
            numberOfUnits = getValueBasedOnFieldVisibility(
                PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS, budget.numberOfUnits, null
            ),
            pricePerUnit = getValueBasedOnFieldVisibility(
                PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT,
                if (budget.unitCostId == null) budget.pricePerUnit else unitCost?.costPerUnit, null
            ),
            description = getValueBasedOnFieldVisibility(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION,
                if (budget.unitCostId == null) budget.description.getTranslationFor(dataLanguage)
                else unitCost?.description?.getTranslationFor(dataLanguage) ?: "", ""),
            comments = budget.comments.getTranslationFor(dataLanguage),
            periodAmounts = if (arePeriodColumnsVisible)
                getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
            total = budget.rowSum ?: BigDecimal.ZERO
        )
    }.toMutableList().also {
        if (flatRate != null) {
            it.add(
                BudgetDetailsRow(
                    partnerInfo = partnerInfo,
                    costCategory = getMessage(
                        "project.partner.budget.travel.and.accommodation.flat.rate.header",
                        exportLocale, messageSource
                    ),
                    flatRate = flatRate,
                    periodAmounts = if (arePeriodColumnsVisible)
                        periodNumbers.map { periodNumber -> PeriodInfo(periodNumber,
                            budgetPerPeriod?.periodBudgets?.firstOrNull { it.periodNumber == periodNumber }?.budgetPerPeriodDetail?.travelCosts
                                ?: BigDecimal.ZERO
                        ) } else emptyList(),
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
            val unitCost =
                if (budget.unitCostId != null)
                        (callData.unitCosts.plus(projectData.sectionE.projectDefinedUnitCosts)).firstOrNull { it.id == budget.unitCostId }
                else
                    null
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(typeTranslationKey, exportLocale, messageSource),
                unitCost = if (budget.unitCostId == null) "N/A" else unitCost?.name?.getTranslationFor(dataLanguage)
                    ?: "",
                unitType = if (isUnitTypeAndNumberOfUnitsVisible) {
                    if (budget.unitCostId == null) budget.unitType.getTranslationFor(dataLanguage) else unitCost?.type?.getTranslationFor(
                        dataLanguage
                    ) ?: ""
                } else "",
                numberOfUnits = if (isUnitTypeAndNumberOfUnitsVisible) budget.numberOfUnits else null,
                pricePerUnit = if (isPricePerUnitVisible) {
                    if (budget.unitCostId == null) budget.pricePerUnit else unitCost?.costPerUnit
                } else null,
                description = if (isDescriptionVisible) {
                    if (budget.unitCostId == null) budget.description.getTranslationFor(dataLanguage) else unitCost?.description?.getTranslationFor(
                        dataLanguage
                    ) ?: ""
                } else "",
                comments = budget.comments.getTranslationFor(dataLanguage),
                awardProcedure = if (isAwardProcedureVisible) budget.awardProcedures.getTranslationFor(dataLanguage) else "",
                investmentNumber = if (isInvestmentVisible)
                    getInvestmentNumber(budget.investmentId, workPackages) else "",
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.rowSum ?: BigDecimal.ZERO
            )
        }.toTypedArray()

    private fun getOfficeCostData(
        partnerInfo: PartnerInfo,
        budgetPerPeriod: ProjectPartnerBudgetPerPeriodData?,
        total: BigDecimal?,
        flatRate: Int?
    ): Array<BudgetDetailsRow> =
        if (flatRate != null) {
            arrayOf(BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "export.cost.category.office.costs", exportLocale, messageSource
                ),
                flatRate = flatRate,
                periodAmounts = if (arePeriodColumnsVisible) periodNumbers.map { periodNumber -> PeriodInfo(periodNumber,
                    budgetPerPeriod?.periodBudgets?.firstOrNull { it.periodNumber == periodNumber }?.budgetPerPeriodDetail?.officeAndAdministrationCosts
                        ?: BigDecimal.ZERO) }
                else emptyList(),
                total = total ?: BigDecimal.ZERO
            ))
        } else emptyArray()

    private fun getPartnerLumpSumData(partnerId: Long?, partnerInfo: PartnerInfo) =
        projectData.sectionE.projectLumpSums.filter { it.lumpSumContributions.any { it.partnerId == partnerId && it.amount > BigDecimal.ZERO }}.map { lumpSum ->
            val lumpSumAmount = lumpSum.lumpSumContributions.first { it.partnerId == partnerId }.amount
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "project.partner.budget.lumpSum",
                    exportLocale,
                    messageSource
                ),
                lumpSumName = lumpSum.programmeLumpSum?.name?.getTranslationFor(dataLanguage) ?: "",
                description = getValueBasedOnFieldVisibility(
                        PROJECT_LUMP_SUMS_DESCRIPTION,
                        lumpSum.programmeLumpSum?.description?.getTranslationFor(dataLanguage) ?: "", ""
                    ),
                periodAmounts = if (arePeriodColumnsVisible)
                    periodNumbers.map { periodNumber ->
                        PeriodInfo(
                            periodNumber,
                            if(lumpSum.period == periodNumber) lumpSumAmount  else BigDecimal.ZERO
                        )
                    } else emptyList(),
                total = lumpSumAmount
            )
        }.toTypedArray()

    private fun getMultiCategoryUnitCostData(partnerInfo: PartnerInfo, unitCostsData: List<BudgetUnitCostEntryData>) =
        unitCostsData.map { budget ->
            val callUnitCost = (callData.unitCosts.plus(projectData.sectionE.projectDefinedUnitCosts)).firstOrNull { it.id == budget.unitCostId }
            BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "project.partner.budget.unitcosts",
                    exportLocale, messageSource
                ),
                unitCost = callUnitCost?.name?.getTranslationFor(dataLanguage) ?: "",
                unitType = getValueBasedOnFieldVisibility(PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS,
                    callUnitCost?.type?.getTranslationFor(dataLanguage) ?: "", ""),
                numberOfUnits = getValueBasedOnFieldVisibility(PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS,
                    budget.numberOfUnits, null),
                pricePerUnit = getValueBasedOnFieldVisibility(PARTNER_BUDGET_UNIT_COSTS_PRICE_PER_UNIT,
                    callUnitCost?.costPerUnit, null),
                description = getValueBasedOnFieldVisibility(PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION,
                    callUnitCost?.description?.getTranslationFor(dataLanguage) ?: "", ""),
                periodAmounts = if (arePeriodColumnsVisible)
                    getBudgetPeriodAmounts(periodNumbers, budget.budgetPeriods) else emptyList(),
                total = budget.rowSum ?: BigDecimal.ZERO
            )
        }.toTypedArray()

    private fun getOtherCostData(
        partnerInfo: PartnerInfo, otherCosts: BigDecimal,
        budgetPerPeriod: ProjectPartnerBudgetPerPeriodData?, flatRate: Int?
    ): Array<BudgetDetailsRow> =
        if (flatRate != null)
            arrayOf(BudgetDetailsRow(
                partnerInfo = partnerInfo,
                costCategory = getMessage(
                    "project.partner.budget.other.costs.flat.rate.header",
                    exportLocale, messageSource
                ),
                flatRate = flatRate,
                periodAmounts = if (arePeriodColumnsVisible) periodNumbers.map { periodNumber -> PeriodInfo(periodNumber,
                    budgetPerPeriod?.periodBudgets?.firstOrNull { it.periodNumber == periodNumber }?.budgetPerPeriodDetail?.otherCosts
                        ?: BigDecimal.ZERO) }
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
            PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isPricePerUnitColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT, PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT,
            PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT, PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isDescriptionColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION, PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION,
            PARTNER_BUDGET_EQUIPMENT_DESCRIPTION, PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION,PROJECT_LUMP_SUMS_DESCRIPTION
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun isAwardProcedureColumnVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        listOf(
            PARTNER_BUDGET_EQUIPMENT_AWARD_PROCEDURE, PARTNER_BUDGET_EXTERNAL_EXPERTISE_AWARD_PROCEDURE,
            PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_AWARD_PROCEDURE
        ).any { isFieldVisible(it, lifecycleData, callData) }

    private fun shouldBeVisible(fieldId: ApplicationFormFieldId) =
        isFieldVisible(fieldId, projectData.lifecycleData, callData)

    private fun <T> getValueBasedOnFieldVisibility(
        fieldId: ApplicationFormFieldId, valueWhenFieldIsVisible: T, valueWhenFieldIsHidden: T
    ) =
        if (shouldBeVisible(fieldId)) valueWhenFieldIsVisible else valueWhenFieldIsHidden

}