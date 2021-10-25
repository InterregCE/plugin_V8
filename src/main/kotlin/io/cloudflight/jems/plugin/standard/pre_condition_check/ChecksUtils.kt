package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.I18nMessageData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ApplicationStatusData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage


fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    mutableListOf<PreConditionCheckMessage>().plus(
        checkResults.filterNotNull()
    ).let { subSectionMessages ->
        PreConditionCheckMessage(
            I18nMessageData(messageKey, messageArgs),
            subSectionMessages.firstOrNull { it.messageType == MessageType.ERROR }?.messageType ?: MessageType.INFO,
            subSectionMessages
        )
    }

fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), messageType: MessageType,
    vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    PreConditionCheckMessage(
        I18nMessageData(messageKey, messageArgs), messageType,
        mutableListOf<PreConditionCheckMessage>().plus(checkResults.filterNotNull())
    )

fun buildPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), messageType: MessageType,
    checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    PreConditionCheckMessage(
        I18nMessageData(messageKey, messageArgs), messageType, checkResults ?: emptyList()
    )

fun buildInfoPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.INFO, *checkResults)

fun buildErrorPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, *checkResults)

fun buildErrorPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, checkResults)

fun Set<InputTranslationData>?.isNotFullyTranslated(mandatoryLanguages: Set<SystemLanguageData>): Boolean {
    return !this.isFullyTranslated(mandatoryLanguages)
}
fun Set<InputTranslationData>?.isFullyTranslated(mandatoryLanguages: Set<SystemLanguageData>): Boolean {
    if (this.isNullOrEmpty()) return false
    val allLanguagesUsed = this.mapTo(HashSet()) { it.language }.containsAll(mandatoryLanguages)
    val allLanguagesTranslated = this.filter { mandatoryLanguages.contains(it.language) }.all { !it.translation.isNullOrBlank() }
    return allLanguagesUsed && allLanguagesTranslated
}

fun CallDetailData.isTwoStepCall()=
    this.endDateTimeStep1 != null

fun ApplicationStatusData.isInStepOne() =
    this == ApplicationStatusData.STEP1_DRAFT || this == ApplicationStatusData.STEP1_SUBMITTED || this == ApplicationStatusData.STEP1_ELIGIBLE || this == ApplicationStatusData.STEP1_INELIGIBLE || this == ApplicationStatusData.STEP1_APPROVED
            || this == ApplicationStatusData.STEP1_APPROVED_WITH_CONDITIONS || this == ApplicationStatusData.STEP1_NOT_APPROVED
