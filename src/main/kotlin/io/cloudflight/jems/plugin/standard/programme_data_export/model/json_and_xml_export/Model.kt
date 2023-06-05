package io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA4.IndicatorOverviewLine
import io.cloudflight.jems.plugin.contract.models.project.sectionC.ProjectDataSectionC
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectRelevanceStrategyData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityDeliverableData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageInvestmentData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.BudgetCostsDetailData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.ProjectDataSectionD
import org.json.JSONObject
import org.json.XML
import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementData
import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementSubcontractData


data class Model(
        @field:JacksonXmlProperty(localName = "programme")
        @SerializedName("programme")
        val program: Programme
    ) {
        fun extractToJsonString(): String = Gson().toJson(this)
        fun extractToXmlStringOld(): String = XML.toString(JSONObject(Gson().toJson(this)))
        fun extractToXmlString(): String {
            val xmlMapper = XmlMapper()
            xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            return xmlMapper.writeValueAsString(this.program)
        }
    }

@JacksonXmlRootElement(localName = "programme")
    data class Programme(
        @field:JacksonXmlProperty(localName = "programme_name")
        @SerializedName("programme_name")
        val programmeName: String,
        @field:JacksonXmlProperty(localName = "period")
        @SerializedName("period")
        val period: String,
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "call")
        @SerializedName("call")
        val call: List<Call>? = null,
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "project")
        @SerializedName("project")
        val project: List<Project>? = null
    )

data class Call(
    @field:JacksonXmlProperty(localName = "serial_number")
    @SerializedName("serial_number") val serialNumber: String?,
    @field:JacksonXmlProperty(localName = "call_start_date")
    @SerializedName("call_start_date") val callStartDate: String?,
    @field:JacksonXmlProperty(localName = "call_end_date")
    @SerializedName("call_end_date") val callEndDate: String?
)

