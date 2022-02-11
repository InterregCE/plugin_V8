package io.cloudflight.jems.plugin.standard.programme_data_export.programme_project_data_export

import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetGeneralCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import java.math.BigDecimal

internal fun getBudgetPerPeriod(projectData: ProjectData) =
    mutableMapOf<Int, BigDecimal>().also { map ->
        projectData.sectionA?.periods?.map { period ->
            map.put(period.number, projectData.sectionD.projectPartnerBudgetPerPeriodData.totals[period.number])
        }.also { periods ->
            if (!periods.isNullOrEmpty()) {
                map[PREPARATION_PERIOD] =
                    projectData.sectionD.projectPartnerBudgetPerPeriodData.totals.firstOrNull() ?: BigDecimal.ZERO
                map[CLOSURE_PERIOD] =
                    projectData.sectionD.projectPartnerBudgetPerPeriodData.totals.lastOrNull() ?: BigDecimal.ZERO
            }
        }
    }

internal fun getFundInfo(projectData: ProjectData, funds: List<ProgrammeFundData>) =
    funds.map { programmeFund ->
        projectData.sectionD.projectPartnerBudgetPerFundData.firstOrNull { it.partner == null }
            ?.budgetPerFund?.firstOrNull { it.fund?.id == programmeFund.id }
            .let { budgetPerFund ->
                FundInfo(
                    fundAmount = budgetPerFund?.value ?: BigDecimal.ZERO,
                    fundPercentage = budgetPerFund?.percentage ?: BigDecimal.ZERO,
                    percentageOfTotal = budgetPerFund?.percentageOfTotal ?: BigDecimal.ZERO
                )
            }
    }

internal fun getLeadPartnerMainAddress(projectData: ProjectData) =
    projectData.sectionB.partners.firstOrNull { it.role == ProjectPartnerRoleData.LEAD_PARTNER }
        ?.addresses?.firstOrNull { it.type == ProjectPartnerAddressTypeData.Organization }

internal fun sumOfContributionByType(projectData: ProjectData, type: ProjectPartnerContributionStatusData) =
    projectData.sectionD.projectPartnerBudgetPerFundData.firstOrNull { it.partner == null }?.let {
        when (type) {
            ProjectPartnerContributionStatusData.AutomaticPublic -> it.autoPublicContribution
            ProjectPartnerContributionStatusData.Public -> it.publicContribution
            ProjectPartnerContributionStatusData.Private -> it.privateContribution
        }
    } ?: BigDecimal.ZERO

internal fun getStaffCostTotals(partners: Set<ProjectPartnerData>): BudgetTotalCostInfo =
    partners.map { partner ->
        val staffCostTotal = partner.budget.projectBudgetCostsCalculationResult.staffCosts
        val flatRate = partner.budget.projectPartnerOptions?.staffCostsFlatRate
        val staffCosts = partner.budget.projectPartnerBudgetCosts.staffCosts
        BudgetTotalCostInfo(
            total = staffCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else staffCostTotal,
            realCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId == null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId != null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
        )
    }.let { all ->
        BudgetTotalCostInfo(
            total = all.map { it.total }.sumOf { it },
            flatRateTotal = all.map { it.flatRateTotal }.sumOf { it },
            realCostTotal = all.map { it.realCostTotal }.sumOf { it },
            unitCostTotal = all.map { it.unitCostTotal }.sumOf { it }
        )
    }

internal fun getTravelCostTotals(partners: Set<ProjectPartnerData>): BudgetTotalCostInfo =
    partners.map { partner ->
        val travelCostTotal = partner.budget.projectBudgetCostsCalculationResult.travelCosts
        val flatRate = partner.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate
        val travelCosts = partner.budget.projectPartnerBudgetCosts.travelCosts
        BudgetTotalCostInfo(
            total = travelCostTotal,
            flatRateTotal = if (flatRate == null) BigDecimal.ZERO else travelCostTotal,
            realCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId == null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
            unitCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId != null }
                .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
        )
    }.let { all ->
        BudgetTotalCostInfo(
            total = all.map { it.total }.sumOf { it },
            flatRateTotal = all.map { it.flatRateTotal }.sumOf { it },
            realCostTotal = all.map { it.realCostTotal }.sumOf { it },
            unitCostTotal = all.map { it.unitCostTotal }.sumOf { it }
        )
    }

internal fun getGeneralBudgetCostTotalsFor(budgetCosts: List<BudgetGeneralCostEntryData>): GeneralBudgetTotalCostInfo =
    GeneralBudgetTotalCostInfo(
        realCostTotal = budgetCosts.filter { it.unitCostId == null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO },
        unitCostTotal = budgetCosts.filter { it.unitCostId != null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO }
    )

internal fun Any?.toCallCellData() =
    CellData(this).backgroundColor(Color.LIGHT_GREEN)

internal fun Any?.toProjectCellData() =
    CellData(this).backgroundColor(Color.LIGHT_BLUE)

internal fun Any?.toAssessmentCellData() =
    CellData(this).backgroundColor(Color.LIGHT_ORANGE)