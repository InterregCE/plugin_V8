package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectPeriodData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingByFundOverview
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA4.IndicatorOverviewLine
import io.cloudflight.jems.plugin.contract.models.project.sectionB.associatedOrganisation.ProjectAssociatedOrganizationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.standard.common.*
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.IStandardExpression
import org.thymeleaf.standard.expression.IStandardExpressionParser
import org.thymeleaf.standard.expression.StandardExpressions
import java.math.BigDecimal
import kotlin.collections.sumOf

const val PROJECT_DATA = "projectData"
const val CALL_DATA = "callData"
const val DATA_LANGUAGE = "dataLanguage"
const val EXPORT_LANGUAGE = "exportLanguage"
const val CLF_UTILS = "clfUtils"
const val MAX_INDICATOR_ID_FROM_DB = 1_000_000_000_000
const val MAX_INDICATOR_FAKE_ID = 3_000_000_000_000

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

    fun getLinesSorted(resultLines: List<IndicatorOverviewLine>) =
        transformIdsOfIndicatorsToRowIds(resultLines)
            .sortedWith(compareBy({it.resultIndicatorId}, {it.outputIndicatorId}))

    fun getResultIndicatorSpan(data: List<IndicatorOverviewLine>) =
        getRowSpanPlanForIndicatorOverviewLines(listOf("resultIndicatorId"), data)

    fun getOutputIndicatorSpan(data: List<IndicatorOverviewLine>) =
        getRowSpanPlanForIndicatorOverviewLines(listOf("resultIndicatorId","outputIndicatorId"), data)

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

    fun getPeriod(periodNumber: Number, periods: List<ProjectPeriodData>): ProjectPeriodData? =
        periods.filter { it.number == periodNumber }.firstOrNull()

    fun isSectionCAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isSectionCVisible(lifecycleData, callData)

    fun isSectionC2Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectRelevanceSectionVisible(lifecycleData, callData)

    fun isSectionC4Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectWorkPackageSectionVisible(lifecycleData, callData)

    fun isProjectResultsSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectResultsSectionVisible(lifecycleData, callData)

    fun isLongTermPlansSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData)=
        isLongTermPlansSectionVisible(lifecycleData, callData)

    private fun transformIdsOfIndicatorsToRowIds(data: List<IndicatorOverviewLine>): List<IndicatorOverviewLine> =
        data.mapIndexed {
                index, indicatorOverviewLine ->
            generateIdsForNotExistingIds(indicatorOverviewLine, index) }

    fun isInvestmentLocationAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isInvestmentLocationVisible(lifecycleData, callData)

    fun getPartnerTypeTranslationString(partnerType: ProjectTargetGroupData?): String {
        if (partnerType != null) {
            return "project.application.form.relevance.target.group.$partnerType"
        }
        return ""
    }

    fun getPartnerSubTypeTranslationString(partnerSubType: PartnerSubTypeData?): String {
        if (partnerSubType != null) {
            return "project.application.form.relevance.target.group.$partnerSubType"
        }
        return ""
    }

    fun getPartnerNaceTranslationString(nace: NaceGroupLevelData?): String {
        if (nace != null) {
            return nace.toString().replace('_', '.')
        }
        return ""
    }

    fun getPartnerListForActivity(activityPartnerIds:Set<Long>, partners:Set<ProjectPartnerData>): Set<ProjectPartnerData> =
        partners.filter { activityPartnerIds.contains(it.id) }.toSet()

    fun isOutputsSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isWorkPlanOutputsSectionVisible(lifecycleData, callData)

    fun isActivitiesSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isWorkPlanActivitiesSectionVisible(lifecycleData, callData)

    private fun generateIdsForNotExistingIds(
        indicatorOverviewLine: IndicatorOverviewLine,
        index: Int
    ): IndicatorOverviewLine {
        if (indicatorOverviewLine.onlyResultWithoutOutputs) {
            return indicatorOverviewLine.copy(
                outputIndicatorId = MAX_INDICATOR_FAKE_ID + index,
                resultIndicatorId = MAX_INDICATOR_FAKE_ID + index,
            )
        }

        val outputIndicatorId = indicatorOverviewLine.outputIndicatorId ?: (MAX_INDICATOR_ID_FROM_DB + index)

        return indicatorOverviewLine.copy(
            outputIndicatorId = outputIndicatorId,
            resultIndicatorId = indicatorOverviewLine.resultIndicatorId ?: (MAX_INDICATOR_ID_FROM_DB + outputIndicatorId)
        )
    }

    private fun getRowSpanPlanForIndicatorOverviewLines(attributes: List<String>, data: List<IndicatorOverviewLine>): List<Long> {

        // if we do not go deeper, stop recursion and fill row-spans for this group
        if (attributes.isEmpty()) {
            return data.mapIndexed { index, _ -> if (index == 0) data.size.toLong() else 0}
        }

        val attributesToFollow = attributes.subList(1, attributes.size)
        val attribute = attributes.first()
        val uniqueRowsWithinGroup = data.associateBy { getValueForIndicatorIds(attribute, it) }.values

        return uniqueRowsWithinGroup
            .map {
                this.getRowSpanPlanForIndicatorOverviewLines(
                    attributesToFollow,
                    data.filter {
                            item -> this.getValueForIndicatorIds(attribute, it) == this.getValueForIndicatorIds(attribute, item)
                    })
            }
            .flatten()
    }

    private fun getValueForIndicatorIds(attribute: String, from: IndicatorOverviewLine): Long? =
        when (attribute) {
            "outputIndicatorId" ->
                from.outputIndicatorId;
            "resultIndicatorId" ->
                from.resultIndicatorId;
            else ->
                -1
        }


}

fun <T> parseAttributeValue(attributeValue: String, context: ITemplateContext, defaultValue: T): T =
    parseAttributeValue(attributeValue, context)?.let {
            it as T
        } ?: defaultValue

fun parseAttributeValue(attributeValue: String, context: ITemplateContext): Any? {
    val configuration: IEngineConfiguration = context.configuration
    val parser: IStandardExpressionParser = StandardExpressions.getExpressionParser(configuration)
    val expression: IStandardExpression = parser.parseExpression(context, attributeValue)
    return expression.execute(context)
}
