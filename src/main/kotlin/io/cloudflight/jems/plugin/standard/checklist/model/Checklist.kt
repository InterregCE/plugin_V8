package io.cloudflight.jems.plugin.standard.checklist.model

import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistStatusData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistTypeData
import java.math.BigDecimal
import java.time.LocalDate

data class Checklist(
    val id: Long,
    val name: String,
    val type: ChecklistTypeData,
    val creatorEmail: String,
    val maxScore: BigDecimal?,
    val minScore: BigDecimal?,
    val consolidated: Boolean,
    val allowsDecimalScore: Boolean,
    val status: ChecklistStatusData,
    val finishedDate: LocalDate?,
    val visible: Boolean,
    val questions: List<Question>
)