package io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common

import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistInstanceData
import java.math.BigDecimal

data class Checklist(
    val id: Long,
    val name: String,
    val maxScore: BigDecimal?,
    val minScore: BigDecimal?,
    val consolidated: Boolean,
    val allowsDecimalScore: Boolean,
    val status: io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistStatusData,
    val visible: Boolean,
    val questions: List<Question>
)

fun ChecklistInstanceData.toReportModel(): Checklist =
    Checklist(
        id,
        name,
        maxScore,
        minScore,
        consolidated,
        allowsDecimalScore,
        status,
        visible,
        questions.map { it.toReportModel() }.sortedBy { it.position }
    )