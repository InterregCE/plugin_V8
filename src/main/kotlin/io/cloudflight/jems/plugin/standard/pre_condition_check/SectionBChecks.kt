package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallTypeData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingFundTypeData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.standard.common.isFieldVisible
import io.cloudflight.jems.plugin.standard.common.percentageDown
import io.cloudflight.jems.plugin.standard.common.sumOf
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private const val SECTION_B_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.b"
private const val SECTION_B_ERROR_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.error"
private const val SECTION_B_INFO_MESSAGES_PREFIX = "$SECTION_B_MESSAGES_PREFIX.info"

private val decimalFormat = DecimalFormat("#,###.00")

fun checkSectionB(sectionBData: ProjectDataSectionB): PreConditionCheckMessage {
    return buildPreConditionCheckMessage(
        messageKey = "project.application.form.section.part.b", messageArgs = emptyMap(),

        checkIfAtLeastOnePartnerIsActive(sectionBData.partners),

        if (isNotSpf())
            checkIfExactlyOneLeadPartnerIsActive(sectionBData.partners) else null,

        checkIfPartnerIdentityContentIsProvided(sectionBData.partners),

        if (isSpf() && sectionBData.partners.isNotEmpty())
            checkIfPartnerTypeIsFilledIn(sectionBData.partners) else null,

        checkIfPartnerAddressContentIsProvided(sectionBData.partners),

        checkIfPartnerPersonContentIsProvided(sectionBData.partners),

        checkIfPartnerMotivationContentIsProvided(sectionBData.partners),

        checkIfPartnerAssociatedOrganisationIsProvided(sectionBData.associatedOrganisations),

        checkIfStaffContentIsProvided(sectionBData.partners),

        checkIfTravelAndAccommodationContentIsProvided(sectionBData.partners),

        checkIfExternalExpertiseAndServicesContentIsProvided(sectionBData.partners),

        checkIfEquipmentContentIsProvided(sectionBData.partners),

        checkIfInfrastructureAndWorksContentIsProvided(sectionBData.partners),

        if (isSpf())
            checkIfSpfContentIsProvided(sectionBData.partners) else null,

        checkIfUnitCostsContentIsProvided(sectionBData.partners),

        checkIfTotalBudgetIsGreaterThanZero(sectionBData.partners),

        if (isSpf())
            checkIfTotalSpfBudgetIsGreaterThanZero(sectionBData.partners) else null,

        if (isSpf())
            checkIfTotalManagementBudgetIsNotGreaterThanSpfBudget(sectionBData.partners.onlyActive()) else null,

        checkIfPeriodsAmountSumUpToBudgetEntrySum(sectionBData.partners),

        if (isSpf() && isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS))
            checkIfSpfPeriodsAmountSumUpToCorrectSum(sectionBData.partners) else null,

        checkIfCoFinancingContentIsProvided(sectionBData.partners),

        if (isSpf())
            checkIfSpfCoFinancingContentIsProvided(sectionBData.partners) else null,

        if (isSpf())
            checkIfSpfOriginOfContributionIsProvided(sectionBData.partners) else null,

        checkIfPartnerContributionEqualsToBudget(sectionBData.partners),

        checkIfStateAidIsValid(sectionBData.partners)
    )
}

private fun checkIfAtLeastOnePartnerIsActive(partners: Set<ProjectPartnerData>?): PreConditionCheckMessage =
    when (CallDataContainer.get().type) {
        CallTypeData.SPF ->
            if (partners == null || partners.filter { it.active }.size != 1)
                buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.exactly.one.partner.should.be.active")
            else
                buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.exactly.one.partner.is.active")

        CallTypeData.STANDARD, null ->
            if (partners.isNullOrEmpty() || partners.none { it.active })
                buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.at.least.one.partner.should.be.active")
            else
                buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.at.least.one.partner.is.active")
    }

private fun checkIfExactlyOneLeadPartnerIsActive(partners: Set<ProjectPartnerData>?) =
    when {
        partners.isNullOrEmpty() || partners.filter { it.role == ProjectPartnerRoleData.LEAD_PARTNER && it.active}.size != 1 ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.exactly.one.lead.partner.should.be.active")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.exactly.one.lead.partner.is.active")
    }

private fun checkIfTotalBudgetIsGreaterThanZero(partners: Set<ProjectPartnerData>) =
    when {
        partners.sumOf { it.budget.projectBudgetCostsCalculationResult.totalCosts } <= BigDecimal.ZERO ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.budget.should.be.greater.than.zero".suffixSpf())
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.budget.is.greater.than.zero".suffixSpf())
    }

