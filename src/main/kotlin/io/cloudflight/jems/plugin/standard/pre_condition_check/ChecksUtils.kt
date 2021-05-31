package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.common.I18nMessageData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.results.ProjectResultTranslatedValueData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageActivityTranslatedValueData
import io.cloudflight.jems.plugin.contract.models.project.sectionC.workpackage.WorkPackageOutputTranslatedValueData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal
import java.math.RoundingMode

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

fun buildInfoPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.INFO, checkResults)

fun buildErrorPreConditionCheckMessage(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), vararg checkResults: PreConditionCheckMessage?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, *checkResults)

fun buildErrorPreConditionCheckMessages(
    messageKey: String, messageArgs: Map<String, String> = emptyMap(), checkResults: List<PreConditionCheckMessage>?
): PreConditionCheckMessage =
    buildPreConditionCheckMessage(messageKey, messageArgs, MessageType.ERROR, checkResults)

fun Set<InputTranslationData>?.isNullOrEmpty() =
    this == null || this.isEmpty() || this.any { it.translation.isNullOrBlank() }

fun Set<InputTranslationData>?.isNullOrEmptyOrMissingAnyTranslation() =
    this.isNullOrEmpty() || this!!.any { it.translation.isNullOrBlank() }

fun Set<ProjectResultTranslatedValueData>?.isResultNullOrEmptyOrMissingAnyDescription() =
    this.isNullOrEmpty() || this.any { it.description.isNullOrBlank() }

fun Set<WorkPackageActivityTranslatedValueData>?.isActivityNullOrEmptyOrMissingAnyDescriptionOrTitle() =
    this.isNullOrEmpty() || this.any { it.description.isNullOrBlank() || it.title.isNullOrEmpty() }

fun Set<WorkPackageOutputTranslatedValueData>?.isOutputNullOrEmptyOrMissingAnyDescriptionOrTitle() =
    this.isNullOrEmpty() || this.any { it.description.isNullOrBlank() || it.title.isNullOrEmpty() }

fun <T> Iterable<T>.sumOf(fieldExtractor: (T) -> BigDecimal?): BigDecimal =
    this.map { fieldExtractor.invoke(it) ?: BigDecimal.ZERO }.fold(BigDecimal.ZERO, BigDecimal::add)

fun BigDecimal.truncate(): BigDecimal =
    setScale(2, RoundingMode.FLOOR)

fun BigDecimal.truncateDown(): BigDecimal =
    setScale(2, RoundingMode.DOWN)

fun BigDecimal.percentage(percentage: Int): BigDecimal =
    multiply(BigDecimal.valueOf(percentage.toLong()))
        .divide(BigDecimal(100))
        .truncate()

fun BigDecimal.percentageDown(percentage: Int): BigDecimal =
    multiply(BigDecimal.valueOf(percentage.toLong()))
        .divide(BigDecimal(100))
        .truncateDown()