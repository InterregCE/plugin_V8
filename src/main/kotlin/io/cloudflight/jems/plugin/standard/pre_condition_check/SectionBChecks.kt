package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectContactTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingFundTypeData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal

private const val SECTION_B_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.b"
private const val SECTION_B_ERROR_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.error"
private const val SECTION_B_INFO_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.info"

private var _lifecycleData: ProjectLifecycleData? = null
private var _callData: CallDetailData? = null

fun checkSectionB(sectionBData: ProjectDataSectionB, lifecycleData: ProjectLifecycleData, callData: CallDetailData): PreConditionCheckMessage {
    _lifecycleData = lifecycleData
    _callData = callData

    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_B_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfAtLeastOnePartnerIsAdded(sectionBData.partners),

        checkIfExactlyOneLeadPartnerIsAdded(sectionBData.partners),

        checkIfTotalCoFinancingIsGreaterThanZero(sectionBData.partners),

        checkIfCoFinancingContentIsProvided(sectionBData.partners),

        checkIfTotalBudgetIsGreaterThanZero(sectionBData.partners),

        checkIfPeriodsAmountSumUpToBudgetEntrySum(sectionBData.partners),

        checkIfStaffContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfTravelAndAccommodationContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfExternalExpertiseAndServicesContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfEquipmentContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfInfrastructureAndWorksContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfUnitCostsContentIsProvided(sectionBData.partners),

        checkIfPartnerIdentityContentIsProvided(sectionBData.partners),

        checkIfPartnerAddressContentIsProvided(sectionBData.partners),

        checkIfPartnerPersonContentIsProvided(sectionBData.partners),

        checkIfPartnerMotivationContentIsProvided(callData.inputLanguages, sectionBData.partners),

        checkIfPartnerContributionEqualsToBudget(sectionBData.partners),

        checkIfPartnerAssociatedOrganisationIsProvided(callData.inputLanguages, sectionBData.associatedOrganisations)
    )
}

private fun checkIfAtLeastOnePartnerIsAdded(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() -> buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.at.least.one.partner.should.be.added")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.at.least.one.partner.is.added")
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

private fun checkIfCoFinancingContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() ||
                partners.any { partner ->
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.amount ?: BigDecimal.ZERO <= BigDecimal.ZERO } ||
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null } ||
                    partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty() ||
                    partner.budget.projectPartnerCoFinancing.finances.any { finance -> finance.percentage <= BigDecimal.ZERO }
                } ->
        {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing.source.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing.legal.status.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerContributionEqualsToBudget(partners: Set<ProjectPartnerData>): PreConditionCheckMessage {
    val errorMessages = mutableListOf<PreConditionCheckMessage>()
    partners.forEach { partner ->
        var fundsAmount = BigDecimal.ZERO
        partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
            if (finance.fundType != ProjectPartnerCoFinancingFundTypeData.PartnerContribution) {
                fundsAmount += partner.budget.projectPartnerBudgetTotalCost.percentageDown(finance.percentage)
            }
        }
        fundsAmount = partner.budget.projectPartnerBudgetTotalCost - fundsAmount

        if (partner.budget.projectPartnerCoFinancing.finances.isEmpty() ||
            fundsAmount !=
            partner.budget.projectPartnerCoFinancing.partnerContributions.sumOf { it.amount }
        ) {
            errorMessages.add(
                buildErrorPreConditionCheckMessage(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution.failed",
                    mapOf("name" to (partner.abbreviation))
                )
            )
        }
    }
    if (errorMessages.size > 0) {
        return buildErrorPreConditionCheckMessages(
            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution",
            messageArgs = emptyMap(),
            errorMessages
        )
    }
    return buildInfoPreConditionCheckMessage(
        "$SECTION_B_INFO_MESSAGES_PREFIX.budget.partner.contribution",
    )
}