private fun checkIfTotalSpfBudgetIsGreaterThanZero(activePartners: Set<ProjectPartnerData>) =
    when {
        activePartners.sumOf { it.budget.projectPartnerSpfBudgetTotalCost } <= BigDecimal.ZERO ->
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.total.spf.budget.should.be.greater.than.zero")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.total.spf.budget.is.greater.than.zero")
    }

private fun checkIfTotalManagementBudgetIsNotGreaterThanSpfBudget(activePartners: Collection<ProjectPartnerData>): PreConditionCheckMessage {
    val partnersWithInvalidBudget = activePartners
        .filter { it.budget.projectBudgetCostsCalculationResult.totalCosts > it.budget.projectPartnerSpfBudgetTotalCost.multiply(BigDecimal.valueOf(2, 1)) }
    if (partnersWithInvalidBudget.isEmpty())
        return buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.share.management.budget.not.over.20.percent")
    else
        return buildErrorPreConditionCheckMessages(
            messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.share.management.budget",
            messageArgs = emptyMap(),
            checkResults = partnersWithInvalidBudget.map { partner ->
                buildErrorPreConditionCheckMessage(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.share.management.budget.should.not.be.over.20.percent",
                    mapOf("name" to (partner.abbreviation))
                )
            }
        )
}

private fun checkIfCoFinancingContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNullOrEmpty() ||
                partners.any { partner ->
                    partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.amount ?: BigDecimal.ZERO <= BigDecimal.ZERO } ||
                            partner.budget.projectPartnerCoFinancing.partnerContributions.any { partnerContribution -> partnerContribution.status == null } ||
                            partner.budget.projectPartnerCoFinancing.finances.isNullOrEmpty() ||
                            partner.budget.projectPartnerCoFinancing.finances.any { finance -> finance.percentage <= BigDecimal.ZERO }
                } -> {
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
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.co.financing".suffixSpf(),
                    messageArgs = emptyMap(),
                    errorMessages
                )
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfSpfCoFinancingContentIsProvided(partners: Set<ProjectPartnerData>): PreConditionCheckMessage? {
    val emptySourcesPartners = partners.onlyActive().filter { partner ->
        partner.budget.projectPartnerSpfCoFinancing?.partnerContributions?.isEmpty() ?: true
    }

    val invalidSourceNamePartners = partners.onlyActive().filter { partner ->
        partner.budget.projectPartnerSpfCoFinancing?.partnerContributions?.any { source -> source.name.isNullOrBlank()  } ?: false
    }

    val invalidSourceStatus = partners.onlyActive().filter { partner ->
        partner.budget.projectPartnerSpfCoFinancing?.partnerContributions?.any { source -> source.status == null  } ?: false
    }

    val invalidSourceAmount = partners.onlyActive().filter { partner ->
        partner.budget.projectPartnerSpfCoFinancing?.partnerContributions?.any { source ->
            source.amount == null || source.amount!! <= BigDecimal.ZERO
        } ?: false
    }

    val checkResults = listOf(
        emptySourcesPartners.extractErrorsFor("spf.co.financing.is.not.provided"),
        invalidSourceNamePartners.extractErrorsFor("spf.co.financing.name.is.not.provided"),
        invalidSourceStatus.extractErrorsFor("spf.co.financing.status.is.not.provided"),
        invalidSourceAmount.extractErrorsFor("spf.co.financing.amount.is.not.provided"),
    ).flatten()

    if (checkResults.isEmpty())
        return null

    return buildErrorPreConditionCheckMessages(
        messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.spf.co.financing",
        messageArgs = emptyMap(),
        checkResults = checkResults,
    )
}

