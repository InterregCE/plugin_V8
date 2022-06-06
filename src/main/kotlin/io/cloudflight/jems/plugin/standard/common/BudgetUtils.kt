package io.cloudflight.jems.plugin.standard.common

import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingOverviewData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectContributionData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import java.math.BigDecimal
import java.math.RoundingMode

const val CLF_BUDGET_UTILS = "clfBudgetUtils"

class BudgetUtils {

    fun getPublicContributionSubTotal(partnerContributions: Collection<ProjectContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Public }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getAutomaticPublicContributionSubTotal(partnerContributions: Collection<ProjectContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.AutomaticPublic }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getPrivateContributionSubTotal(partnerContributions: Collection<ProjectContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Private }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getContributionTotal(partnerContributions: Collection<ProjectContributionData>) =
        partnerContributions.sumOf { it.amount ?: BigDecimal.ZERO }

    fun getPartnerContribution(finances: Collection<ProjectPartnerCoFinancingData>?, total: BigDecimal): BigDecimal {
        if (finances.isNullOrEmpty())
            return BigDecimal.ZERO

        val funds = finances.filter { it.fundType == ProjectPartnerCoFinancingFundTypeData.MainFund }
            .map { percentageDown(it.percentage, total) }

        return total.minus(funds.sumUp())
    }

    fun getTotalCoFinancingRateForCostCategories(coFinancingOverview: ProjectCoFinancingOverviewData): BigDecimal {
        val managementCoFinancing = coFinancingOverview.projectManagementCoFinancing
        val spfCoFinancing = coFinancingOverview.projectSpfCoFinancing
        val totalFundingAmountForCostCategories = managementCoFinancing.totalFundingAmount.add(spfCoFinancing.totalFundingAmount)
        val totalFundAndContributionForCostCategories = managementCoFinancing.totalFundAndContribution.add(spfCoFinancing.totalFundAndContribution)

        return if (totalFundAndContributionForCostCategories.equals(BigDecimal.ZERO)) {
            BigDecimal.ZERO
        } else {
            totalFundingAmountForCostCategories
                .multiply(100.toBigDecimal())
                .divide(totalFundAndContributionForCostCategories, 2, RoundingMode.DOWN)
        }

    }

    fun percentageDown(percentage: BigDecimal, total: BigDecimal) =
        total.percentageDown(percentage)

    // used in the templates
    fun percentageDownTo(amount: BigDecimal, total: BigDecimal) =
        amount.percentageTo(total)

    private fun Collection<BigDecimal>.sumUp() = sumOf { it }

}