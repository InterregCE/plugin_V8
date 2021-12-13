package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.ApplicationFormFieldConfigurationData
import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.CallStatusData
import io.cloudflight.jems.plugin.contract.models.call.FieldVisibilityStatusData
import io.cloudflight.jems.plugin.contract.models.call.flatrate.FlatRateSetupData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.ProjectDataSectionB
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.*
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ChecksSectionB {

    companion object {
        val mandatoryLanguages = setOf(SystemLanguageData.EN, SystemLanguageData.DE)
        val oneStepCallData = CallDetailData(
            id = 0,
            name = "Section B Test Call",
            status = CallStatusData.PUBLISHED,
            startDateTime = ZonedDateTime.of(2021, 12, 10, 10, 19, 32, 90000, ZoneId.systemDefault()),
            endDateTimeStep1 = null,
            endDateTime = ZonedDateTime.of(2027, 12, 3, 12, 20, 59, 90000, ZoneId.systemDefault()),
            isAdditionalFundAllowed = true,
            lengthOfPeriod = 12,
            description = emptySet(),
            objectives = emptyList(),
            strategies = emptyList(),
            funds = emptyList(),
            flatRates = FlatRateSetupData(null, null, null, null, null),
            lumpSums = emptyList(),
            unitCosts = emptyList(),
            applicationFormFieldConfigurations =
            mutableSetOf(
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_TITLE.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_ACRONYM.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_DURATION.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_PRIORITY.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_OBJECTIVE.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_TITLE.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
                ApplicationFormFieldConfigurationData(
                    ApplicationFormFieldId.PROJECT_SUMMARY.id,
                    FieldVisibilityStatusData.STEP_ONE_AND_TWO
                ),
            ),
            inputLanguages = ChecksSectionA.mandatoryLanguages
        )
        val projectLifecycleData = ProjectLifecycleData(status = ApplicationStatusData.DRAFT)

        val coFinancingData = ProjectPartnerCoFinancingAndContributionData(
            finances = emptyList(),
            partnerContributions = emptyList(),
            partnerAbbreviation = ""
        )
        val partnerBudgetCosts = BudgetCostData(
            staffCosts = emptyList(),
            travelCosts = emptyList(),
            externalCosts = emptyList(),
            equipmentCosts = emptyList(),
            infrastructureCosts = emptyList(),
            unitCosts = emptyList()
        )
        val budgetCostsCalculationResultData = BudgetCostsCalculationResultData(
            staffCosts = BigDecimal.ZERO,
            travelCosts = BigDecimal.ZERO,
            officeAndAdministrationCosts = BigDecimal.ZERO,
            otherCosts = BigDecimal.ZERO,
            totalCosts = BigDecimal.ZERO
        )
        val budget = PartnerBudgetData(
            projectPartnerOptions = null,
            projectPartnerCoFinancing = coFinancingData,
            projectPartnerBudgetCosts = partnerBudgetCosts,
            projectPartnerBudgetTotalCost = BigDecimal.ZERO,
            projectBudgetCostsCalculationResult = budgetCostsCalculationResultData
        )
        val partner = ProjectPartnerData(
            id = 123L,
            abbreviation = "",
            role = ProjectPartnerRoleData.LEAD_PARTNER,
            sortNumber = 1,
            nameInOriginalLanguage = "",
            nameInEnglish = "",
            partnerType = ProjectTargetGroupData.LocalPublicAuthority,
            partnerSubType = null,
            nace = null,
            otherIdentifierNumber = "",
            pic = null,
            legalStatusId = null,
            vat = null,
            vatRecovery = null,
            motivation = null,
            stateAid = null,
            budget = budget
        )

        val sectionBData = ProjectDataSectionB(
            partners = emptySet(),
            associatedOrganisations = emptySet()
        )
    }


    @Test
    fun `should show ERROR when project partners are not provided`() {
        CallDataContainer.set(oneStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val checkSectionB = checkSectionB(sectionBData)
        assertThat(checkSectionB.messageType == MessageType.ERROR).isTrue
        assertThat(checkSectionB.subSectionMessages.any { preConditionCheckMessage ->
            preConditionCheckMessage.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.b.error.at.least.one.partner.should.be.added"
        }).isTrue
    }

    @Test
    fun `should show ERROR when partners total co-financing budget is zero`() {
        CallDataContainer.set(oneStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val sectionBData = ProjectDataSectionB(
            partners = setOf(partner),
            associatedOrganisations = emptySet()
        )
        val checkSectionB = checkSectionB(sectionBData)
        assertThat(checkSectionB.messageType == MessageType.ERROR).isTrue
        assertThat(checkSectionB.subSectionMessages.any { preConditionCheckMessage ->
            preConditionCheckMessage.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.b.error.total.co.financing.should.be.greater.than.zero"
        }).isTrue
    }

    @Test
    fun `should show INFO when partners total co-financing is greater than zero`() {
        CallDataContainer.set(oneStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val programmeFund = ProgrammeFundData(
            id = 213L,
            selected = true,
            type = ProgrammeFundTypeData.ERDF,
            abbreviation = emptySet(),
            description = emptySet()
        )
        val projectPartnerFinancingData = ProjectPartnerCoFinancingData(
            fundType = ProjectPartnerCoFinancingFundTypeData.MainFund,
            fund = programmeFund,
            percentage = BigDecimal(100)
        )
        val coFinancingData = ProjectPartnerCoFinancingAndContributionData(
            finances = listOf(projectPartnerFinancingData),
            partnerContributions = emptyList(),
            partnerAbbreviation = ""
        )
        val budget = PartnerBudgetData(
            projectPartnerOptions = null,
            projectPartnerCoFinancing = coFinancingData,
            projectPartnerBudgetCosts = partnerBudgetCosts,
            projectPartnerBudgetTotalCost = BigDecimal.ZERO,
            projectBudgetCostsCalculationResult = budgetCostsCalculationResultData
        )
        val partner = ProjectPartnerData(
            id = 123L,
            abbreviation = "",
            role = ProjectPartnerRoleData.LEAD_PARTNER,
            sortNumber = 1,
            nameInOriginalLanguage = "",
            nameInEnglish = "",
            partnerType = ProjectTargetGroupData.LocalPublicAuthority,
            partnerSubType = null,
            nace = null,
            otherIdentifierNumber = "",
            pic = null,
            legalStatusId = null,
            vat = null,
            vatRecovery = null,
            motivation = null,
            stateAid = null,
            budget = budget
        )
        val sectionBData = ProjectDataSectionB(
            partners = setOf(partner),
            associatedOrganisations = emptySet()
        )
        val checkSectionB = checkSectionB(sectionBData)
        assertThat(checkSectionB.subSectionMessages.any { preConditionCheckMessage ->
            preConditionCheckMessage.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.b.info.total.co.financing.is.greater.than.zero"
        }).isTrue
    }

    @Test
    fun `should show ERROR when partner department is missing and department address is available`() {
        CallDataContainer.set(oneStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val departmentAddress = ProjectPartnerAddressData(
            ProjectPartnerAddressTypeData.Department,
            country = "Italy",
            nutsRegion2 = "region2",
            nutsRegion3 = "region3",
            street = "street",
            houseNumber = "123",
            postalCode = "123",
            city = "City",
            homepage = "homepage"
        )
        val partner = ProjectPartnerData(
            id = 123L,
            abbreviation = "abbr",
            role = ProjectPartnerRoleData.LEAD_PARTNER,
            sortNumber = 1,
            nameInOriginalLanguage = "name",
            nameInEnglish = "name",
            department = emptySet(),
            partnerType = ProjectTargetGroupData.LocalPublicAuthority,
            partnerSubType = null,
            nace = null,
            otherIdentifierNumber = "",
            pic = null,
            legalStatusId = null,
            vat = null,
            vatRecovery = null,
            addresses = listOf(departmentAddress),
            motivation = null,
            stateAid = null,
            budget = budget
        )
        val sectionBData = ProjectDataSectionB(
            partners = setOf(partner),
            associatedOrganisations = emptySet()
        )
        val checkSectionB = checkSectionB(sectionBData)
        val identitySubSection = checkSectionB.subSectionMessages.firstOrNull() { subSectionMessages ->
            subSectionMessages.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.b.error.project.partner.identity"
        }
        assertThat(checkSectionB.messageType == MessageType.ERROR).isTrue
        assertThat(identitySubSection != null &&
                identitySubSection?.subSectionMessages?.any { preConditionCheckMessage ->
                    preConditionCheckMessage.message.i18nKey ==
                            "jems.standard.pre.condition.check.plugin.project.section.b.error.project.partner.department.is.not.provided"
                }).isTrue
    }


    @Test
    fun `should show ERROR when partner department address is missing and department is available`() {
        CallDataContainer.set(oneStepCallData)
        LifecycleDataContainer.set(projectLifecycleData)
        val partner = ProjectPartnerData(
            id = 123L,
            abbreviation = "abbr",
            role = ProjectPartnerRoleData.LEAD_PARTNER,
            sortNumber = 1,
            nameInOriginalLanguage = "name",
            nameInEnglish = "name",
            department = setOf(InputTranslationData(SystemLanguageData.EN, "test department")),
            partnerType = ProjectTargetGroupData.LocalPublicAuthority,
            partnerSubType = null,
            nace = null,
            otherIdentifierNumber = "",
            otherIdentifierDescription = emptySet(),
            pic = null,
            legalStatusId = null,
            legalStatusDescription = emptySet(),
            vat = null,
            vatRecovery = null,
            addresses = emptyList(),
            contacts = emptyList(),
            motivation = null,
            stateAid = null,
            budget = budget
        )
        val sectionBData = ProjectDataSectionB(
            partners = setOf(partner),
            associatedOrganisations = emptySet()
        )
        val checkSectionB = checkSectionB(sectionBData)
        val partnerAddressSubSection = checkSectionB.subSectionMessages.firstOrNull() { subSectionMessages ->
            subSectionMessages.message.i18nKey == "jems.standard.pre.condition.check.plugin.project.section.b.error.project.partner.main.address"
        }
        assertThat(checkSectionB.messageType == MessageType.ERROR).isTrue
        assertThat(partnerAddressSubSection != null &&
                partnerAddressSubSection.subSectionMessages.any { preConditionCheckMessage ->
                    preConditionCheckMessage.message.i18nKey ==
                            "jems.standard.pre.condition.check.plugin.project.section.b.error.project.partner.department.address.is.not.provided"
                }).isTrue
    }

}