private fun checkIfPeriodsAmountSumUpToBudgetEntrySum(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() ||
            partners.any { partner ->
                partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }
            } ->
        {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfStaffContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(mandatoryLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(mandatoryLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.staff.costs",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfTravelAndAccommodationContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(mandatoryLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(mandatoryLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.travel.accommodation",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfExternalExpertiseAndServicesContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(mandatoryLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(mandatoryLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.external.expertise",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfEquipmentContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(mandatoryLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(mandatoryLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.equipment",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfInfrastructureAndWorksContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(mandatoryLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(mandatoryLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(mandatoryLanguages) }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.description.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.infrastructure.works",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfUnitCostsContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry ->
                budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.unitCostId <= 0
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.unitCostId <= 0 }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.cost.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.cost",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerIdentityContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.abbreviation.isBlank() ||
                    partner.nameInEnglish.isNullOrBlank() ||
                    partner.nameInOriginalLanguage.isNullOrBlank() ||
                    partner.legalStatusId ?: 0 <= 0 ||
                    partner.vat.isNullOrEmpty() ||
                    partner.vatRecovery == null
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ABBREVIATED_ORGANISATION_NAME) && partner.abbreviation.isBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.abbreviated.name.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ENGLISH_NAME_OF_ORGANISATION) && partner.nameInEnglish.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.english.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_ORIGINAL_NAME_OF_ORGANISATION) && partner.nameInOriginalLanguage.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.original.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_STATUS) && partner.legalStatusId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.legal.status.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_IDENTIFIER) && partner.vat.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_VAT_RECOVERY) && partner.vatRecovery == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.entitled.recover.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.identity",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerAddressContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.addresses.isEmpty() || (
                partner.addresses.any { address ->
                    (address.type == ProjectPartnerAddressTypeData.Organization &&
                        (address.country.isNullOrBlank() ||
                        address.nutsRegion2.isNullOrBlank() ||
                        address.nutsRegion3.isNullOrBlank() ||
                        address.street.isNullOrBlank() ||
                        address.houseNumber.isNullOrBlank() ||
                        address.postalCode.isNullOrBlank() ||
                        address.city.isNullOrBlank())) ||
                    (address.type == ProjectPartnerAddressTypeData.Department &&
                        checkIfOneOfAddressFieldTouched(address)
                    )
                })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.addresses.isEmpty())
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.address.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                partner.addresses.forEach { address ->
                    val messagePostfix = if (address.type == ProjectPartnerAddressTypeData.Organization)
                        "main"
                        else
                        "department"
                    if (address.country.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.country.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.nutsRegion2.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.nutsRegion3.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.nuts3.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.street.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.street.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.houseNumber.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.house.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.postalCode.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.postal.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (address.city.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.city.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else
            {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerPersonContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.contacts.isEmpty() || partner.contacts.size < 2 || (
                    partner.contacts.any { contact ->
                        contact.type == ProjectContactTypeData.ContactPerson &&
                            (contact.firstName.isNullOrBlank() ||
                                contact.lastName.isNullOrBlank() ||
                                contact.telephone.isNullOrBlank() ||
                                contact.email.isNullOrBlank())
                    } ||
                    partner.contacts.any { contact ->
                        contact.type == ProjectContactTypeData.LegalRepresentative &&
                            (contact.firstName.isNullOrBlank() ||
                                contact.lastName.isNullOrBlank())
                    })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.contacts.isEmpty() || partner.contacts.size < 2)
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.or.representative.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                partner.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson) {
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_FIRST_NAME) && contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_LAST_NAME) && contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.last.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_EMAIL) && contact.email.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.email.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_CONTACT_PERSON_TELEPHONE) && contact.telephone.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.phone.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    } else {
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_REPRESENTATIVE_FIRST_NAME) && contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (isFieldVisible(ApplicationFormFieldId.PARTNER_LEGAL_REPRESENTATIVE_LAST_NAME) && contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.last.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    }
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerMotivationContentIsProvided(mandatoryLanguages: Set<SystemLanguageData>, partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.motivation?.organizationRelevance.isNotFullyTranslated(mandatoryLanguages) ||
                    partner.motivation?.organizationRole.isNotFullyTranslated(mandatoryLanguages)
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_COMPETENCES) && partner.motivation?.organizationRelevance.isNotFullyTranslated(mandatoryLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.thematic.competences.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_ROLE) && partner.motivation?.organizationRole.isNotFullyTranslated(mandatoryLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.role.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            }
            else {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerAssociatedOrganisationIsProvided(mandatoryLanguages: Set<SystemLanguageData>, associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
    when {
        associatedOrganizations.any { associatedOrganization ->
            associatedOrganization.nameInOriginalLanguage.isNullOrBlank() ||
                    associatedOrganization.partner.id ?: 0 <= 0 ||
                    associatedOrganization.roleDescription.isNotFullyTranslated(mandatoryLanguages) ||
                    (associatedOrganization.address != null &&
                            (associatedOrganization.address?.country.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion2.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion3.isNullOrBlank() ||
                                    associatedOrganization.address?.postalCode.isNullOrBlank() ||
                                    associatedOrganization.address?.houseNumber.isNullOrBlank() ||
                                    associatedOrganization.address?.city.isNullOrBlank()
                                    )
                            ) ||
                    (associatedOrganization.contacts.isNullOrEmpty() ||
                        associatedOrganization.contacts.any { contact ->
                            contact.type == ProjectContactTypeData.ContactPerson &&
                                    (contact.firstName.isNullOrBlank() ||
                                        contact.lastName.isNullOrBlank() ||
                                        contact.telephone.isNullOrBlank() ||
                                        contact.email.isNullOrBlank())
                        }  ||
                        associatedOrganization.contacts.any { contact ->
                            contact.type == ProjectContactTypeData.LegalRepresentative &&
                                    (contact.firstName.isNullOrBlank() ||
                                            contact.lastName.isNullOrBlank())
                        }
                    )
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            associatedOrganizations.forEach { associatedOrganization ->
                if (associatedOrganization.nameInOriginalLanguage.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.name.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.partner.id ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.partner.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.country.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.country.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.nutsRegion2.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.nuts2.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.nutsRegion3.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.nuts3.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.street.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.street.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.houseNumber.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.house.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.postalCode.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.postal.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.address?.city.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.city.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                if (associatedOrganization.contacts.isEmpty() || associatedOrganization.contacts.size < 2)
                {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.or.representative.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
                associatedOrganization.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson) {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.first.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.last.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.email.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.email.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.telephone.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.phone.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                    } else {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.first.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.last.name.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            )
                        }
                    }
                }
                if (associatedOrganization.roleDescription.isNotFullyTranslated(mandatoryLanguages)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.role.is.not.provided",
                            mapOf(
                                "name" to (associatedOrganization.nameInOriginalLanguage
                                    ?: associatedOrganization.id.toString())
                            )
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfOneOfAddressFieldTouched(address: ProjectPartnerAddressData): Boolean
{
    val oneOfTouched =
        address.country?.isNotBlank() ?: false ||
                address.nutsRegion2?.isNotBlank() ?: false ||
                address.nutsRegion3?.isNotBlank() ?: false ||
                address.street?.isNotBlank() ?: false ||
                address.houseNumber?.isNotBlank() ?: false ||
                address.postalCode?.isNotBlank() ?: false ||
                address.city?.isNotBlank() ?: false

    val oneOfEmpty =
        address.country.isNullOrBlank() ||
                address.nutsRegion2.isNullOrBlank() ||
                address.nutsRegion3.isNullOrBlank() ||
                address.street.isNullOrBlank() ||
                address.houseNumber.isNullOrBlank() ||
                address.postalCode.isNullOrBlank() ||
                address.city.isNullOrBlank()

    return oneOfTouched && oneOfEmpty
}

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, _lifecycleData!!, _callData!!)
}
