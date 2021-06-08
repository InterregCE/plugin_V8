package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectContactTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerCoFinancingFundTypeData
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

        checkIfPeriodsAmountSumUpToBudgetEntrySum(sectionBData.partners),

        checkIfStaffContentIsProvided(sectionBData.partners),

        checkIfTravelAndAccommodationContentIsProvided(sectionBData.partners),

        checkIfExternalExpertiseAndServicesContentIsProvided(sectionBData.partners),

        checkIfEquipmentContentIsProvided(sectionBData.partners),

        checkIfInfrastructureAndWorksContentIsProvided(sectionBData.partners),

        checkIfUnitCostsContentIsProvided(sectionBData.partners),

        checkIfPartnerIdentityContentIsProvided(sectionBData.partners),

        checkIfPartnerAddressContentIsProvided(sectionBData.partners),

        checkIfPartnerPersonContentIsProvided(sectionBData.partners),

        checkIfPartnerMotivationContentIsProvided(sectionBData.partners),

        checkIfPartnerContributionEqualsToBudget(sectionBData.partners),

        checkIfPartnerAssociatedOrganisationIsProvided(sectionBData.associatedOrganisations)
    )


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

private fun checkIfPartnerContributionEqualsToBudget(partners: Set<ProjectPartnerData>): PreConditionCheckMessage {
    val errorMessages = mutableListOf<PreConditionCheckMessage>()
    partners.forEach { partner ->

        var fundsAmount = BigDecimal.ZERO;
        partner.budget.projectPartnerCoFinancing.finances.forEach { finance ->
            if (finance.fundType != ProjectPartnerCoFinancingFundTypeData.PartnerContribution) {
                fundsAmount = fundsAmount + partner.budget.projectPartnerBudgetTotalCost.percentageDown(
                    finance.percentage ?: BigDecimal.ZERO
                );
            }
        }
        fundsAmount = partner.budget.projectPartnerBudgetTotalCost - fundsAmount;

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
            buildErrorPreConditionCheckMessage("$SECTION_B_ERROR_MESSAGES_PREFIX.periods.amount.do.not.sum.up.to.budget.entry.sum")
        else -> buildInfoPreConditionCheckMessage("$SECTION_B_INFO_MESSAGES_PREFIX.periods.amount.sum.up.to.budget.entry.sum")
    }

private fun checkIfStaffContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation()) ||
                        budgetEntry.comment.isNullOrEmptyOrMissingAnyTranslation() ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.staffCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation() }
                ) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.unit.type.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.budget.projectPartnerBudgetCosts.staffCosts.any { budgetEntry -> budgetEntry.comment.isNullOrEmptyOrMissingAnyTranslation() }) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.comments.is.not.provided",
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
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.staff.costs",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfTravelAndAccommodationContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.travelCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation()) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.travelCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation() }
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
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.travel.accommodation",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfExternalExpertiseAndServicesContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.externalCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation()) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.externalCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation() }
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
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.external.expertise",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfEquipmentContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.equipmentCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation()) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.equipmentCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation() }
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
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.equipment",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfInfrastructureAndWorksContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.budget.projectPartnerBudgetCosts.infrastructureCosts.any { budgetEntry ->
                (budgetEntry.unitCostId == null && budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation()) ||
                        budgetEntry.numberOfUnits <= BigDecimal.ZERO ||
                        budgetEntry.pricePerUnit <= BigDecimal.ZERO
            }
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.budget.projectPartnerBudgetCosts.infrastructureCosts
                        .filter { budgetEntry -> budgetEntry.unitCostId == null }
                        .any { budgetEntry -> budgetEntry.unitType.isNullOrEmptyOrMissingAnyTranslation() }
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
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.budget.infrastructure.works",
                messageArgs = emptyMap(),
                errorMessages
            )
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
                if (partner.abbreviation.isBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.abbreviated.name.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.nameInEnglish.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.english.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.nameInOriginalLanguage.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.organisation.name.original.language.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.legalStatusId ?: 0 <= 0) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.legal.status.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.vat.isNullOrEmpty()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.vatRecovery == null) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.entitled.recover.vat.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.identity",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerAddressContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.addresses.size == 2 && (
                    partner.addresses[0].country.isNullOrBlank() ||
                            partner.addresses[0].nutsRegion2.isNullOrBlank() ||
                            partner.addresses[0].nutsRegion3.isNullOrBlank() ||
                            partner.addresses[0].street.isNullOrBlank() ||
                            partner.addresses[0].houseNumber.isNullOrBlank() ||
                            partner.addresses[0].postalCode.isNullOrBlank() ||
                            partner.addresses[0].city.isNullOrBlank())
        } -> {
            var errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.addresses[0].country.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.country.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].nutsRegion2.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.nuts2.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].nutsRegion3.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.nuts2.nuts3.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].street.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.street.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].houseNumber.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.house.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].postalCode.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.postal.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.addresses[0].city.isNullOrBlank()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address.city.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.main.address",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerPersonContentIsProvided(partners: Set<ProjectPartnerData>) =
    when {
        partners.any { partner ->
            partner.contacts.size == 2 && (
                    partner.contacts.any { contact ->
                        contact.type == ProjectContactTypeData.ContactPerson &&
                                (contact.firstName.isNullOrBlank() ||
                                        contact.lastName.isNullOrBlank() ||
                                        contact.telephone.isNullOrBlank() ||
                                        contact.email.isNullOrBlank())
                    } ||
                            partner.contacts.any { contact ->
                                contact.type == ProjectContactTypeData.ContactPerson &&
                                        (contact.firstName.isNullOrBlank() ||
                                                contact.lastName.isNullOrBlank() ||
                                                contact.telephone.isNullOrBlank() ||
                                                contact.email.isNullOrBlank())
                            })
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                partner.contacts.forEach { contact ->
                    if (contact.type == ProjectContactTypeData.ContactPerson) {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.person.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.partner.person.last.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (contact.email.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.email.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (contact.telephone.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.person.phone.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                    } else {
                        if (contact.firstName.isNullOrBlank()) {
                            errorMessages.add(
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.representative.first.name.is.not.provided",
                                    mapOf("name" to (partner.abbreviation))
                                )
                            )
                        }
                        if (contact.lastName.isNullOrBlank()) {
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
            partner.motivation?.organizationRelevance.isNullOrEmptyOrMissingAnyTranslation() ||
                    partner.motivation?.organizationRole.isNullOrEmptyOrMissingAnyTranslation()
        } -> {
            val errorMessages = mutableListOf<PreConditionCheckMessage>()
            partners.forEach { partner ->
                if (partner.motivation?.organizationRelevance.isNullOrEmptyOrMissingAnyTranslation()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.thematic.competences.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
                if (partner.motivation?.organizationRole.isNullOrEmptyOrMissingAnyTranslation()) {
                    errorMessages.add(
                        buildErrorPreConditionCheckMessage(
                            "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation.role.is.not.provided",
                            mapOf("name" to (partner.abbreviation))
                        )
                    )
                }
            }
            buildErrorPreConditionCheckMessages(
                "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.motivation",
                messageArgs = emptyMap(),
                errorMessages
            )
        }
        else -> null
    }

private fun checkIfPartnerAssociatedOrganisationIsProvided(associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
    when {
        associatedOrganizations.any { associatedOrganization ->
            associatedOrganization.nameInOriginalLanguage.isNullOrBlank() ||
                    associatedOrganization.partner.id ?: 0 <= 0 ||
                    associatedOrganization.roleDescription.isNullOrEmptyOrMissingAnyTranslation() ||
                    (associatedOrganization.address != null &&
                            (associatedOrganization.address?.country.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion2.isNullOrBlank() ||
                                    associatedOrganization.address?.nutsRegion3.isNullOrBlank() ||
                                    associatedOrganization.address?.postalCode.isNullOrBlank() ||
                                    associatedOrganization.address?.houseNumber.isNullOrBlank() ||
                                    associatedOrganization.address?.city.isNullOrBlank()
                                    )
                            ) ||
                    (associatedOrganization.contacts.size == 2 &&
                            (
                                    associatedOrganization.contacts.any { contact ->
                                        contact.type == ProjectContactTypeData.ContactPerson &&
                                                (contact.firstName.isNullOrBlank() ||
                                                        contact.lastName.isNullOrBlank() ||
                                                        contact.telephone.isNullOrBlank() ||
                                                        contact.email.isNullOrBlank())
                                    } ||
                                            associatedOrganization.contacts.any { contact ->
                                                contact.type == ProjectContactTypeData.ContactPerson &&
                                                        (contact.firstName.isNullOrBlank() ||
                                                                contact.lastName.isNullOrBlank() ||
                                                                contact.telephone.isNullOrBlank() ||
                                                                contact.email.isNullOrBlank())
                                            }
                                    )
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
                if (associatedOrganization.contacts.size == 2) {
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
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.email.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
                                    )
                                )
                            }
                            if (contact.telephone.isNullOrBlank()) {
                                buildErrorPreConditionCheckMessage(
                                    "$SECTION_B_ERROR_MESSAGES_PREFIX.project.partner.associated.organisation.contact.person.phone.is.not.provided",
                                    mapOf(
                                        "name" to (associatedOrganization.nameInOriginalLanguage
                                            ?: associatedOrganization.id.toString())
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

                }
                if (associatedOrganization.roleDescription.isNullOrEmptyOrMissingAnyTranslation()) {
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
