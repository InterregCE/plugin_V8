package io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.CallTypeData
import io.cloudflight.jems.plugin.contract.models.programme.ProgrammeInfoData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammeObjectiveDimension
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.contract.models.project.ProjectIdentificationData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.ProjectPartnerRoleData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerBudgetOptionsData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerSummaryData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.models.report.partner.identification.ProjectPartnerReportBaseData
import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementData
import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementSubcontractData
import io.cloudflight.jems.plugin.contract.services.report.ReportPartnerDataProvider
import io.cloudflight.jems.plugin.standard.common.PartnerUtils
import java.math.BigDecimal

data class MainModel(
    val programInfoData: ProgrammeInfoData,
    val basicModelList: List<BasicModel>
) {
    fun buildOneProgram(callList: List<Call>, projectList: List<Project>): Programme {
        val infoData = this.programInfoData
        //logger.info("section A: {}", projectData.sectionA)
        val mPeriod = "${infoData.firstYear?.toString().orEmpty()}-${infoData.lastYear?.toString().orEmpty()}"
        return Programme(
            programmeName = "$mPeriod ${infoData.title}",
            period = mPeriod,
            call = callList,
            project = projectList
        )
    }

    data class BasicModel(
        val projectData: ProjectData,
        val projectIdentificationData: ProjectIdentificationData,
        val versionsData: List<ProjectVersionData>,
        val projectPartnerSummary: List<ProjectPartnerSummaryData>,
        val projectPartnerBudgetOptions: List<ProjectPartnerBudgetOptionsData>,
        val callDetailData: CallDetailData,
        val partnerReports: List<ProjectPartnerReportBaseData>,
        val reportPartnerDataProvider: ReportPartnerDataProvider
    ) {
        private fun ProjectData.buildPartners(): List<Partner> {
            val partners = ArrayList<Partner>()
            this.sectionB.partners.forEach {
                val partnerID : Long? = it.id
                val financeData = it.budget.projectPartnerCoFinancing
                val partnerContributions = financeData.partnerContributions
                var partnerContribution: BigDecimal = BigDecimal.ZERO
                var legalStatus: String? = null
                var contractorList: List<ProjectPartnerReportProcurementData>? = null
                var procurementSubcontractList: List<ProjectPartnerReportProcurementSubcontractData>? = null
                if (partnerID != null){
                    var filteredPartnerReports = partnerReports.filter { report ->
                        report.partnerId == partnerID
                    }
                    contractorList = filteredPartnerReports.flatMap { report ->
                        reportPartnerDataProvider.getProcurementList(report.partnerId, report.id)
                    }
                    procurementSubcontractList = contractorList.flatMap { contractor ->
                        reportPartnerDataProvider.getProcurementSubcontract( partnerID, contractor.reportId, contractor.id)
                    }
                }
                partnerContributions.forEach { contribData ->
                    contribData.amount?.let { amount ->
                        partnerContribution += amount
                    }
                    contribData.status?.let { statusData ->
                        legalStatus = statusData.name
                    }
                }
                val partnerUtils = PartnerUtils()
                val contacts = partnerUtils.getContactPerson(it.contacts)
                val mainAddress = partnerUtils.getMainAddress(it.addresses)
                val departmentAddress = partnerUtils.getDepartmentAddress(it.addresses)
                val totalEligibleBudget = it.budget.projectBudgetCostsCalculationResult.totalCosts
                val coFinancingAmount = totalEligibleBudget - partnerContribution
                partners.add(
                    Partner(
                        partnerType = if (it.role == ProjectPartnerRoleData.LEAD_PARTNER)
                            "lead" else "project",
                        multiLanguageOrganisationName = listOf(
                            GenericMultiLanguage(it.nameInOriginalLanguage.orEmpty(), null),
                            GenericMultiLanguage(it.nameInEnglish.orEmpty(), "en")
                        ),
                        contractors = contractorList?.let { it1 -> Partner.Contractor.listFrom(it1) },
                        subContractors = procurementSubcontractList?.let { it1 -> Partner.SubContractor.listFrom(it1) },
                        address = listOf(
                            Partner.Address(
                                Partner.Address.MainOrDepartment.Main.name.lowercase(),
                                mainAddress?.country,
                                mainAddress?.city,
                                mainAddress?.street,
                                mainAddress?.postalCode
                            ), Partner.Address(
                                Partner.Address.MainOrDepartment.Department.name.lowercase(),
                                departmentAddress?.country,
                                departmentAddress?.city,
                                departmentAddress?.street,
                                departmentAddress?.postalCode,
                                it.department.toListOf<GenericMultiLanguage>()
                            )
                        ),
                        participantIdentificationCode = it.pic,
                        //participantIdNoPic = it.otherIdentifierNumber,
                        participantIdNoPic = it.vat,
                        organisationType = it.partnerType?.toHumanReadable(),
                        webSite = mainAddress?.homepage,
                        legalStatus = legalStatus,
                        mail = contacts?.email,
                        totalEligibleBudget = totalEligibleBudget,
                        coFinancingAmount = coFinancingAmount,
                        coFinancingRate = (coFinancingAmount / totalEligibleBudget) * 100.toBigDecimal(),
                        partnerContribution = if (partnerContribution == BigDecimal.ZERO) null else partnerContribution,
                        gberSchemeDeMinimis = it.stateAid?.stateAidScheme?.measure?.text
                    )
                )
            }
            return partners.toList()
        }

        fun buildOneCall(): Call {
            val callDetail = this.callDetailData
            return Call(
                serialNumber = callDetail.id.toString(),
                callStartDate = callDetail.startDateTime.toLocalDate().toString(),
                callEndDate = callDetail.endDateTime.toLocalDate().toString()
            )
        }

        fun buildOneProject(): Project {
            val projectData = this.projectData
            val projectIdData = this.projectIdentificationData
            val partners = projectData.buildPartners()
            val coFinancingOverview = projectData.sectionA?.coFinancingOverview
            val fundOverviews = coFinancingOverview?.projectManagementCoFinancing?.fundOverviews
            val specificObjective = projectData.sectionA?.specificObjective
            val toi = mutableListOf<String>()
            projectData.dimensionCodes.forEach{ dimensionCode ->
            if (dimensionCode.programmeObjectiveDimension == ProgrammeObjectiveDimension.TypesOfIntervention ) {
                    toi.add(dimensionCode.dimensionCode)
                }
            }
            var period = 0
            projectData.sectionA?.periods?.forEach {
                period += it.number
            }
            val totalBudget = coFinancingOverview?.projectManagementCoFinancing?.totalFundAndContribution
            return Project(
                partner = partners,
                projectId = projectData.sectionA?.customIdentifier,
                acronym = projectData.sectionA?.acronym,
                callSerialNumber = this.callDetailData.id.toString(),
                multiLanguagesName = projectData.sectionA?.title?.toListOf<Project.MultiLanguageName>(),
                multiLanguagesDescription = projectData.sectionA?.intro?.toListOf<Project.MultiLanguageDescription>(),
                /* multiLangExpAndActAchievements = projectData.sectionC.projectWorkPackages.toMultiExpAndActLists(),*/
                multiLangExpAchievements = projectData.sectionC.projectWorkPackages.toMultiExpLists(),

                /*GESTIRE ACTUAL Achievements */

                projectStartDate = if (projectIdData.projectStartDate == null ) "1970-01-01" else projectIdData.projectStartDate.toString(),
                projectEndDate = if (projectIdData.projectEndDate == null ) "1970-01-01" else projectIdData.projectEndDate.toString(),
                totalBudget = totalBudget,
                programmePriority = projectData.sectionA?.programmePriority?.code,
                multiLanguagesPriority = projectData.sectionA?.programmePriority?.title?.toListOf<Project.MultiLanguagePriorityName>(),
                contributingEuFunds = "", /*coFinancingOverview?.projectManagementCoFinancing?.totalEuContribution,*/
                prioritySpecificObjective = specificObjective?.programmeObjectivePolicy?.officialProgrammePolicyCode,
                euFundingAmount = coFinancingOverview?.projectManagementCoFinancing?.totalEuFundingAmount,
                euFundingCoFinancingRate = coFinancingOverview?.projectManagementCoFinancing?.averageEuFinancingRate,
                infraInvestmentArea = projectData.sectionC.projectWorkPackages.calculateInfraInvestmentArea(),
                interventionType = toi,
                strategicOrAbove5m = if (totalBudget == null) false else totalBudget > 5000000.toBigDecimal(),
                technicalAssistanceProject = false,
                smallProject = callDetailData.type == CallTypeData.SPF,
                webSite = null,
                fundsCoFinancingAmount = fundOverviews?.toListOfFundsCoFinancing(),
                fundERDFEquivalents = fundOverviews?.toERDFEquivalentList(),
                relPrecedentProjects = null,
                relSubSequentProjects = null,
                linkedProjects = null,
                deliverables = Project.Deliverable.withSectionC(projectData.sectionC).buildDeliverableList(),
                investmentInfrastructureNumbers = Project.InvestmentBuilder
                    .withSectionC(projectData.sectionC)
                    .backNumber(),
                investmentInfrastructureName = Project.InvestmentBuilder
                    .withSectionC(projectData.sectionC)
                    .backInvestmentNameList(),
                investmentInfrastructureCost = Project.InvestmentBuilder
                    .withSectionD(projectData.sectionD)
                    .backCost(),
                contributionWiderStrategiesAndPolicies = Project
                    .ContributionWiderStrategiesAndPolicies
                    .from(projectData.sectionC.projectRelevance?.projectStrategies),
                relevantMentionAndPrice = null,
                projectIndicators = projectData.sectionA?.resultIndicatorOverview?.indicatorLines.toProjectIndicators(),
                projectHierarchy = null
            )
        }
    }
}