data class Project(
    @field:JacksonXmlProperty(localName = "project_id")
    @SerializedName("project_id") val projectId: String? = null,
    @field:JacksonXmlProperty(localName = "acronym")
    @SerializedName("acronym") val acronym: String? = null,
    @field:JacksonXmlProperty(localName = "call_serial_number")
    @SerializedName("call_serial_number") val callSerialNumber: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "multi_languages_name")
    @SerializedName("multi_languages_name") val multiLanguagesName: List<MultiLanguageName>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "multi_languages_description")
    @SerializedName("multi_languages_description") val multiLanguagesDescription: List<MultiLanguageDescription>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "multi_languages_expected_achievements")
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SerializedName("multi_languages_expected_achievements") val multiLangExpAchievements: List<List<MultiLanguagesExpectedAchievements>>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "multi_languages_actual_achievements")
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SerializedName("multi_languages_actual_achievements") val multiLangActAchievements: List<List<MultiLanguagesActualAchievements>>? = null,
    @field:JacksonXmlProperty(localName = "project_start_date") @SerializedName("project_start_date") val projectStartDate: String? = null,
    @field:JacksonXmlProperty(localName = "project_end_date") @SerializedName("project_end_date") val projectEndDate: String? = null,
    @field:JacksonXmlProperty(localName = "total_budget")  @SerializedName("total_budget") val totalBudget: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "contributing_eu_funds") @SerializedName("contributing_eu_funds") val contributingEuFunds: String? = null,
    @field:JacksonXmlProperty(localName = "programme_priority_code") @SerializedName("programme_priority_code")
    val programmePriority: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "multi_languages_programme_priority_name")
    @SerializedName("multi_languages_programme_priority_name") val multiLanguagesPriority: List<MultiLanguagePriorityName>? = null,
    @field:JacksonXmlProperty(localName = "priority_specific_objective")
    @SerializedName("priority_specific_objective") val prioritySpecificObjective: String? = null,
    @field:JacksonXmlProperty(localName = "eu_funding_amount")
    @SerializedName("eu_funding_amount") val euFundingAmount: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "eu_funding_cofinancing_rate")
    @SerializedName("eu_funding_cofinancing_rate") val euFundingCoFinancingRate: BigDecimal? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "investment_area")
    @SerializedName("investment_area") val infraInvestmentArea: List<GenericAddress>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "intervention_type")
    @SerializedName("intervention_type") val interventionType: List<String>? = null,
    @field:JacksonXmlProperty(localName = "strategic_or_above5m")
    @SerializedName("strategic_or_above5m") val strategicOrAbove5m: Boolean = false,
    @field:JacksonXmlProperty(localName = "technical_assistance_project")
    @SerializedName("technical_assistance_project") val technicalAssistanceProject: Boolean = false,
    @field:JacksonXmlProperty(localName = "small_project")
    @SerializedName("small_project") val smallProject: Boolean = false,
    @field:JacksonXmlProperty(localName = "website")
    @SerializedName("website") val webSite: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "funds_cofinancing_amounts")
    @SerializedName("funds_cofinancing_amounts") val fundsCoFinancingAmount: List<FundsCoFinancingAmount>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "ERDF_equivalents")
    @SerializedName("ERDF_equivalents") val fundERDFEquivalents: List<ERDFEquivalent>? = null,
    @field:JacksonXmlProperty(localName = "rel_precedent_projects")
    @SerializedName("rel_precedent_projects") val relPrecedentProjects: String? = null,
    @field:JacksonXmlProperty(localName = "rel_subsequent_projects")
    @SerializedName("rel_subsequent_projects") val relSubSequentProjects: String? = null,
    @field:JacksonXmlProperty(localName = "linked_projects")
    @SerializedName("linked_projects") val linkedProjects: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "deliverables")
    @SerializedName("deliverables") val deliverables: List<Deliverable.DeliverableItem>? = null,
    @field:JacksonXmlProperty(localName = "investment_infrastructure_number")
    @SerializedName("investment_infrastructure_number") val investmentInfrastructureNumbers: Int? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "investment_infrastructure_name")
    @SerializedName("investment_infrastructure_name")
    val investmentInfrastructureName: List<InvestmentBuilder.InvestmentName>? = null,
    @field:JacksonXmlProperty(localName = "investment_infrastructure_cost")
    @SerializedName("investment_infrastructure_cost") val investmentInfrastructureCost: BigDecimal? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "contribution_wider_strategies_and_policies")
    @SerializedName("contribution_wider_strategies_and_policies")
    val contributionWiderStrategiesAndPolicies: List<ContributionWiderStrategiesAndPolicies>? = null,
    @field:JacksonXmlProperty(localName = "relevant_mentions_and_prizes")
    @SerializedName("relevant_mentions_and_prizes") val relevantMentionAndPrice: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "project_indicators")
    @SerializedName("project_indicators") val projectIndicators: List<ProjectIndicator?>? = null,
    @field:JacksonXmlProperty(localName = "project_hierarchy")
    @SerializedName("project_hierarchy") val projectHierarchy: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "partner")
    @SerializedName("partner") val partner: List<Partner>? = null,
) {
    class InvestmentBuilder private constructor() {
        var sectionC: ProjectDataSectionC? = null
        var sectionD: ProjectDataSectionD? = null
        val investments = ArrayList<WorkPackageInvestmentData>()
        val budgetCostsDetailData = ArrayList<BudgetCostsDetailData?>()

        data class InvestmentName(
            @SerializedName("description") val description: String,
            @SerializedName("language") val language: String
        )

        fun backCost(): BigDecimal? {
            var back: BigDecimal? = null
            budgetCostsDetailData.forEach {
                it?.infrastructureCosts?.let { cost ->
                    back = if (back == null)
                        cost
                    else
                        back!! + cost
                }
            }
            return back
        }

        fun backNumber(): Int {
            var back = 0
            investments.forEach {
                back += it.investmentNumber
            }
            return back
        }

        fun backInvestmentNameList(): List<InvestmentName> {
            val back = ArrayList<InvestmentName>()
            investments.forEach {
                back.addAll(it.title.toListOf<InvestmentName>())
            }
            return back.toList()
        }

        companion object {
            fun withSectionC(sectionC: ProjectDataSectionC) = InvestmentBuilder().apply {
                this.sectionC = sectionC
                sectionC.projectWorkPackages.forEach {
                    //it.workPackageNumber da concatenare I workPackageNumber + investmentNumber
                    this.investments.addAll(it.investments)
                }
            }

            fun withSectionD(sectionD: ProjectDataSectionD) = InvestmentBuilder().apply {
                this.sectionD = sectionD
                sectionD.projectPartnerBudgetPerPeriodData.partnersBudgetPerPeriod.forEach {
                    this.budgetCostsDetailData.add(it.totalPartnerBudgetDetail)
                }
            }
        }
    }

    class Deliverable private constructor() {
        var sectionC: ProjectDataSectionC? = null
        var activities = ArrayList<WorkPackageActivityData>()

        data class DeliverableItem(
            @SerializedName("description") val description: String,
            @SerializedName("language") val language: String
        )

        fun buildDeliverableList(): List<DeliverableItem> {
            val back = ArrayList<DeliverableItem>()
            val listDeliverable = ArrayList<WorkPackageActivityDeliverableData>()
            this.activities.forEach {
                listDeliverable.addAll(it.deliverables)
            }
            listDeliverable.forEach {
                back.addAll(it.title.toListOf<DeliverableItem>())
            }
            return back.toList()
        }

        companion object {
            fun withSectionC(sectionC: ProjectDataSectionC) = Deliverable().apply {
                this.sectionC = sectionC
                sectionC.projectWorkPackages.forEach {
                    this.activities.addAll(it.activities)
                }
            }
        }
    }

    data class MultiLanguageName(
        @SerializedName("name") val name: String,
        @SerializedName("language") val language: String
    )

    data class MultiLanguageDescription(
        @field:JacksonXmlProperty(localName = "description")
        @SerializedName("description") val description: String,
        @field:JacksonXmlProperty(localName = "language")
        @SerializedName("language") val language: String
    )

    data class MultiLanguagesExpectedAndActualAchievements(
        @field:JacksonXmlProperty(localName = "expected_and_actual_achievements")
        @SerializedName("expected_and_actual_achievements") val expectedAndActualAchievements: String,
        @field:JacksonXmlProperty(localName = "language")
        @SerializedName("language") val language: String? = null
    )
    data class MultiLanguagesExpectedAchievements(
        @field:JacksonXmlProperty(localName = "expected_achievements")
        @SerializedName("expected_achievements") val expectedAchievements: String,
        @field:JacksonXmlProperty(localName = "language") @SerializedName("language") val language: String
    )
    data class MultiLanguagesActualAchievements(
        @field:JacksonXmlProperty(localName = "actual_achievements")
        @SerializedName("actual_achievements") val actualAchievements: String,
        @field:JacksonXmlProperty(localName = "language")
        @SerializedName("language") val language: String? = null
    )

    data class MultiLanguagePriorityName(
        @field:JacksonXmlProperty(localName = "name") @SerializedName("name") val name: String,
        @field:JacksonXmlProperty(localName = "language") @SerializedName("language") val language: String? = null
    )

    data class FundsCoFinancingAmount(
        @field:JacksonXmlProperty(localName = "fund")
        @SerializedName("fund") val fund: String?,
        @field:JacksonXmlProperty(localName = "amount")
        @SerializedName("amount") val amount: BigDecimal?,
        @field:JacksonXmlProperty(localName = "cofinancing_rate")
        @SerializedName("cofinancing_rate") val coFinancingRate: BigDecimal?
    )

    data class ERDFEquivalent(
        @field:JacksonXmlProperty(localName = "source")
        @SerializedName("source") val source: String?,
        @field:JacksonXmlProperty(localName = "amount")
        @SerializedName("amount") val amount: BigDecimal?,
        @field:JacksonXmlProperty(localName = "cofinancing_rate")
        @SerializedName("cofinancing_rate") val coFinancingRate: BigDecimal?
    )

    data class ContributionWiderStrategiesAndPolicies(
        @field:JacksonXmlProperty(localName = "strategy")
        @SerializedName("strategy") val strategy: String?,
        @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "contribution")
        @SerializedName("contribution") val contribution: List<GenericMultiLanguage>?
    ) {
        companion object {
            fun from(list: List<ProjectRelevanceStrategyData>?): List<ContributionWiderStrategiesAndPolicies> {
                val back = ArrayList<ContributionWiderStrategiesAndPolicies>()
                list?.forEach {
                    back.add(
                        ContributionWiderStrategiesAndPolicies(
                            it.strategy?.name,
                            it.specification.toListOf<GenericMultiLanguage>()
                        )
                    )
                }
                return back.toList()
            }
        }
    }

    data class ProjectIndicator(
        @field:JacksonXmlProperty(localName = "output_indicator_code")
        @SerializedName("output_indicator_code") val outputIndicatorCode: String?,
        @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "output_indicator_name")
        @SerializedName("output_indicator_name") val outputIndicatorName: List<GenericMultiLanguage>?,
        @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "output_indicator_title")
        @SerializedName("output_indicator_title") val outputIndicatorTitle: List<GenericMultiLanguage>?,
        @field:JacksonXmlProperty(localName = "output_indicator_delivered")
        @SerializedName("output_indicator_delivered") val outputIndicatorDelivered: BigDecimal?,
        @field:JacksonXmlProperty(localName = "result_indicator_code")
        @SerializedName("result_indicator_code") val resultIndicatorCode: String?,
        @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "result_indicator_name")
        @SerializedName("result_indicator_name") val resultIndicatorName: List<GenericMultiLanguage>?,
        @field:JacksonXmlProperty(localName = "result_indicator_delivered")
        @SerializedName("result_indicator_delivered") val resultIndicatorDelivered: BigDecimal?
    ) {
        companion object {
            fun fromIndicatorOverviewLine(indicatorOverview: IndicatorOverviewLine?): ProjectIndicator? {
                if (indicatorOverview == null) return null
                return ProjectIndicator(
                    if (indicatorOverview.outputIndicatorIdentifier != null)
                        "${indicatorOverview.outputIndicatorIdentifier}"
                    else null,
                    indicatorOverview.outputIndicatorName?.toListOf<GenericMultiLanguage>(),
                    indicatorOverview.projectOutputTitle?.toListOf<GenericMultiLanguage>(),
                    indicatorOverview.outputIndicatorTargetValueSumUp,
                    if (indicatorOverview.resultIndicatorIdentifier != null)
                        "${indicatorOverview.outputIndicatorIdentifier}"
                    else null,
                    indicatorOverview.resultIndicatorName?.toListOf<GenericMultiLanguage>(),
                    indicatorOverview.resultIndicatorTargetValueSumUp
                )
            }
        }
    }
}

