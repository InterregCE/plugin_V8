package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal

private const val SECTION_B_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.b"
private const val SECTION_B_ERROR_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.error"
private const val SECTION_B_INFO_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.info"

fun checkSectionB(sectionBData: ProjectDataSectionB): PreConditionCheckMessage =
    buildPreConditionCheckMessage(
        messageKey = "$SECTION_B_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfAtLeastOnePartnerIsAdded(sectionBData.partners),

        checkIfExactlyOneLeadPartnerIsAdded(sectionBData.partners),

        checkIfTotalCoFinancingIsGreaterThanZero(sectionBData.partners),

        checkIfTotalBudgetIsGreaterThanZero(sectionBData.partners),

        checkIfPeriodsAmountSumUpToBudgetEntrySum(sectionBData.partners)
    )


private fun checkIfAtLeastOnePartnerIsAdded(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.at.least.one.partner.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_MESSAGES_PREFIX.at.least.one.partner.is.added")
    }

private fun checkIfExactlyOneLeadPartnerIsAdded(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() || partners.filter { it.role == ProjectPartnerRoleData.LEAD_PARTNER }.size != 1 ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.exactly.one.lead.partner.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.exactly.one.lead.partner.is.added")
    }

private fun checkIfTotalCoFinancingIsGreaterThanZero(partners: Set<ProjectPartnerData>) =
    when {
        partners.sumOf { partner -> partner.budget.projectPartnerCoFinancing.partnerContributions.sumOf { it.amount } } <= BigDecimal.ZERO ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.co.financing.should.be.greater.than.zero")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.co.financing.is.greater.than.zero")
    }

private fun checkIfTotalBudgetIsGreaterThanZero(partners: Set<ProjectPartnerData>) =
    when {
        partners.sumOf { it.budget.projectPartnerBudgetTotalCost } <= BigDecimal.ZERO ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.budget.should.be.greater.than.zero")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.budget.is.greater.than.zero")
    }

private fun checkIfPeriodsAmountSumUpToBudgetEntrySum(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }
        } ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.periods.amount.do.not.sum.up.to.budget.entry.sum")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.periods.amount.sum.up.to.budget.entry.sum")
    }

