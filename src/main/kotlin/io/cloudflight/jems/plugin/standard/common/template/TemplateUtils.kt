package io.cloudflight.jems.plugin.standard.common.template

import io.cloudflight.jems.plugin.contract.models.call.CallDetailData
import io.cloudflight.jems.plugin.contract.models.common.InputTranslationData
import io.cloudflight.jems.plugin.contract.models.common.SystemLanguageData
import io.cloudflight.jems.plugin.contract.models.project.lifecycle.ProjectLifecycleData
import io.cloudflight.jems.plugin.standard.common.*
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.IStandardExpression
import org.thymeleaf.standard.expression.IStandardExpressionParser
import org.thymeleaf.standard.expression.StandardExpressions
import io.cloudflight.jems.plugin.contract.models.project.ApplicationFormFieldId
import java.math.BigDecimal

const val CLF_UTILS = "clfUtils"

class TemplateUtils {

    fun getEnglishTranslation(translationData: Set<InputTranslationData>) =
        translationData.getTranslationFor(SystemLanguageData.EN)

    fun isSectionCAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData): Boolean{
        return  isSectionC1Available(lifecycleData, callData) ||
                isSectionC2Available(lifecycleData, callData) ||
                isFieldAvailable("PROJECT_PARTNERSHIP", lifecycleData, callData) ||
                isSectionC4Available(lifecycleData, callData) ||
                isProjectResultsSectionAvailable(lifecycleData, callData) ||
                isSectionC7Available(lifecycleData, callData) ||
                isLongTermPlansSectionAvailable(lifecycleData, callData)
    }
    fun isSectionC1Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectOverallObjectiveSectionVisible(lifecycleData, callData)
    fun isSectionC2Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectRelevanceSectionVisible(lifecycleData, callData)

    fun isSectionC4Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectWorkPackageSectionVisible(lifecycleData, callData)

    fun isSectionC7Available(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectManagementSectionVisible(lifecycleData, callData)

    fun isProjectResultsSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isProjectResultsSectionVisible(lifecycleData, callData)

    fun isLongTermPlansSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData)=
        isLongTermPlansSectionVisible(lifecycleData, callData)

    fun isInvestmentSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isInvestmentSectionVisible(lifecycleData, callData) || isInvestmentLocationAvailable(lifecycleData, callData)

    fun isInvestmentLocationAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isInvestmentLocationVisible(lifecycleData, callData)

    fun isInvestmentJustificationAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isInvestmentJustificationVisible(lifecycleData, callData)

    fun isOutputsSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isWorkPlanOutputsSectionVisible(lifecycleData, callData)

    fun isActivitiesSectionAvailable(lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isWorkPlanActivitiesSectionVisible(lifecycleData, callData)

    fun isFieldAvailable(fieldId: String,lifecycleData: ProjectLifecycleData, callData: CallDetailData) =
        isFieldVisible(ApplicationFormFieldId.valueOf(fieldId), lifecycleData, callData)

    fun intToBigDecimal(value: Int?): BigDecimal? {
        return if(value is Int) {
            BigDecimal(value)
        } else {
            null
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> parseAttributeValue(attributeValue: String, context: ITemplateContext, defaultValue: T): T =
    parseAttributeValue(attributeValue, context)?.let {
            it as T
        } ?: defaultValue

fun parseAttributeValue(attributeValue: String, context: ITemplateContext): Any? {
    val configuration: IEngineConfiguration = context.configuration
    val parser: IStandardExpressionParser = StandardExpressions.getExpressionParser(configuration)
    val expression: IStandardExpression = parser.parseExpression(context, attributeValue)
    return expression.execute(context)
}