private fun checkIfSpfOriginOfContributionIsProvided(partners: Set<ProjectPartnerData>): PreConditionCheckMessage? {
    val invalidPartners = partners.onlyActive().map { partner ->
        val partnerFundsSumUp = partner.budget.projectPartnerSpfCoFinancing?.finances
            ?.filter { it.fundType == ProjectPartnerCoFinancingFundTypeData.MainFund }
            ?.map { it.percentage.multiply(BigDecimal.valueOf(1, 2)).multiply(partner.budget.projectPartnerSpfBudgetTotalCost).setScale(2, RoundingMode.DOWN) }
            ?.sumOf { it } ?: BigDecimal.ZERO
        val partnerContribution = partner.budget.projectPartnerSpfBudgetTotalCost - partnerFundsSumUp

        Triple<String, BigDecimal, BigDecimal>(
            partner.abbreviation /* partner abbr */,
            partnerContribution /* actual contribution */,
            (partner.budget.projectPartnerSpfCoFinancing?.partnerContributions
                ?.sumOf { it.amount ?: BigDecimal.ZERO } ?: BigDecimal.ZERO) /* expected contribution */,
        )
    }.filter { it.second.compareTo(it.third) != 0 }

    if (invalidPartners.isEmpty())
        return null

    return buildErrorPreConditionCheckMessages(
        messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.spf.co.financing.contribution",
        messageArgs = emptyMap(),
        checkResults = invalidPartners.map { partnerData ->
            buildErrorPreConditionCheckMessage(
                messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.spf.co.financing.contribution.not.match",
                mapOf("name" to partnerData.first, "actual" to decimalFormat.format(partnerData.second), "expected" to decimalFormat.format(partnerData.third))
            )
        },
    )
}

private fun checkIfPartnerContributionEqualsToBudget(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNotEmpty()
        -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                var fundsAmount = BigDecimal.ZERO
                partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
                    if (finance.fundType != ProjectPartnerCoFinancingFundTypeData.PartnerContribution) {
                        fundsAmount += partner.budget.projectBudgetCostsCalculationResult.totalCosts.percentageDown(
                            finance.percentage
                        )
                    }
                }
                fundsAmount = partner.budget.projectBudgetCostsCalculationResult.totalCosts - fundsAmount

                if (partner.budget.projectPartnerCoFinancing.finances.isEmpty() ||
                    fundsAmount.compareTo(partner.budget.projectPartnerCoFinancing.partnerContributions.sumOf { it.amount }) != 0
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
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.partner.contribution".suffixSpf(),
                    messageArgs = emptyMap(),
                    errorMessages
                )
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfPeriodsAmountSumUpToBudgetEntrySum(partners: Set<ProjectPartnerData>) =
    when {
        !isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_PERIODS) -> null
        partners.isNullOrEmpty() ||
                partners.any { partner ->
                    partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                            partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                            partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                            partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                            partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } } ||
                            partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.rowSum != budgetEntry.budgetPeriods.sumOf { it.amount } }
                } -> {
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
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfSpfPeriodsAmountSumUpToCorrectSum(partners: Set<ProjectPartnerData>): PreConditionCheckMessage? {
    val invalidPartners = partners.filter { partner ->
        partner.budget.projectPartnerBudgetCosts.spfCosts.any { budgetEntry ->
            budgetEntry.budgetPeriods.sumOf { it.amount }.compareTo(budgetEntry.rowSum ?: BigDecimal.ZERO) != 0
        }
    }

    return when {
        invalidPartners.isEmpty() -> null
        else -> buildErrorPreConditionCheckMessages(
            messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods.spf",
            messageArgs = emptyMap(),
            checkResults = invalidPartners.map { partner ->
                buildErrorPreConditionCheckMessage(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.allocation.periods.spf.not.provided",
                    mapOf("name" to (partner.abbreviation))
                )
            },
        )
    }
}

private fun checkIfStaffContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_STAFF_COST_STAFF_FUNCTION) &&
                    partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfTravelAndAccommodationContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_TRAVEL_AND_ACCOMMODATION_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfExternalExpertiseAndServicesContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EXTERNAL_EXPERTISE_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfEquipmentContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_EQUIPMENT_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfInfrastructureAndWorksContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO ||
                        budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.no.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_PRICE_PER_UNIT) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry -> budgetEntry.pricePerUnit <= BigDecimal.ZERO }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.price.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_INFRASTRUCTURE_AND_WORKS_DESCRIPTION) &&
                    partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages) }
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfSpfContentIsProvided(partners: Set<ProjectPartnerData>): PreConditionCheckMessage? {
    val partnersMissingUnitType = if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_SPF_UNIT_TYPE_AND_NUMBER_OF_UNITS))
        partners.filter { partner -> partner.budget.projectPartnerBudgetCosts.spfCosts.any { budgetEntry ->
            budgetEntry.unitType.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        } } else emptyList()

    val partnersMissingNumberOfUnits = if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_SPF_UNIT_TYPE_AND_NUMBER_OF_UNITS))
        partners.filter { partner -> partner.budget.projectPartnerBudgetCosts.spfCosts.any { budgetEntry ->
            budgetEntry.numberOfUnits <= BigDecimal.ZERO
        } } else emptyList()

    val partnersMissingDescription = if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_SPF_DESCRIPTION))
        partners.filter { partner -> partner.budget.projectPartnerBudgetCosts.spfCosts.any { budgetEntry ->
            budgetEntry.description.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        } } else emptyList()

    val partnersMissingPricePerUnit = if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_SPF_PRICE_PER_UNIT))
        partners.filter { partner -> partner.budget.projectPartnerBudgetCosts.spfCosts.any { budgetEntry ->
            budgetEntry.pricePerUnit <= BigDecimal.ZERO
        } } else emptyList()

    val checkResults = listOf(
        partnersMissingUnitType.extractErrorsFor("budget.unit.type.is.not.provided"),
        partnersMissingNumberOfUnits.extractErrorsFor("budget.unit.no.is.not.provided"),
        partnersMissingDescription.extractErrorsFor("budget.description.is.not.provided"),
        partnersMissingPricePerUnit.extractErrorsFor("budget.unit.price.is.not.provided"),
    ).flatten()

    if (checkResults.isEmpty())
        return null

    return buildErrorPreConditionCheckMessages(
        messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.spf",
        messageArgs = emptyMap(),
        checkResults = checkResults,
    )
}