data class Partner(
    @field:JacksonXmlProperty(localName = "partner_type")
    @SerializedName("partner_type") val partnerType: String? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JacksonXmlProperty(localName = "multi_languages_organisation_name")
    @SerializedName("multi_languages_organisation_name")
    val multiLanguageOrganisationName: List<GenericMultiLanguage>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "contractors")
    @SerializedName("contractors") val contractors: List<Contractor>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "sub_contractors")
    @SerializedName("sub_contractors") val subContractors: List<SubContractor>? = null,
    @field:JacksonXmlElementWrapper(useWrapping = false)  @field:JacksonXmlProperty(localName = "address")
    @SerializedName("address") val address: List<Address>? = null,
    @field:JacksonXmlProperty(localName = "participant_identification_code")
    @SerializedName("participant_identification_code") val participantIdentificationCode: String? = null,
    @field:JacksonXmlProperty(localName = "participant_id_no_pic")
    @SerializedName("participant_id_no_pic") val participantIdNoPic: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) @field:JacksonXmlProperty(localName = "organisation_type")
    @SerializedName("organisation_type") val organisationType: String? = null,
    @field:JacksonXmlProperty(localName = "website")
    @SerializedName("website") val webSite: String? = null,
    @field:JacksonXmlProperty(localName = "legal_status")
    @SerializedName("legal_status") val legalStatus: String? = null,
    @field:JacksonXmlProperty(localName = "mail")
    @SerializedName("mail") val mail: String? = null,
    @field:JacksonXmlProperty(localName = "total_eligible_budget")
    @SerializedName("total_eligible_budget") val totalEligibleBudget: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "cofinancing_amount")
    @SerializedName("cofinancing_amount") val coFinancingAmount: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "cofinancing_rate")
    @SerializedName("cofinancing_rate") val coFinancingRate: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "partner_contribution")
    @SerializedName("partner_contribution") val partnerContribution: BigDecimal? = null,
    @field:JacksonXmlProperty(localName = "GBER_schemes_de_minimis")
    @SerializedName("GBER_schemes_de_minimis") val gberSchemeDeMinimis: String? = null
) {
    data class Address(
        @field:JacksonXmlProperty(localName = "main_or_department")
        @SerializedName("main_or_department") val mainOrDepartment: String?,
        @field:JacksonXmlProperty(localName = "country")
        @SerializedName("country") val country: String?,
        @field:JacksonXmlProperty(localName = "town")
        @SerializedName("town") val town: String?,
        @field:JacksonXmlProperty(localName = "street")
        @SerializedName("street") val street: String?,
        @field:JacksonXmlProperty(localName = "postal_code")
        @SerializedName("postal_code") val postCode: String?,
        @field:JacksonXmlElementWrapper(useWrapping = false) @field:JacksonXmlProperty(localName = "department")
        @SerializedName("department") val department: List<GenericMultiLanguage>? = null
    ) {
        enum class MainOrDepartment {
            Main,
            Department
        }
    }

    data class Contractor(
            @field:JacksonXmlProperty(localName = "contractor_name")
            @SerializedName("contractor_name") val contractorName: String,
            @field:JacksonXmlProperty(localName = "contractor_vat")
            @SerializedName("contractor_vat") val contractorVat: String
    ) {
        companion object {
            fun listFrom(list: List<ProjectPartnerReportProcurementData>): List<Contractor> {
                val back = ArrayList<Contractor>()
                list.forEach {
                    back.add(Contractor(it.supplierName, it.vatNumber))
                }
                return back.toList()
            }
        }
    }

    data class SubContractor(
            @field:JacksonXmlProperty(localName = "sub_contractor_name")
            @SerializedName("sub_contractor_name") val subContractorName: String,
            @field:JacksonXmlProperty(localName = "sub_contractor_vat")
            @SerializedName("sub_contractor_vat") val subContractorVat: String
    ) {
        companion object {
            fun listFrom(list: List<ProjectPartnerReportProcurementSubcontractData>): List<SubContractor> {
                val back = ArrayList<SubContractor>()
                list.forEach {
                    back.add(SubContractor(it.supplierName, it.vatNumber))
                }
                return back.toList()
            }
        }
    }
}

data class GenericMultiLanguage(
    @field:JacksonXmlProperty(localName = "name")
    @SerializedName("name") val organisationName: String,
    @field:JacksonXmlProperty(localName = "language")
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @SerializedName("language") val language: String? = null
)

data class GenericAddress(
    @field:JacksonXmlProperty(localName = "country")
    @SerializedName("country")
    val country: String?,
    @field:JacksonXmlProperty(localName = "town")
    @SerializedName("town")
    val city: String?,
    @field:JacksonXmlProperty(localName = "street")
    @SerializedName("street")
    val street: String?,
    @field:JacksonXmlProperty(localName = "house_number")
    @SerializedName("house_number")
    val houseNumber: String?,
    @field:JacksonXmlProperty(localName = "postal_code")
    @SerializedName("postal_code")
    val postalCode: String?
)








































