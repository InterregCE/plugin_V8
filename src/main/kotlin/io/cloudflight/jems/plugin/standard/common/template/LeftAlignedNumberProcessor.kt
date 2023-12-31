package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.standard.common.format
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape
import java.math.BigDecimal

class LeftAlignedNumberProcessor(defaultDialectPrefix: String) : AbstractAttributeTagProcessor(
    TemplateMode.HTML, defaultDialectPrefix,
    null, true,
    "leftAlignedNumber", true,
    StandardDialect.PROCESSOR_PRECEDENCE - 50, true
) {

    override fun doProcess(
        context: ITemplateContext?, tag: IProcessableElementTag?,
        attributeName: AttributeName?, attributeValue: String?,
        structureHandler: IElementTagStructureHandler?
    ) {
        if (context != null && attributeValue != null && structureHandler != null) {
            structureHandler.setBody(
                HtmlEscape.escapeHtml5(
                    parseAttributeValue(attributeValue, context)?.let {
                        (it as BigDecimal).format()
                    } ?: ""
                ), false
            )
        }
    }
}