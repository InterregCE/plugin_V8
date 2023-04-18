package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.standard.common.CALL_DATA
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

class TextApplicationFormTranslationProcessor(defaultDialectPrefix: String) : AbstractAttributeTagProcessor(
    TemplateMode.HTML, defaultDialectPrefix,
    null, true,
    "textApplicationFormTranslation", true,
    StandardDialect.PROCESSOR_PRECEDENCE, true
) {

    override fun doProcess(
        context: ITemplateContext?, tag: IProcessableElementTag?,
        attributeName: AttributeName?, attributeValue: String?,
        structureHandler: IElementTagStructureHandler?
    ) {
        if (context != null && attributeValue != null && structureHandler != null) {
            val translationKey = parseAttributeValue(attributeValue, context)?.toString() ?: ""
            val callSpecificKey = "call-id-${(context.getVariable(CALL_DATA) as CallDetailData).id}.$translationKey"

            val translation = HtmlEscape.escapeHtml5Xml(
                context.translate(callSpecificKey) ?: context.translate(translationKey) ?: translationKey
            )

            if (tag?.elementCompleteName == "bookmark")
                structureHandler.setAttribute("name", translation)
            else
                structureHandler.setBody(translation, false)
        }
    }

    private fun ITemplateContext.translate(key: String) = getMessage(
        TextApplicationFormTranslationProcessor::class.java,
        key, arrayOfNulls(0), false
    )

}
