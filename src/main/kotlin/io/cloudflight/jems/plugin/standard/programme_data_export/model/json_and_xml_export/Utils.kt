package io.cloudflight.jems.plugin.standard.programme_data_export.model.json_and_xml_export

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.programme.fund.ProgrammeFundTypeData
import io.cloudflight.jems.plugin.contract.models.project.ProjectData


//import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementData
//import io.cloudflight.jems.plugin.contract.models.report.partner.procurement.ProjectPartnerReportProcurementSubcontractData

import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA3.ProjectCoFinancingByFundOverviewData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.tableA4.IndicatorOverviewLine
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerBudgetOptionsData
import io.cloudflight.jems.plugin.contract.models.project.sectionB.partners.budget.ProjectPartnerSummaryData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.relevance.ProjectTargetGroupData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.ProjectWorkPackageData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageInvestmentData
import io.cloudflight.jems.plugin.contract.models.project.versions.ProjectVersionData
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProgrammeDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider

private inline fun <reified KotlinClass> kotlinClassInstance(arg1: String, arg2: String): KotlinClass? {
    val clazz = Class.forName(KotlinClass::class.java.name)
    clazz.constructors.forEach { singleConstructor ->
        if (singleConstructor.parameters.size == 2) {
            runCatching {
                singleConstructor.newInstance(arg1, arg2) as KotlinClass
            }.onSuccess {
                return it
            }
        }
    }
    return null
}

/**This method allows to create a class list from a set of [io.cloudflight.jems.plugin.contract.models.common.InputTranslationData]
 * The only important thing is that your class has a constructor with two string fields */
internal inline fun <reified KotlinClass> Set<InputTranslationData>.toListOf(): List<KotlinClass> {
    val back = ArrayList<KotlinClass>()
    this.forEach {
        kotlinClassInstance<KotlinClass>(
            it.translation.orEmpty(),
            it.language.name.lowercase()
        )?.let { instanceOf ->
            back.add(instanceOf)
        }
    }
    return back.toList()
}

internal fun List<ProjectWorkPackageData>.calculateInfraInvestmentArea(): List<GenericAddress> {
    val back = ArrayList<GenericAddress>()
    this.forEach {
        back.addAll(it.investments.calculateInfraInvestmentArea())
    }
    return back.toList()
}

@JvmName("calculateInfraInvestmentAreaPrivate")
private fun List<WorkPackageInvestmentData>.calculateInfraInvestmentArea(): List<GenericAddress> {
    val back = ArrayList<GenericAddress>()
    this.forEach {
        back.add(
            GenericAddress(
                it.address?.country,
                it.address?.city,
                it.address?.street,
                it.address?.houseNumber,
                it.address?.postalCode
            )
        )
    }
    return back.toList()
}

internal fun ProjectTargetGroupData.toHumanReadable(): String? {
    return when (this) {
        ProjectTargetGroupData.InfrastructureAndServiceProvider -> this.infrastructureAndServiceProvider()
        ProjectTargetGroupData.InterestGroups -> "${this.name.toHumanReadable()} including NGOs"
        ProjectTargetGroupData.EducationTrainingCentreAndSchool -> this.educationTrainingCentreAndSchool()
        ProjectTargetGroupData.EnterpriseExceptSme -> this.enterpriseExceptSme()
        ProjectTargetGroupData.Sme, ProjectTargetGroupData.Egtc -> this.name.uppercase()
        ProjectTargetGroupData.InternationalOrganisationEeig -> this.internationalOrganisationEig()
        ProjectTargetGroupData.CrossBorderLegalBody -> this.crossLegalBody()
        else -> this.name.toHumanReadable()
    }
}

private fun ProjectTargetGroupData.infrastructureAndServiceProvider(): String {
    var newString = ""
    this.name.stringArrayRegex().forEachIndexed { i, each ->
        newString += when (i) {
            0 -> each
            2 -> " (public) ${each.lowercase()}"
            else -> " ${each.lowercase()}"
        }
    }
    return newString
}

private fun ProjectTargetGroupData.educationTrainingCentreAndSchool(): String {
    var newString = ""
    this.name.stringArrayRegex().forEachIndexed { i, each ->
        newString += when (i) {
            0 -> each
            1 -> "/${each.lowercase()}"
            else -> " ${each.lowercase()}"
        }
    }
    return newString
}

private fun ProjectTargetGroupData.enterpriseExceptSme(): String {
    var newString = ""
    val array = this.name.stringArrayRegex()
    array.forEachIndexed { i, each ->
        newString += when (i) {
            0 -> each
            1 -> ", ${each.lowercase()}"
            array.size - 1 -> " ${each.uppercase()}"
            else -> " ${each.lowercase()}"
        }
    }
    return newString
}

private fun ProjectTargetGroupData.internationalOrganisationEig(): String {
    var newString = ""
    val array = this.name.stringArrayRegex()
    array.forEachIndexed { i, each ->
        newString += when (i) {
            0 -> each
            array.size - 1 -> ", ${each.uppercase()}"
            else -> " ${each.lowercase()}"
        }
    }
    return newString
}

private fun ProjectTargetGroupData.crossLegalBody(): String {
    var newString = ""
    this.name.stringArrayRegex().forEachIndexed { i, each ->
        newString += when (i) {
            0 -> each
            1 -> "-${each.lowercase()}"
            else -> " ${each.lowercase()}"
        }
    }
    return newString
}

