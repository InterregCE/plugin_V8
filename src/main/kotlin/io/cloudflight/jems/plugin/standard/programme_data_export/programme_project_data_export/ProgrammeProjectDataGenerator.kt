package io.cloudflight.jems.plugin.standard.programme_data_export.programme_project_data_export

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectAssessmentEligibilityResultData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectAssessmentQualityResultData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerContributionStatusData
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProgrammeProjectDataExportRow
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProjectAndCallData
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

open class ProgrammeProjectDataGenerator(
    private val projectAndCallDataList: List<ProjectAndCallData>,
    private val failedProjectIds: Set<Long>,
    private val programmeInfoData: ProgrammeInfoData,
    private val exportationDateTime: ZonedDateTime,
    private val exportLanguage: SystemLanguageData,
    private val dataLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {
    private val exportLocale = exportLanguage.toLocale()
    private val maximalPeriods =
        projectAndCallDataList.map { it.projectData.sectionA?.periods ?: emptyList() }.maxByOrNull { it.size }
            ?.map { it.number }
            ?.toMutableList().also {
                it?.add(0, PREPARATION_PERIOD)
                it?.add(CLOSURE_PERIOD)
            } ?: emptyList()

    fun getData() =
        ExcelData().also {
            it.addSheet("Project data").also { sheet ->
                sheet.addRow(CellData(getFileTitle(programmeInfoData.title, exportationDateTime)))
                sheet.addRow(*getHeaderRow(maximalPeriods, programmeInfoData.funds, exportLanguage, messageSource))
                    .borderTop()
                sheet.addRows(getRows(generateProgrammeProjectDataExportRows()))
                sheet.data.lastOrNull()?.borderBottom()
            }
            if (failedProjectIds.isNotEmpty())
                it.addSheet("Failed projects").also { sheet ->
                    sheet.addRow(
                        getMessage(
                            "project.application.form.field.project.id", exportLocale, messageSource
                        ).toErrorCellData()
                    )
                    sheet.addRows(failedProjectIds.map { projectId -> arrayOf(projectId.toErrorCellData()) })
                }
        }

    private fun getFileTitle(programmeTitle: String?, exportationDateTime: ZonedDateTime) =
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle} - project data - ${
            exportationDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm:ss"))
        }"

    private fun getRows(data: List<ProgrammeProjectDataExportRow>): List<Array<CellData>> =
        data.map { row ->
            mutableListOf<CellData>().also {
                it.add(row.callId.toCallCellData().borderLeft())
                it.add(row.callName.toCallCellData())
                it.add(row.callStartDate().toCallCellData())
                it.add(row.callEndDateStepOne().toCallCellData())
                it.add(row.callEndDate().toCallCellData())
                it.add(row.periodLength.toCallCellData())

                it.add(row.projectId.toProjectCellData())
                it.add(row.projectAcronym.toProjectCellData())
                it.add(row.projectTitle.toProjectCellData())
                it.add(row.projectVersion.toProjectCellData())
                it.add(row.projectStatus.toMessage().toProjectCellData())
                it.add(row.projectProgrammePriority.toProjectCellData())
                it.add(row.projectSpecificObjective.toProjectCellData())
                it.add(row.projectDuration.toProjectCellData())
                it.add(row.numberOfPeriods.toProjectCellData())
                it.addAll(row.fundInfoList.flatMap { fund ->
                    listOf(
                        fund.fundAmount.toProjectCellData(),
                        fund.fundPercentage.toProjectCellData(),
                        fund.percentageOfTotal.toProjectCellData()
                    )
                })
                it.add(row.publicContribution.toProjectCellData())
                it.add(row.autoPublicContribution.toProjectCellData())
                it.add(row.privateContribution.toProjectCellData())
                it.add(row.getTotalPartnerContribution().toProjectCellData())
                it.add(row.totalEligibleBudget.toProjectCellData())
                it.add(row.staffCostTotals.total.toProjectCellData())
                it.add(row.staffCostTotals.flatRateTotal.toProjectCellData())
                it.add(row.staffCostTotals.realCostTotal.toProjectCellData())
                it.add(row.staffCostTotals.unitCostTotal.toProjectCellData())
                it.add(row.officeCostTotals.toProjectCellData())
                it.add(row.officeCostFlatRatesTotals.toProjectCellData())
                it.add(row.travelCostTotals.total.toProjectCellData())
                it.add(row.travelCostTotals.flatRateTotal.toProjectCellData())
                it.add(row.travelCostTotals.realCostTotal.toProjectCellData())
                it.add(row.travelCostTotals.unitCostTotal.toProjectCellData())
                it.add(row.externalCostTotals.getTotal().toProjectCellData())
                it.add(row.externalCostTotals.realCostTotal.toProjectCellData())
                it.add(row.externalCostTotals.unitCostTotal.toProjectCellData())
                it.add(row.equipmentCostTotals.getTotal().toProjectCellData())
                it.add(row.equipmentCostTotals.realCostTotal.toProjectCellData())
                it.add(row.equipmentCostTotals.unitCostTotal.toProjectCellData())
                it.add(row.infrastructureCostTotals.getTotal().toProjectCellData())
                it.add(row.infrastructureCostTotals.realCostTotal.toProjectCellData())
                it.add(row.infrastructureCostTotals.unitCostTotal.toProjectCellData())
                it.add(row.otherCosts.toProjectCellData())
                it.add(row.unitCostsCoveringMultipleCostCategories.toProjectCellData())
                it.add(row.lumpSumsCoveringMultipleCostCategories.toProjectCellData())
                it.addAll(maximalPeriods.map { periodNumber ->
                    row.budgetPerPeriod.entries.firstOrNull { it.key == periodNumber }?.let {
                        it.value.toProjectCellData()
                    } ?: BigDecimal.ZERO.toProjectCellData()
                })
                it.add(row.leadPartnerOrganizationName.toProjectCellData())
                it.add(row.leadPartnerOrganizationNameInEnglish.toProjectCellData())
                it.add(row.leadPartnerMainAddressData?.country.toProjectCellData())
                it.add(row.leadPartnerMainAddressData?.nutsRegion2.toProjectCellData())
                it.add(row.leadPartnerMainAddressData?.nutsRegion3.toProjectCellData())
                it.add(row.submissionDateStep1.toProjectCellData())
                it.add(row.firstSubmissionDate.toProjectCellData())
                it.add(row.latestResubmissionDate.toProjectCellData())

                it.add(row.eligibilityDecisionStep1.toMessage().toAssessmentCellData())
                it.add(row.eligibilityDecisionDateStep1.toAssessmentCellData())
                it.add(row.fundingDecisionStep1.toMessage().toAssessmentCellData())
                it.add(row.fundingDecisionDateStep1.toAssessmentCellData())
                it.add(row.eligibilityAssessmentResultStep1.toMessage().toAssessmentCellData())
                it.add(row.eligibilityAssessmentNotesStep1.toAssessmentCellData())
                it.add(row.qualityAssessmentResultStep1.toMessage().toAssessmentCellData())
                it.add(row.qualityAssessmentNotesStep1.toAssessmentCellData())
                it.add(row.eligibilityDecision.toMessage().toAssessmentCellData())
                it.add(row.eligibilityDecisionDate.toAssessmentCellData())
                it.add(row.fundingDecision.toMessage().toAssessmentCellData())
                it.add(row.fundingDecisionDate.toAssessmentCellData())
                it.add(row.eligibilityAssessmentResult.toMessage().toAssessmentCellData())
                it.add(row.eligibilityAssessmentNotes.toAssessmentCellData())
                it.add(row.qualityAssessmentResult.toMessage().toAssessmentCellData())
                it.add(row.qualityAssessmentNotes.toAssessmentCellData().borderRight())
            }.toTypedArray()
        }

    private fun generateProgrammeProjectDataExportRows(): List<ProgrammeProjectDataExportRow> =
        projectAndCallDataList
            .sortedWith(compareBy({ it.callDetailData.id }, { it.projectData.sectionA?.customIdentifier }))
            .map { entry ->
                val projectVersion = entry.projectVersion
                val projectData = entry.projectData
                val callDetailData = entry.callDetailData
                ProgrammeProjectDataExportRow(
                    callId = callDetailData.id,
                    callName = callDetailData.name,
                    callStartDate = callDetailData.startDateTime,
                    callEndDateStepOne = callDetailData.endDateTimeStep1,
                    callEndDate = callDetailData.endDateTime,
                    periodLength = callDetailData.lengthOfPeriod,
                    projectId = projectData.sectionA?.customIdentifier!!,
                    projectAcronym = projectData.sectionA?.acronym,
                    projectTitle = projectData.sectionA?.title?.getTranslationFor(dataLanguage),
                    projectVersion = projectVersion.version,
                    projectStatus = projectData.lifecycleData.status,
                    projectProgrammePriority = projectData.sectionA?.programmePriority?.code,
                    projectSpecificObjective = projectData.sectionA?.specificObjective?.code,
                    projectDuration = projectData.sectionA?.duration,
                    numberOfPeriods = projectData.sectionA?.periods?.size,
                    fundInfoList = getFundInfo(projectData, programmeInfoData.funds),
                    publicContribution = sumOfContributionByType(
                        projectData,
                        ProjectPartnerContributionStatusData.Public
                    ),
                    autoPublicContribution = sumOfContributionByType(
                        projectData, ProjectPartnerContributionStatusData.AutomaticPublic
                    ),
                    privateContribution = sumOfContributionByType(
                        projectData, ProjectPartnerContributionStatusData.Private
                    ),
                    totalEligibleBudget = projectData.sectionD.projectPartnerBudgetPerFundData.firstOrNull { it.partner == null }?.totalEligibleBudget
                        ?: BigDecimal.ZERO,
                    staffCostTotals = getStaffCostTotals(projectData.sectionB.partners),
                    officeCostTotals = projectData.sectionB.partners
                        .map { it.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts }.sumOf { it },
                    officeCostFlatRatesTotals = projectData.sectionB.partners
                        .map { it.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts }.sumOf { it },
                    travelCostTotals = getTravelCostTotals(projectData.sectionB.partners),
                    externalCostTotals = getGeneralBudgetCostTotalsFor(projectData.sectionB.partners
                        .flatMap { it.budget.projectPartnerBudgetCosts.externalCosts }),
                    equipmentCostTotals = getGeneralBudgetCostTotalsFor(projectData.sectionB.partners
                        .flatMap { it.budget.projectPartnerBudgetCosts.equipmentCosts }),
                    infrastructureCostTotals = getGeneralBudgetCostTotalsFor(projectData.sectionB.partners
                        .flatMap { it.budget.projectPartnerBudgetCosts.infrastructureCosts }),
                    otherCosts = projectData.sectionB.partners.map { it.budget.projectBudgetCostsCalculationResult.otherCosts }
                        .sumOf { it },
                    unitCostsCoveringMultipleCostCategories = projectData.sectionB.partners
                        .flatMap { it.budget.projectPartnerBudgetCosts.unitCosts }
                        .sumOf { it.rowSum ?: BigDecimal.ZERO },
                    lumpSumsCoveringMultipleCostCategories = projectData.sectionE.projectLumpSums
                        .flatMap { it.lumpSumContributions }.sumOf { it.amount },
                    budgetPerPeriod = getBudgetPerPeriod(projectData),
                    leadPartnerOrganizationName = projectData.sectionB.partners.firstOrNull { it.role.isLead }?.nameInOriginalLanguage,
                    leadPartnerOrganizationNameInEnglish = projectData.sectionB.partners.firstOrNull { it.role.isLead }?.nameInEnglish,
                    leadPartnerMainAddressData = getLeadPartnerMainAddress(projectData),
                    submissionDateStep1 = projectData.lifecycleData.submissionDateStepOne,
                    firstSubmissionDate = projectData.lifecycleData.firstSubmissionDate,
                    latestResubmissionDate = projectData.lifecycleData.lastResubmissionDate,
                    eligibilityDecisionStep1 = projectData.lifecycleData.assessmentStep1?.eligibilityDecision?.status,
                    eligibilityDecisionDateStep1 = projectData.lifecycleData.assessmentStep1?.eligibilityDecision?.decisionDate,
                    fundingDecisionStep1 = projectData.lifecycleData.assessmentStep1?.fundingDecision?.status,
                    fundingDecisionDateStep1 = projectData.lifecycleData.assessmentStep1?.fundingDecision?.decisionDate,
                    eligibilityAssessmentResultStep1 = projectData.lifecycleData.assessmentStep1?.assessmentEligibility?.result,
                    eligibilityAssessmentNotesStep1 = projectData.lifecycleData.assessmentStep1?.assessmentEligibility?.note,
                    qualityAssessmentResultStep1 = projectData.lifecycleData.assessmentStep1?.assessmentQuality?.result,
                    qualityAssessmentNotesStep1 = projectData.lifecycleData.assessmentStep1?.assessmentQuality?.note,
                    eligibilityDecision = projectData.lifecycleData.assessmentStep2?.eligibilityDecision?.status,
                    eligibilityDecisionDate = projectData.lifecycleData.assessmentStep2?.eligibilityDecision?.decisionDate,
                    fundingDecision = projectData.lifecycleData.assessmentStep2?.fundingDecision?.status,
                    fundingDecisionDate = projectData.lifecycleData.assessmentStep2?.fundingDecision?.decisionDate,
                    eligibilityAssessmentResult = projectData.lifecycleData.assessmentStep2?.assessmentEligibility?.result,
                    eligibilityAssessmentNotes = projectData.lifecycleData.assessmentStep2?.assessmentEligibility?.note,
                    qualityAssessmentResult = projectData.lifecycleData.assessmentStep2?.assessmentQuality?.result,
                    qualityAssessmentNotes = projectData.lifecycleData.assessmentStep2?.assessmentQuality?.note,
                )
            }

    private fun ProjectAssessmentEligibilityResultData?.toMessage() =
        if (this == null) null else
            getMessage(
                "common.label.projecteligibilityassessmentresult.".plus(this.name), exportLocale, messageSource
            )

    private fun ProjectAssessmentQualityResultData?.toMessage() =
        if (this == null) null else
            getMessage(
                "common.label.projectqualityassessmentresult.".plus(this.name), exportLocale, messageSource
            )

    private fun ApplicationStatusData?.toMessage() =
        if (this == null) null else
            getMessage(
                "common.label.projectapplicationstatus.".plus(this.name), exportLocale, messageSource
            )
}