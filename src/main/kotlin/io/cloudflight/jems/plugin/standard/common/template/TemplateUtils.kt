package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingByFundOverview
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.percentageDown
import io.cloudflight.jems.plugin.standard.common.percentageTo
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.IStandardExpression
import org.thymeleaf.standard.expression.IStandardExpressionParser
import org.thymeleaf.standard.expression.StandardExpressions
import java.math.BigDecimal

const val PROJECT_DATA = "projectData"
const val CALL_DATA = "callData"
const val DATA_LANGUAGE = "dataLanguage"
const val EXPORT_LANGUAGE = "exportLanguage"
const val CLF_UTILS = "clfUtils"

class TemplateUtils {
    fun sortPartnersBySortNumber(partners: Set<ProjectPartnerData>) =
        partners.sortedBy { it.sortNumber }

    fun getLeadPartner(partners: Set<ProjectPartnerData>) = partners.firstOrNull { it.role.isLead }

    fun sortAssociatedOrganizationsBySortNumber(associatedOrganizations: Set<ProjectAssociatedOrganizationData>) =
        associatedOrganizations.sortedBy { it.sortNumber }

    fun getMainAddress(addresses: List<ProjectPartnerAddressData>) =
        addresses.firstOrNull { it.type == ProjectPartnerAddressTypeData.Organization }

    fun getLegalRepresentative(contacts: List<ProjectPartnerContactData>) =
        contacts.firstOrNull { it.type === ProjectContactTypeData.LegalRepresentative }

    fun getContactPerson(contacts: List<ProjectPartnerContactData>) =
        contacts.firstOrNull { it.type === ProjectContactTypeData.ContactPerson }

    fun getDepartmentAddress(addresses: List<ProjectPartnerAddressData>) =
        addresses.firstOrNull { it.type == ProjectPartnerAddressTypeData.Department }

    fun getPublicContributionSubTotal(partnerContributions: Collection<ProjectPartnerContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Public }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getAutomaticPublicContributionSubTotal(partnerContributions: Collection<ProjectPartnerContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.AutomaticPublic }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getPrivateContributionSubTotal(partnerContributions: Collection<ProjectPartnerContributionData>) =
        partnerContributions.filter { it.status == ProjectPartnerContributionStatusData.Private }
            .sumOf { it.amount ?: BigDecimal.ZERO }

    fun getContributionTotal(partnerContributions: Collection<ProjectPartnerContributionData>) =
        partnerContributions.sumOf { it.amount ?: BigDecimal.ZERO }

    // it is used in the templates
    fun percentageDown(percentage: BigDecimal, total: BigDecimal) =
        total.percentageDown(percentage)

    // it is used in the templates
    fun percentageDownTo(amount: BigDecimal, total: BigDecimal) =
        amount.percentageTo(total)

    fun getEuFundsOverview(fundOverviews: List<ProjectCoFinancingByFundOverview>) =
        fundOverviews.filter { it.fundType != ProgrammeFundTypeData.OTHER }

    fun getOtherFundsOverview(fundOverviews: List<ProjectCoFinancingByFundOverview>) =
        fundOverviews.filter { it.fundType == ProgrammeFundTypeData.OTHER }

    fun getStateAidCheckResultTranslationKey(stateAidData: ProjectPartnerStateAidData) =
        if (stateAidData.answer1 == null || stateAidData.answer2 == null || stateAidData.answer3 == null || stateAidData.answer4 == null)
            "project.partner.state.aid.complete.form.first"
        else if (stateAidData.answer1 == true && stateAidData.answer2 == true && stateAidData.answer3 == true && stateAidData.answer4 == true)
            "project.partner.state.aid.risk.of.state.aid"
        else if ((stateAidData.answer1 == false || stateAidData.answer2 == false || stateAidData.answer3 == false) && stateAidData.answer4 == true)
            "project.partner.state.aid.risk.of.indirect.aid"
        else
            "project.partner.state.aid.no.risk.of.state.aid"

    fun getEnglishTranslation(translationData: Set<InputTranslationData>) =
        translationData.getTranslationFor(SystemLanguageData.EN)
}

fun <T> parseAttributeValue(attributeValue: String, context: ITemplateContext, defaultValue: T): T {
    val configuration: IEngineConfiguration = context.configuration
    val parser: IStandardExpressionParser = StandardExpressions.getExpressionParser(configuration)
    val expression: IStandardExpression = parser.parseExpression(context, attributeValue)
    return runCatching {
        expression.execute(context)?.let {
            it as T
        } ?: defaultValue
    }.getOrThrow()
}