private fun List<ProjectPartnerData>.extractErrorsFor(messageKeySuffix: String) = map { partner ->
    buildErrorPreConditionCheckMessage(
        messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.$messageKeySuffix",
        mapOf("name" to (partner.abbreviation))
    )
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
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_BUDGET_UNIT_COSTS_UNIT_TYPE_AND_NUMBER_OF_UNITS) &&
                    partner.budget.projectPartnerBudgetCosts.unitCosts.any { budgetEntry -> budgetEntry.numberOfUnits <= BigDecimal.ZERO }
                ) {
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
                    partner.vatRecovery == null ||
                    isDepartmentMissingWhenDepartmentAddressIsAvailable(partner)
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
                if (isDepartmentMissingWhenDepartmentAddressIsAvailable(partner)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.department.is.not.provided",
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerTypeIsFilledIn(partners: Set<ProjectPartnerData>): PreConditionCheckMessage {
    val partnersWithInvalidType = partners.onlyActive().filter { it.partnerType == null }
    if (partnersWithInvalidType.isEmpty())
        return buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.project.partner.type")
    else
        return buildErrorPreConditionCheckMessages(
            messageKey = "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.type",
            messageArgs = emptyMap(),
            checkResults = partnersWithInvalidType.map { partner ->
                buildErrorPreConditionCheckMessage(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.type.is.not.provided",
                    mapOf("name" to (partner.abbreviation))
                )
            }
        )
}

private fun checkIfPartnerAddressContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.addresses.isEmpty() ||
                    (isPartnerDepartmentAddressMissingWhenDepartmentIsAvailable(partner)) ||
                    (partner.addresses.any { address ->
                        (address.type == ProjectPartnerAddressTypeData.Organization &&
                                (address.country.isNullOrBlank() ||
                                        address.nutsRegion2.isNullOrBlank() ||
                                        address.nutsRegion3.isNullOrBlank() ||
                                        address.street.isNullOrBlank() ||
                                        address.houseNumber.isNullOrBlank() ||
                                        address.postalCode.isNullOrBlank() ||
                                        address.city.isNullOrBlank())) ||
                                (address.type == ProjectPartnerAddressTypeData.Department &&
                                        checkIfOneOfAddressFieldTouched(address))
                    })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.addresses.isEmpty()) {
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
                    var fieldId =
                        if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_COUNTRY_AND_NUTS
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_COUNTRY_AND_NUTS
                    if (isFieldVisible(fieldId) && address.country.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.country.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (isFieldVisible(fieldId) && address.nutsRegion2.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (isFieldVisible(fieldId) && address.nutsRegion3.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.nuts2.nuts3.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId =
                        if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_STREET
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_STREET
                    if (isFieldVisible(fieldId) && address.street.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.street.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId =
                        if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_HOUSE_NUMBER
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_HOUSE_NUMBER
                    if (isFieldVisible(fieldId) && address.houseNumber.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.house.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId =
                        if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_POSTAL_CODE
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_POSTAL_CODE
                    if (isFieldVisible(fieldId) && address.postalCode.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.postal.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    fieldId =
                        if (address.type == ProjectPartnerAddressTypeData.Organization) ApplicationFormFieldId.PARTNER_MAIN_ADDRESS_CITY
                        else ApplicationFormFieldId.PARTNER_SECONDARY_ADDRESS_CITY
                    if (isFieldVisible(fieldId) && address.city.isNullOrBlank()) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.$messagePostfix.address.city.is.not.provided",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                }
                if (isPartnerDepartmentAddressMissingWhenDepartmentIsAvailable(partner)) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.department.address.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            if (errorMessages.count() > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            } else {
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
                if (partner.contacts.isEmpty() || partner.contacts.size < 2) {
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

private fun checkIfPartnerMotivationContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.motivation?.organizationRelevance.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
                    partner.motivation?.organizationRole.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_COMPETENCES) && partner.motivation?.organizationRelevance.isNotFullyTranslated(
                        CallDataContainer.get().inputLanguages
                    )
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.thematic.competences.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (isFieldVisible(ApplicationFormFieldId.PARTNER_MOTIVATION_ROLE) && partner.motivation?.organizationRole.isNotFullyTranslated(
                        CallDataContainer.get().inputLanguages
                    )
                ) {
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
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfPartnerAssociatedOrganisationIsProvided(associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
    when {
        isFieldVisible(ApplicationFormFieldId.PARTNER_ASSOCIATED_ORGANIZATIONS) &&
                associatedOrganizations.any { associatedOrganization ->
                    associatedOrganization.nameInOriginalLanguage.isNullOrBlank() ||
                            associatedOrganization.partner.id ?: 0 <= 0 ||
                            associatedOrganization.roleDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages) ||
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
                                    } ||
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
                if (associatedOrganization.contacts.isEmpty() || associatedOrganization.contacts.size < 2) {
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
                if (associatedOrganization.roleDescription.isNotFullyTranslated(CallDataContainer.get().inputLanguages)) {
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

private fun checkIfStateAidIsValid(partners: Set<ProjectPartnerData>) =
    when {
        partners.isNotEmpty()
        -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.stateAid != null && isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_CRITERIA_SELF_CHECK)) {
                    if (partner.stateAid?.answer1 == null ||
                        partner.stateAid?.justification1.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer1.justification.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (partner.stateAid?.answer2 == null ||
                        partner.stateAid?.justification2.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria1.answer2.justification.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (partner.stateAid?.answer3 == null ||
                        partner.stateAid?.justification3.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer1.justification.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (partner.stateAid?.answer4 == null ||
                        partner.stateAid?.justification4.isNotFullyTranslated(CallDataContainer.get().inputLanguages)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.criteria2.answer2.justification.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (partner.stateAid?.answer1 ?: false &&
                        partner.stateAid?.answer2 ?: false &&
                        partner.stateAid?.answer3 ?: false &&
                        partner.stateAid?.answer4 ?: false &&
                        partner.stateAid?.stateAidScheme == null &&
                        isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_SCHEME)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.gber.scheme.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                    if (partner.stateAid?.answer4 ?: false &&
                        partner.stateAid?.activities.isNullOrEmpty() &&
                        isFieldVisible(ApplicationFormFieldId.PARTNER_STATE_AID_RELEVANT_ACTIVITIES)
                    ) {
                        errorMessages.add(
                            buildErrorPreConditionCheckMessage(
                                "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid.partner.activities.failed",
                                mapOf("name" to (partner.abbreviation))
                            )
                        )
                    }
                }
            }
            if (errorMessages.size > 0) {
                buildErrorPreConditionCheckMessages(
                    "$SECTION_B_ERROR_MESSAGES_PREFIX.state.aid",
                    messageArgs = emptyMap(),
                    errorMessages
                )
            } else {
                null
            }
        }
        else -> null
    }

private fun checkIfOneOfAddressFieldTouched(address: ProjectPartnerAddressData): Boolean {
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
    return isFieldVisible(fieldId, LifecycleDataContainer.get()!!, CallDataContainer.get())
}

private fun isDepartmentMissingWhenDepartmentAddressIsAvailable(partner: ProjectPartnerData) =
    partner.department.isNullOrEmpty() &&
            partner.addresses.any { address -> address.type == ProjectPartnerAddressTypeData.Department }

private fun isPartnerDepartmentAddressMissingWhenDepartmentIsAvailable(partner: ProjectPartnerData) =
    partner.department.isNotEmpty() &&
            partner.addresses.none { address -> address.type == ProjectPartnerAddressTypeData.Department }

private fun String.suffixSpf() = if (isSpf()) "$this.spf" else this

private fun isSpf() = CallDataContainer.get().type == CallTypeData.SPF

private fun isNotSpf() = !isSpf()

private fun Set<ProjectPartnerData>.onlyActive() = filter { it.active }