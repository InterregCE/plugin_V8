package io.cloudflight.jems.plugin.standard.report

import io.cloudflight.jems.plugin.contract.models.report.partner.expenditure.ProjectPartnerReportExpenditureCostData
import io.cloudflight.jems.plugin.contract.models.report.partner.expenditure.ReportBudgetCategoryData
import io.cloudflight.jems.plugin.contract.pre_condition_check.ControlReportSamplingCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.ControlReportSamplingCheckResult
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
open class ControlReportSamplingCheck(val dataProvider: ReportPartnerDataProvider): ControlReportSamplingCheckPlugin {

    override fun check(partnerId: Long, reportId: Long): ControlReportSamplingCheckResult {
        val expenditures = dataProvider.getExpenditureCosts(partnerId, reportId)
        val totalEligibleBudget = dataProvider.getCoFinancingOverview(partnerId, reportId).total.totalEligibleBudget

        val sampledExpenditureIds = expenditures.filter {
           it.hasProcurement() || it.isStaffCost() || it.requireToNotHaveVatFor(totalEligibleBudget) || it.isReIncluded()
        }.mapTo(HashSet()) { it.id!! }

        return ControlReportSamplingCheckResult(sampledExpenditureIds)
    }

    private fun ProjectPartnerReportExpenditureCostData.hasProcurement() = contractId != null && contractId != 0L

    private fun ProjectPartnerReportExpenditureCostData.isStaffCost() = costCategory == ReportBudgetCategoryData.StaffCosts

    private fun ProjectPartnerReportExpenditureCostData.requireToNotHaveVatFor(budget: BigDecimal) =
        budget > BigDecimal(5000000) && vat != null && vat!! > BigDecimal.ZERO

    private fun ProjectPartnerReportExpenditureCostData.isReIncluded() = parkingMetadata != null

    override fun getDescription(): String =
        "Control risk based sampling (automation)"

    override fun getKey() =
        "control-report-sampling"

    override fun getName() =
        "Control risk based sampling"

    override fun getVersion(): String =
        "0.0.1"

}