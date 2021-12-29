package io.cloudflight.jems.plugin.standard.budget_export.models

import io.cloudflight.jems.plugin.contract.models.project.sectionD.ProjectPeriodBudgetData
import java.math.BigDecimal


open class BudgetAndLumpSumTotalsRow(
    val partnerInfo: PartnerInfo,
    val fundInfoList: List<FundInfo>,
    val publicContribution: BigDecimal,
    val automaticPublicContribution: BigDecimal,
    val privateContribution: BigDecimal,
    val totalEligibleBudget: BigDecimal,
    val totalEligibleBudgetPercentage: BigDecimal,
    val staffCostTotals: BudgetTotalCostInfo,
    val officeCostTotals: BigDecimal,
    val officeCostFlatRatesTotals: BigDecimal,
    val travelCostTotals: BudgetTotalCostInfo,
    val externalCostTotals: GeneralBudgetTotalCostInfo,
    val equipmentCostTotals: GeneralBudgetTotalCostInfo,
    val infrastructureCostTotals: GeneralBudgetTotalCostInfo,
    val otherCosts: BigDecimal,
    val unitCostsCoveringMultipleCostCategories: BigDecimal,
    val lumpSumsCoveringMultipleCostCategories: BigDecimal,
    val partnerBudgetPerPeriod: List<ProjectPeriodBudgetData>
    ) {
    fun getTotalPartnerContribution() =
        publicContribution.plus(automaticPublicContribution).plus(privateContribution)
}
