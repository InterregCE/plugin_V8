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


fun isInvestmentSectionVisible(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
    listOf(
        ApplicationFormFieldId.PROJECT_INVESTMENT_TITLE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHY_IS_INVESTMENT_NEEDED,
        ApplicationFormFieldId.PROJECT_INVESTMENT_CROSS_BORDER_TRANSNATIONAL_RELEVANCE_OF_INVESTMENT,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_IS_BENEFITING,
        ApplicationFormFieldId.PROJECT_INVESTMENT_PILOT_CLARIFICATION,
        ApplicationFormFieldId.PROJECT_INVESTMENT_COUNTRY,
        ApplicationFormFieldId.PROJECT_INVESTMENT_STREET,
        ApplicationFormFieldId.PROJECT_INVESTMENT_HOUSE_NUMBER,
        ApplicationFormFieldId.PROJECT_INVESTMENT_POSTAL_CODE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_CITY,
        ApplicationFormFieldId.PROJECT_INVESTMENT_RISK,
        ApplicationFormFieldId.PROJECT_INVESTMENT_DOCUMENTATION,
        ApplicationFormFieldId.PROJECT_INVESTMENT_WHO_OWNS_THE_INVESTMENT_SITE,
        ApplicationFormFieldId.PROJECT_INVESTMENT_OWNERSHIP_AFTER_END_OF_PROJECT,
        ApplicationFormFieldId.PROJECT_INVESTMENT_MAINTENANCE,
    ).any {
        isFieldVisible(it, lifecycleData, callData)
    }
