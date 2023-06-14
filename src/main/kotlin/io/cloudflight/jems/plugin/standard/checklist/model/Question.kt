package io.cloudflight.jems.plugin.standard.checklist.model

import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionTypeData
import java.math.BigDecimal

data class Question(
    val id: Long,
    val position: Int,
    val weight: BigDecimal?,
    var question: String?,
    val score: BigDecimal?,
    val type: ChecklistQuestionTypeData,

    val headlineMetadata: HeadlineMetadata?,
    val optionsToggleMetadata: OptionsToggleMetadata?,
    val scoreMetadata: ScoreMetadata?,
    val textInputMetadata: TextInputMetadata?,

    val answerMetadata: AnswerMetadata?
)