package io.cloudflight.jems.plugin.standard.pre_condition_check


import io.cloudflight.jems.plugin.contract.pre_condition_check.PreConditionCheckPlugin
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckResult
import io.cloudflight.jems.plugin.contract.services.ProjectDataProvider
import org.springframework.stereotype.Service


@Service
open class PreConditionCheckDefaultImpl(val projectDataProvider: ProjectDataProvider) : PreConditionCheckPlugin {

    override fun check(projectId: Long): PreConditionCheckResult {
        val projectData = projectDataProvider.getProjectDataForProjectId(projectId)

        if (projectData.sectionA == null ||
            projectData.sectionA!!.title.isEmpty() ||
            projectData.sectionA!!.title.all { it.translation.isNullOrBlank() }
        ) {
            return PreConditionCheckResult(
                listOf(
                    PreConditionCheckMessage(
                        "plugin.section.a.project.title.not.defined",
                        MessageType.ERROR,
                        emptyList()
                    )
                ),
                isSubmissionAllowed = false
            )
        }

        return PreConditionCheckResult(
            emptyList(),
            isSubmissionAllowed = true
        )
    }

    override fun getDescription(): String =
        "Standard implementation for pre condition check"

    override fun getKey() =
        "standard-pre-condition-check-plugin"

    override fun getName() =
        "Standard pre condition check"

    override fun getVersion(): String =
        "1.0.0"
}
