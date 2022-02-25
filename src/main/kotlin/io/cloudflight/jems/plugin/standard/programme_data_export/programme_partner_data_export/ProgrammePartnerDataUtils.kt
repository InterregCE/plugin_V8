package io.cloudflight.jems.plugin.standard.programme_data_export.programme_partner_data_export

import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.*
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetGeneralCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetStaffCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.BudgetTravelAndAccommodationCostEntryData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.PartnerBudgetPerFundData
import io.cloudflight.jems.plugin.contract.models.project.sectionD.ProjectPartnerBudgetPerPeriodData
import io.cloudflight.jems.plugin.standard.budget_export.CLOSURE_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.PREPARATION_PERIOD
import io.cloudflight.jems.plugin.standard.budget_export.models.BudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.FundInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.GeneralBudgetTotalCostInfo
import io.cloudflight.jems.plugin.standard.budget_export.models.PartnerInfo
import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.getMessage
import io.cloudflight.jems.plugin.standard.programme_data_export.model.PartnerAddress
import io.cloudflight.jems.plugin.standard.programme_data_export.model.PartnerContact
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.*

internal fun getBudgetPerPeriod(projectPartnerBudgetPerPeriodData: ProjectPartnerBudgetPerPeriodData?) =
    mutableMapOf<Int, BigDecimal>().also { map ->
        projectPartnerBudgetPerPeriodData?.periodBudgets?.map { period ->
            map.put(period.periodNumber, period.totalBudgetPerPeriod)
        }.also { periods ->
            if (!periods.isNullOrEmpty()) {
                map[PREPARATION_PERIOD] =
                    projectPartnerBudgetPerPeriodData?.periodBudgets?.map { it.totalBudgetPerPeriod }?.firstOrNull()
                        ?: BigDecimal.ZERO
                map[CLOSURE_PERIOD] =
                    projectPartnerBudgetPerPeriodData?.periodBudgets?.map { it.totalBudgetPerPeriod }?.lastOrNull()
                        ?: BigDecimal.ZERO
            }
        }
    }

internal fun getGeneralBudgetCostTotalsFor(budgetCosts: List<BudgetGeneralCostEntryData>): GeneralBudgetTotalCostInfo =
    GeneralBudgetTotalCostInfo(
        realCostTotal = budgetCosts.filter { it.unitCostId == null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO },
        unitCostTotal = budgetCosts.filter { it.unitCostId != null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO }
    )

