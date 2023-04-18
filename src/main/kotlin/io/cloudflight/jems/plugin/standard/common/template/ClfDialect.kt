package io.cloudflight.jems.plugin.standard.common.template

import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.standard.StandardDialect

const val CLF_DIALECT_PREFIX = "clf"

class ClfDialect : AbstractProcessorDialect(
    "Cloudflight thymeleaf dialect", CLF_DIALECT_PREFIX, StandardDialect.PROCESSOR_PRECEDENCE
) {

    override fun getProcessors(dialectPrefix: String?): MutableSet<IProcessor> =
        mutableSetOf<IProcessor>().also {
            it.add(FieldVisibilityProcessor(dialectPrefix!!))
            it.add(TextTranslationByDataLanguageProcessor(dialectPrefix))
            it.add(TextTranslationByExportLanguageProcessor(dialectPrefix))
            it.add(NumberProcessor(dialectPrefix))
            it.add(LeftAlignedNumberProcessor(dialectPrefix))
            it.add(PercentageProcessor(dialectPrefix))
            it.add(TextApplicationFormTranslationProcessor(dialectPrefix))
            it.add(TextBasedOnCallTypeProcessor(dialectPrefix))
        }
}
