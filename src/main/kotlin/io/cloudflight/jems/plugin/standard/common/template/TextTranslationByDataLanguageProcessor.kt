package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.standard.common.DATA_LANGUAGE
import io.cloudflight.jems.plugin.standard.common.getTranslationFor
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

class TextTranslationByDataLanguageProcessor(defaultDialectPrefix: String) : AbstractAttributeTagProcessor(
    TemplateMode.HTML, defaultDialectPrefix,
    null, true,
    "textTranslationByDataLanguage", true,
    StandardDialect.PROCESSOR_PRECEDENCE, true
) {

    override fun doProcess(
        context: ITemplateContext?, tag: IProcessableElementTag?,
        attributeName: AttributeName?, attributeValue: String?,
        structureHandler: IElementTagStructureHandler?
    ) {
        if (context != null && attributeValue != null && structureHandler != null) {
            structureHandler.setBody(
                HtmlEscape.escapeHtml5Xml(
                    parseAttributeValue(attributeValue, context, emptySet<InputTranslationData>())
                        .getTranslationFor(context.getVariable(DATA_LANGUAGE) as SystemLanguageData)
                ),
                false
            )
        }
    }
}
