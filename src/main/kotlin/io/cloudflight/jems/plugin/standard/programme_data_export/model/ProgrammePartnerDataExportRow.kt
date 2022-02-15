package io.cloudflight.jems.plugin.standard.programme_data_export.model

import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData

import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import java.math.BigDecimal

open class ProgrammePartnerDataExportRow(
    val callId: Long,
    val callName: String,

    val projectId: String,
    val projectAcronym: String?,
    val projectTitle: String?,
    val projectVersion: String,
    val projectStatus: ApplicationStatusData,
    val projectProgrammePriority: String?,
    val projectSpecificObjective: String?,

    val partnerNumber: String?,
    val partnerRole: ProjectPartnerRoleData?,
    val partnerAbbreviation: String?,
    val partnerNameInOriginalLanguage: String?,
    val partnerNameInEnglish: String?,
    val partnerCountry: String?,
    val partnerNuts3: String?,
    val partnerNuts2: String?,
    val fundInfoList: List<FundInfo>,
    val publicContribution: BigDecimal,
    val automaticPublicContribution: BigDecimal,
    val privateContribution: BigDecimal,
    val totalEligibleBudget: BigDecimal,
    val totalEligibleBudgetPercentage: BigDecimal,
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
    val partnerBudgetPerPeriod: Map<Int, BigDecimal>,

    val department: String?,
    val partnerType: ProjectTargetGroupData?,
    val partnerSubType: PartnerSubTypeData?,
    val legalStatus: String?,
    val sectorOfActivityAtNace: NaceGroupLevelData?,
    val vat: String?,
    val vatRecovery: ProjectPartnerVatRecoveryData?,
    val otherIdentifierNumber: String?,
    val otherIdentifierDescription: String?,
    val pic: String?,
    val stateAidCheck: String?,
    val stateAidScheme: String?,
    val mainAddress: PartnerAddress?,
    val departmentAddress: PartnerAddress?,
    val legalRepresentativeContact: PartnerContact?,
    val contactPartner: PartnerContact?

) {
    fun getTotalPartnerContribution() =
        publicContribution.plus(automaticPublicContribution).plus(privateContribution)
}
