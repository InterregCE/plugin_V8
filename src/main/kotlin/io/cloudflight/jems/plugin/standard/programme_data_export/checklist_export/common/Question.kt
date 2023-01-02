package io.cloudflight.jems.plugin.standard.programme_data_export.checklist_export.common

import com.google.gson.Gson
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionTypeData
import java.math.BigDecimal
import java.math.RoundingMode

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

class HeadlineMetadata(val value: String?)

class OptionsToggleMetadata(
    var question: String?,
    val firstOption: String?,
    val secondOption: String?,
    val thirdOption: String?,
    val justification: String?
)

class ScoreMetadata (
    var question: String?,
    val weight: BigDecimal?,
)

class TextInputMetadata (
    var question: String?,
    val explanationLabel: String?,
    val explanationMaxLength: Int?,
)

class AnswerMetadata (
    val answer: String?,
    val justification: String?,
    val explanation: String?,
    val score: BigDecimal?,
    var weightedScore: BigDecimal?
)

val gson = Gson()

fun ChecklistQuestionData.toReportModel(): Question {

    var headlineMetadata = if (type == ChecklistQuestionTypeData.HEADLINE && !questionMetadataJson.isNullOrEmpty()) gson.fromJson(
        answerMetadataJson,
        HeadlineMetadata::class.java
    ) else null

    var optionsToggleMetadata = if (type == ChecklistQuestionTypeData.OPTIONS_TOGGLE && !questionMetadataJson.isNullOrEmpty()) gson.fromJson(
        answerMetadataJson,
        OptionsToggleMetadata::class.java
    ) else null

    var scoreMetadata = if (type == ChecklistQuestionTypeData.SCORE && !questionMetadataJson.isNullOrEmpty()) gson.fromJson(
        answerMetadataJson,
        ScoreMetadata::class.java
    ) else null

    var textInputMetadata = if (type == ChecklistQuestionTypeData.TEXT_INPUT && !questionMetadataJson.isNullOrEmpty()) gson.fromJson(
        answerMetadataJson,
        TextInputMetadata::class.java
    ) else null

    var answerMetadata = if (!answerMetadataJson.isNullOrEmpty()) gson.fromJson(
        questionMetadataJson,
        AnswerMetadata::class.java
    ) else null

    if (scoreMetadata != null && answerMetadata != null) {
        answerMetadata!!.weightedScore = (answerMetadata.score ?: BigDecimal.ZERO).multiply(scoreMetadata.weight ?: BigDecimal.ZERO)
    }

    return Question(
        id,
        position,
        weight,
        question,
        score,
        type,

        headlineMetadata,
        optionsToggleMetadata,
        scoreMetadata,
        textInputMetadata,

        answerMetadata,
    )
}
