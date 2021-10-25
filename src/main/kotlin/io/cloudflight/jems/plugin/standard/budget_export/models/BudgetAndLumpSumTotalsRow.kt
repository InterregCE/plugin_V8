package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal


open class BudgetAndLumpSumTotalsRow(
    val partnerInfo: PartnerInfo,
    val fundInfoList: List<FundInfo>,
    val publicContribution: BigDecimal,
    val automaticPublicContribution: BigDecimal,
    val privateContribution: BigDecimal,
    val totalEligibleBudget: BigDecimal,
    val staffCostTotals: BudgetTotalCostInfo,
    val officeCostTotals: BigDecimal,
    val officeCostFlatRatesTotals: BigDecimal,
    val travelCostTotals: BudgetTotalCostInfo,
    val externalCostTotals: GeneralBudgetTotalCostInfo,
    val equipmentCostTotals: GeneralBudgetTotalCostInfo,
    val infrastructureCostTotals: GeneralBudgetTotalCostInfo,
    val otherCosts: BigDecimal,
    val unitCostsCoveringMultipleCostCategories: BigDecimal,
    val lumpSumsCoveringMultipleCostCategories: BigDecimal
) {
    fun getTotalPartnerContribution() =
        publicContribution.plus(automaticPublicContribution).plus(privateContribution)
}
