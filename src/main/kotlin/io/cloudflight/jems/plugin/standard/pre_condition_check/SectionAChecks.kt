package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityDataSimple
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectDataSectionA
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage

private const val SECTION_A_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.a"
private const val SECTION_A_ERROR_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.error"
private const val SECTION_A_INFO_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.info"

private var _lifecycleData: ProjectLifecycleData? = null
private var _callData: CallDetailData? = null

fun checkSectionA(sectionAData: ProjectDataSectionA?, lifecycleData: ProjectLifecycleData, callData: CallDetailData): PreConditionCheckMessage {
    _lifecycleData = lifecycleData
    _callData = callData

    return buildPreConditionCheckMessage(
        messageKey = "$SECTION_A_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfTitleIsProvided(callData.inputLanguages, sectionAData?.title),

        checkIfAcronymIsProvided(sectionAData?.acronym),

        checkIfDurationIsProvided(sectionAData?.duration),

        checkIfProgrammePriorityIsProvided(sectionAData?.programmePriority),

        checkIfIntroIsProvidedInEnglish(sectionAData?.intro),

        checkIfIntroIsProvided(callData.inputLanguages, sectionAData?.intro)
    )
}

private fun checkIfTitleIsProvided(mandatoryLanguages: Set<SystemLanguageData>, title: Set<InputTranslationData>?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_TITLE) && title.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.title.is.not.provided")
        else -> null
    }

private fun checkIfAcronymIsProvided(acronym: String?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_ACRONYM) &&
        acronym.isNullOrBlank() -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.acronym.is.not.provided")
        else -> null
    }

private fun checkIfDurationIsProvided(duration: Int?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_DURATION) &&
        duration == null -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.duration.is.not.provided")
        else -> null
    }

private fun checkIfProgrammePriorityIsProvided(programmePriority: ProgrammePriorityDataSimple?) =
    when {
        isFieldVisible(ApplicationFormFieldId.PROJECT_PRIORITY) &&
        (programmePriority == null || programmePriority.code.isBlank()) -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.programme.priority.is.not.provided")
        else -> null
    }

private fun checkIfIntroIsProvidedInEnglish(intro: Set<InputTranslationData>?) =
    when {
        intro.isNotFullyTranslated(setOf(SystemLanguageData.EN))
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.intro.in.en.is.not.provided")
        else -> null
    }

private fun checkIfIntroIsProvided(mandatoryLanguages: Set<SystemLanguageData>, intro: Set<InputTranslationData>?) =
    when {
        intro.isNotFullyTranslated(mandatoryLanguages)
        -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.intro.is.not.provided")
        else -> null
    }

private fun isFieldVisible(fieldId: ApplicationFormFieldId): Boolean {
    return isFieldVisible(fieldId, _lifecycleData!!, _callData!!)
}
