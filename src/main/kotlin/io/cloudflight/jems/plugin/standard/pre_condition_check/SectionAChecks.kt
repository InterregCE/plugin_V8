package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.programme.priority.ProgrammePriorityDataSimple
import io.cloudflight.jems.plugin.contract.models.project.sectionA.ProjectDataSectionA
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage

private const val SECTION_A_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.a"
private const val SECTION_A_ERROR_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.error"
private const val SECTION_A_INFO_MESSAGES_PREFIX = "$SECTION_A_MESSAGES_PREFIX.info"

fun checkSectionA(sectionAData: ProjectDataSectionA?): PreConditionCheckMessage =
    buildPreConditionCheckMessage(
        messageKey = "$SECTION_A_MESSAGES_PREFIX.header", messageArgs = emptyMap(),

        checkIfTitleIsProvided(sectionAData?.title),

        checkIfAcronymIsProvided(sectionAData?.acronym),

        checkIfDurationIsProvided(sectionAData?.duration),

        checkIfProgrammePriorityIsProvided(sectionAData?.programmePriority),

        checkIfIntroIsProvidedInEnglish(sectionAData?.intro)
    )


private fun checkIfTitleIsProvided(title: Set<InputTranslationData>?) =
    when {
        title.isNullOrEmptyOrMissingAnyTranslation() -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.title.is.not.provided")
        else -> null
    }

private fun checkIfAcronymIsProvided(acronym: String?) =
    when {
        acronym.isNullOrBlank() -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.acronym.is.not.provided")
        else -> null
    }

private fun checkIfDurationIsProvided(duration: Int?) =
    when (duration) {
        null -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.duration.is.not.provided")
        else -> null
    }

private fun checkIfProgrammePriorityIsProvided(programmePriority: ProgrammePriorityDataSimple?) =
    when {
        programmePriority == null || programmePriority.code.isBlank() -> buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.programme.priority.is.not.provided")
        else -> null
    }

private fun checkIfIntroIsProvidedInEnglish(intro: Set<InputTranslationData>?) =
    when {
        intro.isNullOrEmpty() || intro!!.first { it.language == SystemLanguageData.EN }.translation.isNullOrBlank() ->
            buildErrorPreConditionCheckMessage("$SECTION_A_ERROR_MESSAGES_PREFIX.intro.in.en.is.not.provided")
        else -> buildInfoPreConditionCheckMessage("$SECTION_A_INFO_MESSAGES_PREFIX.intro.in.en.is.provided")
    }