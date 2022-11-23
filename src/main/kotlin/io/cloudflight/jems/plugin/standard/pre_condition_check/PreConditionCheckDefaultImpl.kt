package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.pre_condition_check.PreConditionCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.CallDataProvider
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.CallDataContainer
import io.cloudflight.jems.plugin.standard.pre_condition_check.helpers.LifecycleDataContainer
import org.springframework.stereotype.Service

const val MESSAGES_PREFIX = "jems.standard.pre.condition.check.plugin.project"

@Service
open class PreConditionCheckDefaultImpl(
    val projectDataProvider: ProjectDataProvider,
    val callDataProvider: CallDataProvider
) : PreConditionCheckPlugin {

    override fun check(projectId: Long): PreConditionCheckResult =
        projectDataProvider.getProjectDataForProjectId(projectId).let { projectData ->
            val callData = callDataProvider.getCallDataByProjectId(projectId)
            CallDataContainer.set(callData)
            LifecycleDataContainer.set(projectData.lifecycleData)
            val messages = mutableListOf<PreConditionCheckMessage>()
            if (projectData.lifecycleData.status.isInStepOne()) {
                messages.add(checkAcronymForStepOne(projectData.sectionA?.acronym))
            } else {
                messages.add(checkSectionA(projectData.sectionA))
                messages.add(checkSectionB(projectData.sectionB))
                messages.add(checkSectionC(projectData.sectionC))
                messages.add(checkSectionE(projectData.sectionE))
            }
            return PreConditionCheckResult(
                messages = messages,
                isSubmissionAllowed = messages.none { it.messageType == MessageType.ERROR }
            )
        }

    override fun getDescription(): String =
        "Standard implementation for pre condition check"

    override fun getKey() =
        "standard-pre-condition-check-plugin"

    override fun getName() =
        "Standard pre condition check"

    override fun getVersion(): String =
        "1.0.24"
}
