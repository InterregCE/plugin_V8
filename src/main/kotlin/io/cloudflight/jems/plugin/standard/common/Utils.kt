package io.cloudflight.jems.plugin.standard.common

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.standard.budget_export.FALL_BACK_LANGUAGE
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*


fun SystemLanguageData.toLocale() =
    Locale(name.toLowerCase())

fun Set<InputTranslationData>.getTranslationFor(
    language: SystemLanguageData, useFallBackLanguage: Boolean = true
): String =
    this.firstOrNull { it.language == language }?.translation
        ?: if (useFallBackLanguage) this.firstOrNull { it.language == FALL_BACK_LANGUAGE }?.translation
            ?: "" else ""

fun getMessagesWithoutArgs(messageSource: MessageSource, exportLocale: Locale, vararg keys: String) =
    keys.map {
        messageSource.getMessage(it, null, exportLocale)
    }

fun getMessage(key: String, exportLocale: Locale, messageSource: MessageSource, args: Array<out Any>? = null) =
    messageSource.getMessage(key, args, exportLocale)

fun <T> Iterable<T>.sumOf(fieldExtractor: (T) -> BigDecimal?): BigDecimal =
    this.map { fieldExtractor.invoke(it) ?: BigDecimal.ZERO }.fold(BigDecimal.ZERO, BigDecimal::add)

fun BigDecimal.percentageTo(total: BigDecimal, roundingMode: RoundingMode = RoundingMode.DOWN): BigDecimal =
    if (total > BigDecimal.ZERO) BigDecimal(100).multiply(this).divide(total, 2, roundingMode) else BigDecimal.ZERO

fun BigDecimal.percentageDown(percentage: BigDecimal): BigDecimal =
    multiply(percentage)
        .divide(BigDecimal(100), 2, RoundingMode.DOWN)

fun BigDecimal.format(locale: Locale): String =
    NumberFormat.getInstance(locale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
        isGroupingUsed = true
    }.format(this)