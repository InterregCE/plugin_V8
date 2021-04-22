package io.cloudflight.jems.plugin.default.pre_condition_check


import io.cloudflight.jems.plugin.pre_condition_check.PreConditionCheckPlugin
import io.cloudflight.jems.plugin.pre_condition_check.models.MessageType
import io.cloudflight.jems.plugin.pre_condition_check.models.PreConditionCheckMessage
import io.cloudflight.jems.plugin.pre_condition_check.models.PreConditionCheckResult
import org.springframework.stereotype.Service


@Service
open class PreConditionCheckDefaultImpl : PreConditionCheckPlugin {

    override fun check(projectId: Long): PreConditionCheckResult =
            PreConditionCheckResult(
                    listOf(
                            PreConditionCheckMessage(
                                    "external",
                                    MessageType.ERROR,
                                    emptyList()
                            )
                    ),
                    isSubmissionAllowed = true
            )

    override fun getDescription(): String =
            "Default implementation for pre condition check"

    override fun getKey() =
            "default-pre-condition-check-plugin"

    override fun getName() =
            "Default pre condition check"

    override fun getVersion(): String =
            "V1.0"
}
