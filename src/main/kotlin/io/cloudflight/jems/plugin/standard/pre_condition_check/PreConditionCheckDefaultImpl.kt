package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.pre_condition_check.PreConditionCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import org.springframework.stereotype.Service

const val MESSAGES_PREFIX = "jems.standard.pre.condition.check.plugin.project"

@Service
open class PreConditionCheckDefaultImpl(val projectDataProvider: ProjectDataProvider) : PreConditionCheckPlugin {

    override fun check(projectId: Long): PreConditionCheckResult =
        projectDataProvider.getProjectDataForProjectId(projectId).let { projectData ->
            mutableListOf<PreConditionCheckMessage>().plus(
                arrayOf(
                    checkSectionA(projectData.sectionA),
                    checkSectionB(projectData.sectionB),
                    checkSectionC(projectData.sectionC),
                    checkSectionE(projectData.sectionE)
                )
            ).let { messages ->
                PreConditionCheckResult(
                    messages = messages,
                    isSubmissionAllowed = messages.none { it.messageType == MessageType.ERROR }
                )
            }
        }

    override fun getDescription(): String =
        "Standard implementation for pre condition check"

    override fun getKey() =
        "standard-pre-condition-check-plugin"

    override fun getName() =
        "Standard pre condition check"

    override fun getVersion(): String =
        "1.0.2"
}
