package io.cloudflight.jems.plugin.standard.pre_condition_check

import io.cloudflight.jems.plugin.contract.models.project.sectionE.ProjectDataSectionE
import io.cloudflight.jems.plugin.contract.models.project.sectionE.lumpsum.ProjectLumpSumData
import io.cloudflight.jems.plugin.contract.pre_condition_check.models.PreConditionCheckMessage
import java.math.BigDecimal

private const val SECTION_E_MESSAGES_PREFIX = "$MESSAGES_PREFIX.section.e"
private const val SECTION_E_ERROR_MESSAGES_PREFIX = "$SECTION_E_MESSAGES_PREFIX.error"
private const val SECTION_E_INFO_MESSAGES_PREFIX = "$SECTION_E_MESSAGES_PREFIX.info"

fun checkSectionE(sectionEData: ProjectDataSectionE): PreConditionCheckMessage =
    buildPreConditionCheckMessage(
        messageKey = "$SECTION_E_MESSAGES_PREFIX.header", messageArgs = emptyMap(),
        checkIfPartnersShareSumUpToTotalLumpSum(sectionEData.projectLumpSums)
    )

private fun checkIfPartnersShareSumUpToTotalLumpSum(lumpSums: List<ProjectLumpSumData>) =
    when {
        lumpSums.any { lumpSum -> lumpSum.programmeLumpSum?.cost ?: BigDecimal.ZERO != lumpSum.lumpSumContributions.sumOf { it.amount } } ->
            buildErrorPreConditionCheckMessage("$SECTION_E_ERROR_MESSAGES_PREFIX.partner.amount.do.not.sum.up.to.budget.entry.sum")
        else -> buildInfoPreConditionCheckMessage("$SECTION_E_INFO_MESSAGES_PREFIX.periods.amount.sum.up.to.budget.entry.sum")
    }