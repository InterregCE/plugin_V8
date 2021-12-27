package io.cloudflight.jems.plugin.standard.common

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.FieldVisibilityStatusData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.standard.pre_condition_check.isInStepOne
import io.cloudflight.jems.plugin.standard.pre_condition_check.isTwoStepCall


fun isFieldVisible(
    fieldId: ApplicationFormFieldId, lifecycleData: ProjectLifecycleData, callData: CallDetailData
): Boolean {
    val fieldConfiguration = callData.applicationFormFieldConfigurations.firstOrNull { it.id == fieldId.id }
        ?: return false
    return when {
        fieldConfiguration.visibilityStatus == FieldVisibilityStatusData.NONE -> false
        callData.isTwoStepCall() && lifecycleData.status.isInStepOne() && fieldConfiguration.visibilityStatus == FieldVisibilityStatusData.STEP_TWO_ONLY -> false
        else -> true
    }
}

fun isAnyFieldVisible(
    fieldIds: Set<ApplicationFormFieldId>, lifecycleData: ProjectLifecycleData, callData: CallDetailData
): Boolean =
    fieldIds.any { isFieldVisible(it, lifecycleData, callData) }

fun isProjectRelevanceSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_TERRITORIAL_CHALLENGES,
    ApplicationFormFieldId.PROJECT_HOW_ARE_CHALLENGES_AND_OPPORTUNITIES_TACKLED,
    ApplicationFormFieldId.PROJECT_WHY_IS_COOPERATION_NEEDED,
    ApplicationFormFieldId.PROJECT_TARGET_GROUP,
    ApplicationFormFieldId.PROJECT_STRATEGY_CONTRIBUTION,
    ApplicationFormFieldId.PROJECT_SYNERGIES,
    ApplicationFormFieldId.PROJECT_HOW_BUILDS_PROJECT_ON_AVAILABLE_KNOWLEDGE,
).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isProjectWorkPackageSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    isWorkPlanObjectiveSectionVisible(lifecycleData, callData) ||
            isInvestmentSectionVisible(lifecycleData, callData) ||
            isWorkPlanActivitiesSectionVisible(lifecycleData, callData) ||
            isWorkPlanOutputsSectionVisible(lifecycleData, callData)

fun isWorkPlanObjectiveSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_WORK_PACKAGE_TITLE,
    ApplicationFormFieldId.PROJECT_SPECIFIC_OBJECTIVE,
    ApplicationFormFieldId.PROJECT_COMMUNICATION_OBJECTIVES_AND_TARGET_AUDIENCE
).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isInvestmentSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    listOf(
        ApplicationFormFieldId.PROJECT_INVESTMENT_TITLE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHY_IS_INVESTMENT_NEEDED,
        ApplicationFormFieldId.PROJECT_INVESTMENT_PERIOD,
        ApplicationFormFieldId.PROJECT_INVESTMENT_CROSS_BORDER_TRANSNATIONAL_RELEVANCE_OF_INVESTMENT,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_IS_BENEFITING,
        ApplicationFormFieldId.PROJECT_INVESTMENT_PILOT_CLARIFICATION,
        ApplicationFormFieldId.PROJECT_INVESTMENT_RISK,
        ApplicationFormFieldId.PROJECT_INVESTMENT_DOCUMENTATION,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_OWNS_THE_INVESTMENT_SITE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_OWNERSHIP_AFTER_END_OF_PROJECT,
        ApplicationFormFieldId.PROJECT_INVESTMENT_MAINTENANCE,
    ).any {
        isFieldVisible(it, lifecycleData, callData)
    }

fun isInvestmentLocationVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    listOf(
        ApplicationFormFieldId.PROJECT_INVESTMENT_COUNTRY,
        ApplicationFormFieldId.PROJECT_INVESTMENT_STREET,
        ApplicationFormFieldId.PROJECT_INVESTMENT_HOUSE_NUMBER,
        ApplicationFormFieldId.PROJECT_INVESTMENT_POSTAL_CODE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_CITY,
    ).any {
        isFieldVisible(it, lifecycleData, callData)
    }

fun isInvestmentJustificationVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    listOf(
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHY_IS_INVESTMENT_NEEDED,
        ApplicationFormFieldId.PROJECT_INVESTMENT_CROSS_BORDER_TRANSNATIONAL_RELEVANCE_OF_INVESTMENT,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_IS_BENEFITING,
        ApplicationFormFieldId.PROJECT_INVESTMENT_PILOT_CLARIFICATION,
    ).any {
        isFieldVisible(it, lifecycleData, callData)
    }


fun isWorkPlanActivitiesSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_ACTIVITIES_TITLE,
    ApplicationFormFieldId.PROJECT_ACTIVITIES_DESCRIPTION,
    ApplicationFormFieldId.PROJECT_ACTIVITIES_START_PERIOD,
    ApplicationFormFieldId.PROJECT_ACTIVITIES_END_PERIOD,
    ApplicationFormFieldId.PROJECT_ACTIVITIES_STATE_AID_PARTNERS_INVOLVED,
    ApplicationFormFieldId.PROJECT_ACTIVITIES_DELIVERABLES
).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isWorkPlanOutputsSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_OUTPUT_TITLE,
    ApplicationFormFieldId.PROJECT_OUTPUT_PROGRAMME_OUTPUT_INDICATOR_AND_MEASUREMENT_UNIT,
    ApplicationFormFieldId.PROJECT_OUTPUT_TARGET_VALUE,
    ApplicationFormFieldId.PROJECT_OUTPUT_DELIVERY_PERIOD,
    ApplicationFormFieldId.PROJECT_OUTPUT_DESCRIPTION

).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isProjectResultsSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_RESULTS_PROGRAMME_RESULT_INDICATOR_AMD_MEASUREMENT_UNIT,
    ApplicationFormFieldId.PROJECT_RESULTS_TARGET_VALUE,
    ApplicationFormFieldId.PROJECT_RESULTS_BASELINE,
    ApplicationFormFieldId.PROJECT_RESULTS_DELIVERY_PERIOD,
    ApplicationFormFieldId.PROJECT_RESULTS_DESCRIPTION
).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isProjectManagementSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_COORDINATION,
    ApplicationFormFieldId.PROJECT_QUALITY_MEASURES,
    ApplicationFormFieldId.PROJECT_COMMUNICATION_APPROACH,
    ApplicationFormFieldId.PROJECT_FINANCIAL_MANAGEMENT_AND_REPORTING,
    ApplicationFormFieldId.PROJECT_COOPERATION_CRITERIA,
    ApplicationFormFieldId.PROJECT_HORIZONTAL_PRINCIPLES,
).any {
    isFieldVisible(it, lifecycleData, callData)
}

fun isLongTermPlansSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) = listOf(
    ApplicationFormFieldId.PROJECT_OWNERSHIP,
    ApplicationFormFieldId.PROJECT_DURABILITY,
    ApplicationFormFieldId.PROJECT_TRANSFERABILITY,
).any {
    isFieldVisible(it, lifecycleData, callData)
}

