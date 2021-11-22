package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import io.cloudflight.jems.plugin.contract.models.project.ProjectData
import io.cloudflight.jems.plugin.standard.common.isAnyFieldVisible
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode

class FieldVisibilityProcessor(defaultDialectPrefix: String) : AbstractAttributeTagProcessor(
    TemplateMode.HTML, defaultDialectPrefix,
    null, true,
    "ifAnyFieldIsVisible", true,
    StandardDialect.PROCESSOR_PRECEDENCE + 100, true
) {

    override fun doProcess(
        context: ITemplateContext?, tag: IProcessableElementTag?,
        attributeName: AttributeName?, attributeValue: String?,
        structureHandler: IElementTagStructureHandler?
    ) {
        if (context != null && attributeValue != null && structureHandler != null) {
            if (!isAnyFieldVisible(
                    attributeValue.split(",").map { fieldId -> ApplicationFormFieldId.valueOf(fieldId.trim()) }.toSet(),
                    lifecycleData = (context.getVariable(PROJECT_DATA) as ProjectData).lifecycleData,
                    callData = context.getVariable(CALL_DATA) as CallDetailData
                )
            ) structureHandler.removeElement()
        }
    }
}