internal fun getTravelCostTotals(
    travelCosts: List<BudgetTravelAndAccommodationCostEntryData>, travelCostTotal: BigDecimal, flatRate: Int?
): BudgetTotalCostInfo =
    BudgetTotalCostInfo(
        total = travelCostTotal,
        flatRateTotal = if (flatRate == null) BigDecimal.ZERO else travelCostTotal,
        realCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId == null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
        unitCostTotal = if (flatRate == null) travelCosts.filter { it.unitCostId != null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
    )

internal fun getFoundInfoList(partnerBudgetPerFundData: Set<PartnerBudgetPerFundData>,
                              programmeInfoData: ProgrammeInfoData) =
    programmeInfoData.funds.map { element ->
        partnerBudgetPerFundData.firstOrNull { it.fund?.id == element.id }.let { budgetPerFund ->
            FundInfo(
                fundAmount = budgetPerFund?.value ?: BigDecimal.ZERO,
                fundPercentage = budgetPerFund?.percentage ?: BigDecimal.ZERO,
                percentageOfTotal = budgetPerFund?.percentageOfTotal ?: BigDecimal.ZERO
            )
        }
    }

internal fun getStaffCostTotals(
    staffCosts: List<BudgetStaffCostEntryData>, staffCostTotal: BigDecimal, flatRate: Int?
): BudgetTotalCostInfo =
    BudgetTotalCostInfo(
        total = staffCostTotal,
        flatRateTotal = if (flatRate == null) BigDecimal.ZERO else staffCostTotal,
        realCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId == null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO,
        unitCostTotal = if (flatRate == null) staffCosts.filter { it.unitCostId != null }
            .sumOf { it.rowSum ?: BigDecimal.ZERO } else BigDecimal.ZERO
    )

internal fun getPartnerInfo(partner: ProjectPartnerData, exportLocale: Locale, messageSource: MessageSource): PartnerInfo =
    partner.addresses.firstOrNull { it.type === ProjectPartnerAddressTypeData.Organization }.let { address ->
        PartnerInfo(
            if (partner.role.isLead) "LP${partner.sortNumber.toString()}" else "PP${partner.sortNumber.toString()}",
            getMessage("project.application.form.partner.table.status.${if(partner.active) "active" else "inactive"}", exportLocale, messageSource),
            partner.abbreviation,
            partner.nameInOriginalLanguage,
            partner.nameInEnglish,
            address?.country, address?.nutsRegion3, address?.nutsRegion2
        )
    }

internal fun getPartnerAddress(
    addresses: List<ProjectPartnerAddressData>,
    addressType: ProjectPartnerAddressTypeData
): PartnerAddress =
    addresses.firstOrNull { it.type === addressType }.let { address ->
        PartnerAddress(
            address?.street?.plus("-").let { it?.plus(address?.houseNumber) },
            address?.country,
            address?.nutsRegion2,
            address?.nutsRegion3,
            address?.postalCode,
            address?.city,
            address?.homepage
        )
    }

internal fun getPartnerContact(contacts: List<ProjectPartnerContactData>, contactType: ProjectContactTypeData):
        PartnerContact =
    contacts.firstOrNull { it.type === contactType }.let { contact ->
        PartnerContact(
            contact?.title,
            contact?.firstName,
            contact?.lastName,
            contact?.email,
            contact?.telephone
        )
    }

internal fun getStateAidCheck(projectPartnerStateAidData: ProjectPartnerStateAidData?, exportLocale: Locale,
                              messageSource: MessageSource): String {
    if (projectPartnerStateAidData.riskOfAid())
        return getMessage(
            "project.partner.state.aid.risk.of.state.aid", exportLocale, messageSource
        )
    if (projectPartnerStateAidData.indirectRiskOfAid())
        return getMessage(
            "project.partner.state.aid.risk.of.indirect.aid", exportLocale, messageSource
        )
    if (projectPartnerStateAidData.unsetAid())
        return ""
    return getMessage(
        "project.partner.state.aid.no.risk.of.state.aid", exportLocale, messageSource
    )
}

internal fun ApplicationStatusData?.toMessage(exportLocale: Locale, messageSource: MessageSource) =
    if (this == null) null else
        getMessage(
            "common.label.projectapplicationstatus.".plus(this.name), exportLocale, messageSource
        )

internal fun ProjectPartnerRoleData?.toMessage(exportLocale: Locale, messageSource: MessageSource) =
    if (this == null) null else
        getMessage(
            "common.label.project.partner.role.".plus(this.name), exportLocale, messageSource
        )

internal fun ProjectTargetGroupData?.toMessage(exportLocale: Locale, messageSource: MessageSource) =
    if (this == null) null else
        getMessage(
            "project.application.form.relevance.target.group.".plus(this.name), exportLocale, messageSource
        )

internal fun PartnerSubTypeData?.toMessage(exportLocale: Locale, messageSource: MessageSource) =
    if (this == null) null else
        getMessage(
            "project.application.form.relevance.target.group.".plus(this.name), exportLocale, messageSource
        )

internal fun ProjectPartnerStateAidData?.riskOfAid() =
    this?.answer1 == true && this.answer2 == true && this.answer3 == true && this.answer4 == true

internal fun ProjectPartnerStateAidData?.indirectRiskOfAid() =
    this?.answer1 == false || this?.answer2 == false || this?.answer3 == false && this.answer4 == true

internal fun ProjectPartnerStateAidData?.unsetAid() =
    this?.answer1 == null || this.answer2 == null || this.answer3 == null || this.answer4 == null

internal fun Any?.toCallCellData() =
    CellData(this).backgroundColor(Color.LIGHT_GREEN)

internal fun Any?.toProjectCellData() =
    CellData(this).backgroundColor(Color.LIGHT_BLUE)

internal fun Any?.toPartnerBudgetData() =
    CellData(this).backgroundColor(Color.LIGHT_ORANGE)

internal fun Any?.toPartnerCellData() =
    CellData(this).backgroundColor(Color.LIGHT_RED)