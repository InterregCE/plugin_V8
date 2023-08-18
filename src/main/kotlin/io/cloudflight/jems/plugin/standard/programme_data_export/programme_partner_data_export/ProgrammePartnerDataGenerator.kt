package io.cloudflight.jems.plugin.standard.programme_data_export.programme_partner_data_export

import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import io.cloudflight.jems.plugin.standard.common.toLocale
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProjectAndCallAndPartnerData
import io.cloudflight.jems.plugin.standard.programme_data_export.model.ProgrammePartnerDataExportRow
import io.cloudflight.jems.plugin.standard.programme_data_export.programme_project_data_export.toErrorCellData
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ProgrammePartnerDataGenerator(
    private val partnersToExport: List<ProjectAndCallAndPartnerData>,
    private val programmeInfoData: ProgrammeInfoData,
    private val failedProjectIds: Set<Long>,
    private val exportationDateTime: ZonedDateTime,
    private val exportLanguage: SystemLanguageData,
    private val dataLanguage: SystemLanguageData,
    private val messageSource: MessageSource
) {
    private val exportLocale = exportLanguage.toLocale()
    private val maximalPeriods =
        partnersToExport.map { it.projectData.sectionA?.periods ?: emptyList() }.maxByOrNull { it.size }
            ?.map { it.number }
            ?.toMutableList().also {
                it?.add(0, PREPARATION_PERIOD)
                it?.add(CLOSURE_PERIOD)
            } ?: emptyList()

    fun getData() =
        ExcelData().also {
            it.addSheet("Partner data").also { sheet ->
                sheet.addRow(CellData(getFileTitle(programmeInfoData.title, exportationDateTime)))
                sheet.addRow(
                    *getHeaderRow(
                        maximalPeriods,
                        programmeInfoData,
                        exportLocale,
                        messageSource,
                        exportLanguage
                    )
                ).borderTop()
                sheet.addRows(getRows(generateProgrammeProjectPartnerDataExportRows()))
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
        "${if (programmeTitle.isNullOrBlank()) "programme" else programmeTitle} - partner data - ${
            exportationDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm:ss"))
        }"


    private fun getRows(data: List<ProgrammePartnerDataExportRow>): List<Array<CellData>> =
        data.map { row ->
            mutableListOf<CellData>().also {
                it.add(row.callId.toCallCellData().borderLeft())
                it.add(row.callName.toCallCellData())

                it.add(row.projectId.toProjectCellData())
                it.add(row.projectAcronym.toProjectCellData())
                it.add(row.projectTitle.toProjectCellData())
                it.add(row.projectVersion.toProjectCellData())
                it.add(row.projectStatus.toMessage(exportLocale, messageSource).toProjectCellData())
                it.add(row.projectProgrammePriority.toProjectCellData())
                it.add(row.projectSpecificObjective.toProjectCellData())

                it.add(row.partnerInfo.partnerNumber.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerStatus.toPartnerBudgetData())
                it.add(row.partnerRole.toMessage(exportLocale, messageSource).toPartnerBudgetData())
                it.add(row.partnerInfo.partnerAbbreviation.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerNameInOriginalLanguage.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerNameInEnglish.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerCountry.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerNuts2.toPartnerBudgetData())
                it.add(row.partnerInfo.partnerNuts3.toPartnerBudgetData())
                it.addAll(row.fundInfoList.flatMap { it.toStringList() }.map { it.toPartnerBudgetData() })
                it.add(row.publicContribution.toPartnerBudgetData())
                it.add(row.automaticPublicContribution.toPartnerBudgetData())
                it.add(row.privateContribution.toPartnerBudgetData())
                it.add(row.getTotalPartnerContribution().toPartnerBudgetData())
                it.add(row.totalEligibleBudget.toPartnerBudgetData())
                it.add(row.totalEligibleBudgetPercentage.toPartnerBudgetData())

                it.add(row.staffCostTotals.total.toPartnerBudgetData())
                it.add(row.staffCostTotals.flatRateTotal.toPartnerBudgetData())
                it.add(row.staffCostTotals.realCostTotal.toPartnerBudgetData())
                it.add(row.staffCostTotals.unitCostTotal.toPartnerBudgetData())
                it.add(row.officeCostTotals.toPartnerBudgetData())
                it.add(row.officeCostFlatRatesTotals.toPartnerBudgetData())
                it.add(row.travelCostTotals.total.toPartnerBudgetData())
                it.add(row.travelCostTotals.flatRateTotal.toPartnerBudgetData())
                it.add(row.travelCostTotals.realCostTotal.toPartnerBudgetData())
                it.add(row.travelCostTotals.unitCostTotal.toPartnerBudgetData())
                it.add(row.externalCostTotals.getTotal().toPartnerBudgetData())
                it.add(row.externalCostTotals.realCostTotal.toPartnerBudgetData())
                it.add(row.externalCostTotals.unitCostTotal.toPartnerBudgetData())
                it.add(row.equipmentCostTotals.getTotal().toPartnerBudgetData())
                it.add(row.equipmentCostTotals.realCostTotal.toPartnerBudgetData())
                it.add(row.equipmentCostTotals.unitCostTotal.toPartnerBudgetData())
                it.add(row.infrastructureCostTotals.getTotal().toPartnerBudgetData())
                it.add(row.infrastructureCostTotals.realCostTotal.toPartnerBudgetData())
                it.add(row.infrastructureCostTotals.unitCostTotal.toPartnerBudgetData())
                it.add(row.otherCosts.toPartnerBudgetData())
                it.add(row.unitCostsCoveringMultipleCostCategories.toPartnerBudgetData())
                it.add(row.lumpSumsCoveringMultipleCostCategories.toPartnerBudgetData())
                it.addAll(maximalPeriods.map { periodNumber ->
                    row.partnerBudgetPerPeriod.entries.firstOrNull { it.key == periodNumber }?.let {
                        it.value.toPartnerBudgetData()
                    } ?: BigDecimal.ZERO.toPartnerBudgetData()
                })

                it.add(row.department.toPartnerCellData())
                it.add(row.partnerType.toMessage(exportLocale, messageSource).toPartnerCellData())
                it.add(row.partnerSubType.toMessage(exportLocale, messageSource).toPartnerCellData())
                it.add(row.legalStatus.toPartnerCellData())
                it.add(row.sectorOfActivityAtNace?.text.toPartnerCellData())
                it.add(row.vat.toPartnerCellData())
                it.add(row.vatRecovery.toPartnerCellData())
                it.add(row.otherIdentifierNumber.toPartnerCellData())
                it.add(row.otherIdentifierDescription.toPartnerCellData())
                it.add(row.pic.toPartnerCellData())
                it.add(row.stateAidCheck.toPartnerCellData())
                it.add(row.stateAidScheme.toPartnerCellData())

                it.add(row.mainAddress?.country.toPartnerCellData())
                it.add(row.mainAddress?.nutsRegion3.toPartnerCellData())
                it.add(row.mainAddress?.streetHouseNumber.toPartnerCellData())
                it.add(row.mainAddress?.postalCode.toPartnerCellData())
                it.add(row.mainAddress?.city.toPartnerCellData())
                it.add(row.mainAddress?.homepage.toPartnerCellData())
                it.add(row.departmentAddress?.country.toPartnerCellData())
                it.add(row.departmentAddress?.nutsRegion3.toPartnerCellData())
                it.add(row.departmentAddress?.streetHouseNumber.toPartnerCellData())
                it.add(row.departmentAddress?.postalCode.toPartnerCellData())
                it.add(row.departmentAddress?.city.toPartnerCellData())

                it.add(row.legalRepresentativeContact?.title.toPartnerCellData())
                it.add(row.legalRepresentativeContact?.firstName.toPartnerCellData())
                it.add(row.legalRepresentativeContact?.lastName.toPartnerCellData())
                it.add(row.contactPartner?.title.toPartnerCellData())
                it.add(row.contactPartner?.firstName.toPartnerCellData())
                it.add(row.contactPartner?.lastName.toPartnerCellData())
                it.add(row.contactPartner?.email.toPartnerCellData())
                it.add(
                    row.contactPartner?.telephone.toPartnerCellData()
                        .borderRight()
                )

            }.toTypedArray()
        }

    private fun generateProgrammeProjectPartnerDataExportRows(): List<ProgrammePartnerDataExportRow> =
        partnersToExport
            .sortedWith(compareBy({ it.callDetailData.id }, { it.projectData.sectionA?.customIdentifier }, {it.projectPartnerData.sortNumber}))
            .map { entry ->
            val projectVersion = entry.projectVersionData
            val projectData = entry.projectData
            val callData = entry.callDetailData
            val partnerData = entry.projectPartnerData
            val projectPartnerBudgetPerFundData =
                projectData.sectionD.projectPartnerBudgetPerFundData.first { it.partner?.id == partnerData.id }
            ProgrammePartnerDataExportRow(
                callId = callData.id,
                callName = callData.name,

                projectId = projectData.sectionA?.customIdentifier!!,
                projectAcronym = projectData.sectionA?.acronym,
                projectTitle = projectData.sectionA?.title?.getTranslationFor(dataLanguage),
                projectVersion = projectVersion.version,
                projectStatus = projectData.lifecycleData.status,
                projectProgrammePriority = projectData.sectionA?.programmePriority?.code,
                projectSpecificObjective = projectData.sectionA?.specificObjective?.code,
                partnerInfo = getPartnerInfo(partnerData, exportLocale, messageSource),
                partnerRole = partnerData.role,
                fundInfoList = getFoundInfoList(projectPartnerBudgetPerFundData.budgetPerFund, programmeInfoData),
                publicContribution = projectPartnerBudgetPerFundData.publicContribution ?: BigDecimal.ZERO,
                automaticPublicContribution = projectPartnerBudgetPerFundData.autoPublicContribution ?: BigDecimal.ZERO,
                privateContribution = projectPartnerBudgetPerFundData.privateContribution ?: BigDecimal.ZERO,
                totalEligibleBudget = projectPartnerBudgetPerFundData.totalEligibleBudget ?: BigDecimal.ZERO,
                totalEligibleBudgetPercentage = projectPartnerBudgetPerFundData.percentageOfTotalEligibleBudget
                    ?: BigDecimal.ZERO,
                staffCostTotals = getStaffCostTotals(
                    partnerData.budget.projectPartnerBudgetCosts.staffCosts,
                    partnerData.budget.projectBudgetCostsCalculationResult.staffCosts,
                    partnerData.budget.projectPartnerOptions?.staffCostsFlatRate
                ),
                officeCostTotals = partnerData.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                officeCostFlatRatesTotals = partnerData.budget.projectBudgetCostsCalculationResult.officeAndAdministrationCosts,
                travelCostTotals = getTravelCostTotals(
                    partnerData.budget.projectPartnerBudgetCosts.travelCosts,
                    partnerData.budget.projectBudgetCostsCalculationResult.travelCosts,
                    partnerData.budget.projectPartnerOptions?.travelAndAccommodationOnStaffCostsFlatRate
                ),
                externalCostTotals = getGeneralBudgetCostTotalsFor(partnerData.budget.projectPartnerBudgetCosts.externalCosts),
                equipmentCostTotals = getGeneralBudgetCostTotalsFor(partnerData.budget.projectPartnerBudgetCosts.equipmentCosts),
                infrastructureCostTotals = getGeneralBudgetCostTotalsFor(partnerData.budget.projectPartnerBudgetCosts.infrastructureCosts),
                otherCosts = partnerData.budget.projectBudgetCostsCalculationResult.otherCosts,
                unitCostsCoveringMultipleCostCategories = partnerData.budget.projectPartnerBudgetCosts.unitCosts
                    .sumOf { it.rowSum ?: BigDecimal.ZERO },
                lumpSumsCoveringMultipleCostCategories = projectData.sectionE.projectLumpSums.flatMap { it.lumpSumContributions }
                    .filter { it.partnerId == partnerData.id }.sumOf { it.amount },
                partnerBudgetPerPeriod = getBudgetPerPeriod(projectData.sectionD.projectPartnerBudgetPerPeriodData.partnersBudgetPerPeriod
                    .firstOrNull { it.partner.id == partnerData.id }),

                department = partnerData.department.getTranslationFor(dataLanguage),
                partnerType = partnerData.partnerType,
                partnerSubType = partnerData.partnerSubType,
                legalStatus = partnerData.legalStatusDescription.getTranslationFor(dataLanguage),
                sectorOfActivityAtNace = partnerData.nace,
                vat = partnerData.vat,
                vatRecovery = partnerData.vatRecovery,
                otherIdentifierNumber = partnerData.otherIdentifierNumber,
                otherIdentifierDescription = partnerData.otherIdentifierDescription.getTranslationFor(dataLanguage),
                pic = partnerData.pic,
                stateAidCheck = getStateAidCheck(partnerData.stateAid, exportLocale, messageSource),
                stateAidScheme = partnerData.stateAid?.stateAidScheme?.abbreviatedName?.getTranslationFor(dataLanguage),
                mainAddress = getPartnerAddress(partnerData.addresses, ProjectPartnerAddressTypeData.Organization),
                departmentAddress = getPartnerAddress(partnerData.addresses, ProjectPartnerAddressTypeData.Department),
                legalRepresentativeContact = getPartnerContact(
                    partnerData.contacts,
                    ProjectContactTypeData.LegalRepresentative
                ),
                contactPartner = getPartnerContact(partnerData.contacts, ProjectContactTypeData.ContactPerson)
            )
        }

}
