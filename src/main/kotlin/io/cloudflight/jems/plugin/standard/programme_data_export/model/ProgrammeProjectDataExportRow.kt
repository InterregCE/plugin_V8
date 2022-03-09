package io.cloudflight.jems.plugin.standard.programme_data_export.model

import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectAssessmentEligibilityResultData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectAssessmentQualityResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerAddressData
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZonedDateTime


open class ProgrammeProjectDataExportRow(
    val callId: Long,
    val callName: String,
    private val callStartDate: ZonedDateTime,
    private val callEndDateStepOne: ZonedDateTime?,
    private val callEndDate: ZonedDateTime,
    val periodLength: Int?,

    val projectId: String,
    val projectAcronym: String?,
    val projectTitle: String?,
    val projectVersion: String,
    val projectStatus: ApplicationStatusData,
    val projectProgrammePriority: String?,
    val projectSpecificObjective: String?,
    val startData: ZonedDateTime? = null, // for now the value is always null
    val endData: ZonedDateTime? = null, // for now the value is always null
    val projectDuration: Int?,
    val numberOfPeriods: Int?,
    val fundInfoList: List<FundInfo>,
    val publicContribution: BigDecimal,
    val autoPublicContribution: BigDecimal,
    val privateContribution: BigDecimal,
    val totalEligibleBudget: BigDecimal,

    val staffCostTotals: BudgetTotalCostInfo,
    val officeCostTotals: BigDecimal,
    val officeCostFlatRatesTotals: BigDecimal,
    val travelCostTotals: BudgetTotalCostInfo,
    val externalCostTotals: GeneralBudgetTotalCostInfo,
    val equipmentCostTotals: GeneralBudgetTotalCostInfo,
    val infrastructureCostTotals: GeneralBudgetTotalCostInfo,
    val otherCosts: BigDecimal,

    val unitCostsCoveringMultipleCostCategories: BigDecimal,
    val lumpSumsCoveringMultipleCostCategories: BigDecimal,
    val budgetPerPeriod: Map<Int, BigDecimal>,

    val leadPartnerOrganizationName: String?,
    val leadPartnerOrganizationNameInEnglish: String?,
    val leadPartnerMainAddressData: ProjectPartnerAddressData?,

    val submissionDateStep1: ZonedDateTime?,
    val firstSubmissionDate: ZonedDateTime?,
    val latestResubmissionDate: ZonedDateTime?,
    val contractedDate: ZonedDateTime?,

    val eligibilityDecisionStep1: ApplicationStatusData?,
    val eligibilityDecisionDateStep1: LocalDate?,
    val fundingDecisionStep1: ApplicationStatusData?,
    val fundingDecisionDateStep1: LocalDate?,
    val eligibilityAssessmentResultStep1: ProjectAssessmentEligibilityResultData?,
    val eligibilityAssessmentNotesStep1: String?,
    val qualityAssessmentResultStep1: ProjectAssessmentQualityResultData?,
    val qualityAssessmentNotesStep1: String?,

    val eligibilityDecision: ApplicationStatusData?,
    val eligibilityDecisionDate: LocalDate?,
    val fundingDecision: ApplicationStatusData?,
    val fundingDecisionDate: LocalDate?,
    val eligibilityAssessmentResult: ProjectAssessmentEligibilityResultData?,
    val eligibilityAssessmentNotes: String?,
    val qualityAssessmentResult: ProjectAssessmentQualityResultData?,
    val qualityAssessmentNotes: String?,
) {

    fun getTotalPartnerContribution() =
        publicContribution.plus(autoPublicContribution).plus(privateContribution)

    fun callStartDate() =
        truncateToMinutes(callStartDate)

    fun callEndDateStepOne() =
        truncateToMinutes(callEndDateStepOne)

    fun callEndDate() =
        truncateToMinutes(callEndDate)

    private fun truncateToMinutes(date: ZonedDateTime?) =
        date?.withSecond(0)?.withNano(0)
}