private fun String.stringArrayRegex(): Array<String> {
    return this.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])".toRegex())
        .dropLastWhile { it.isEmpty() }
        .toTypedArray()
}

fun CamelCaseString?.toHumanReadable(): String? {
    if (this == null) return null
    var newString = ""
    this.stringArrayRegex().forEachIndexed { i, each ->
        newString += if (i == 0) each else " ${each.lowercase()}"
    }
    return newString
}

fun List<ProjectVersionData>.getProjectVersionsToExport(): List<ProjectVersionData> {
    val filter = this.filter {
        it.status !in setOf(
            ApplicationStatusData.STEP1_DRAFT,
            ApplicationStatusData.DRAFT,
            ApplicationStatusData.RETURNED_TO_APPLICANT,
            ApplicationStatusData.RETURNED_TO_APPLICANT_FOR_CONDITIONS,
            ApplicationStatusData.MODIFICATION_PRECONTRACTING,
            ApplicationStatusData.MODIFICATION_PRECONTRACTING_SUBMITTED,
            ApplicationStatusData.IN_MODIFICATION,
            ApplicationStatusData.MODIFICATION_REJECTED,
            ApplicationStatusData.MODIFICATION_SUBMITTED
        )
    }
    return filter.groupBy { it.projectId }.entries.mapNotNull {
        it.value.maxByOrNull { projectVersion -> projectVersion.createdAt }
    }
}

fun List<ProjectCoFinancingByFundOverviewData>.toListOfFundsCoFinancing(): List<Project.FundsCoFinancingAmount> {
    val arrayList = ArrayList<Project.FundsCoFinancingAmount>()
    this.forEach {
        arrayList.add(
            Project.FundsCoFinancingAmount(
                it.fundType?.name,
                it.fundingAmount,
                it.coFinancingRate
            )
        )
    }
    return arrayList.toList()
}

fun List<ProjectCoFinancingByFundOverviewData?>?.toERDFEquivalentList(): List<Project.ERDFEquivalent>? {
    if (this == null) return null
    val arrayList = ArrayList<Project.ERDFEquivalent>()
    this.filter { it?.fundType == ProgrammeFundTypeData.OTHER }.forEach {
        arrayList.add(
            Project.ERDFEquivalent("${it?.fundType?.name} - ${it?.fundAbbreviation.toString()}", it?.fundingAmount, it?.coFinancingRate)
        )
    }
    return arrayList.toList()
}

fun List<IndicatorOverviewLine?>?.toProjectIndicators(): List<Project.ProjectIndicator?>? {
    if (this == null) return null
    val list = ArrayList<Project.ProjectIndicator?>()
    this.forEach {
        list.add(Project.ProjectIndicator.fromIndicatorOverviewLine(it))
    }
    return list.toList()
}

/*
fun List<ProjectWorkPackageData>.toMultiExpAndActLists(): List<List<Project.MultiLanguagesExpectedAndActualAchievements>> {
    val back = ArrayList<List<Project.MultiLanguagesExpectedAndActualAchievements>>()
    this.forEach {
        back.add(it.specificObjective.toListOf<Project.MultiLanguagesExpectedAndActualAchievements>())
    }
    return back.toList()
}
 */

fun List<ProjectWorkPackageData>.toMultiExpLists(): List<List<Project.MultiLanguagesExpectedAchievements>> {
    val back = ArrayList<List<Project.MultiLanguagesExpectedAchievements>>()
    this.forEach {
        back.add(it.specificObjective.toListOf<Project.MultiLanguagesExpectedAchievements>())
    }
    return back.toList()
}

private fun ProjectData.backPartnerAndBudgetList(
    projectDataProvider: ProjectDataProvider,
    versionData: ProjectVersionData
): Pair<ArrayList<ProjectPartnerSummaryData>, ArrayList<ProjectPartnerBudgetOptionsData>> {
    val partners = this.sectionB.partners.toList()
    val partnersList = ArrayList<ProjectPartnerSummaryData>()
    val partnersBudgetList = ArrayList<ProjectPartnerBudgetOptionsData>()
    partners.forEach { partnerData ->
        partnerData.id?.let { mId ->
            partnersList.add(projectDataProvider.getProjectPartnerSummaryData(mId))
            projectDataProvider.getProjectPartnerBudgetOptions(mId, versionData.version)
                ?.let { it1 -> partnersBudgetList.add(it1) }
        }
    }
    return Pair(partnersList, partnersBudgetList)
}

internal fun List<ProjectVersionData>.toMainModel(
    programmeDataProvider: ProgrammeDataProvider,
    projectDataProvider: ProjectDataProvider,
    callDataProvider: CallDataProvider
): MainModel {
    val listInjected = ArrayList<MainModel.BasicModel>()
    this.forEach {
        val projectData = projectDataProvider.getProjectDataForProjectId(it.projectId, it.version)
        val (partnerData, budgetData) = projectData.backPartnerAndBudgetList(projectDataProvider, it)
        listInjected.add(
            MainModel.BasicModel(
                projectData,
                projectDataProvider.getProjectIdentificationData(it.projectId),
                projectDataProvider.getAllProjectVersions(),
                partnerData,
                budgetData,
                callDataProvider.getCallDataByProjectId(it.projectId)
            )
        )
    }
    return MainModel(programmeDataProvider.getProgrammeData(), listInjected.toList())
}


private typealias CamelCaseString = String