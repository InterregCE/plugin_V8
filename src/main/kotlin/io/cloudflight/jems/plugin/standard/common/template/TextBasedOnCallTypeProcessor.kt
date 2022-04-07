package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.call.CallTypeData
import io.cloudflight.jems.plugin.standard.common.CALL_DATA
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

class TextBasedOnCallTypeProcessor(defaultDialectPrefix: String) : AbstractAttributeTagProcessor(
    TemplateMode.HTML, defaultDialectPrefix,
    null, true,
    "textBasedOnCallType", true,
    StandardDialect.PROCESSOR_PRECEDENCE, true
) {

    override fun doProcess(
        context: ITemplateContext?, tag: IProcessableElementTag?,
        attributeName: AttributeName?, attributeValue: String?,
        structureHandler: IElementTagStructureHandler?
    ) {
        if (context != null && attributeValue != null && structureHandler != null) {
            val parsedTranslationKey = parseAttributeValue(attributeValue, context)
            val translationKeyPrefix = (context.getVariable(CALL_DATA) as CallDetailData).let {
                if (it.type == CallTypeData.SPF) "spf." else ""
            }

            structureHandler.setBody(
                HtmlEscape.escapeHtml5Xml(
                    context.getMessage(
                        TextBasedOnCallTypeProcessor::class.java,
                        translationKeyPrefix.plus(parsedTranslationKey), arrayOfNulls(0), true
                    )
                ), false
            )
        }
    }
}
