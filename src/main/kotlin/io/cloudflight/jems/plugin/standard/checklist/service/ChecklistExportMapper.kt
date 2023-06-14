package io.cloudflight.jems.plugin.standard.checklist.service

import com.google.gson.Gson
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistInstanceData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionData
import io.cloudflight.jems.plugin.contract.models.project.checklist.ChecklistQuestionTypeData
import io.cloudflight.jems.plugin.standard.checklist.model.AnswerMetadata
import io.cloudflight.jems.plugin.standard.checklist.model.Checklist
import io.cloudflight.jems.plugin.standard.checklist.model.HeadlineMetadata
import io.cloudflight.jems.plugin.standard.checklist.model.OptionsToggleMetadata
import io.cloudflight.jems.plugin.standard.checklist.model.Question
import io.cloudflight.jems.plugin.standard.checklist.model.ScoreMetadata
import io.cloudflight.jems.plugin.standard.checklist.model.TextInputMetadata
import java.math.BigDecimal

private val gson = Gson()

private fun <T> ChecklistQuestionData.extractQuestionMetadata(questionType: ChecklistQuestionTypeData, answerType: Class<T>): T? =
    if (type == questionType && questionMetadataJson.isNotEmpty()) gson.fromJson(questionMetadataJson, answerType) else null

private fun <T> ChecklistQuestionData.extractAnswerMetadata(answerType: Class<T>): T? =
    if (answerMetadataJson.isNotEmpty()) gson.fromJson(answerMetadataJson, answerType) else null

fun ChecklistInstanceData.toReportModel(): Checklist = Checklist(id = id,
    name = name,
    type = type,
    creatorEmail = creatorEmail,
    maxScore = maxScore,
    minScore = minScore,
    consolidated = consolidated,
    allowsDecimalScore = allowsDecimalScore,
    status = status,
    finishedDate = finishedDate,
    visible = visible,
    questions = questions.map { it.toReportModel() }.sortedBy { it.position })

fun ChecklistQuestionData.toReportModel(): Question = Question(
    id = id,
    position = position,
    weight = weight,
    question = question,
    score = score,
    type = type,

    headlineMetadata = extractQuestionMetadata(ChecklistQuestionTypeData.HEADLINE, HeadlineMetadata::class.java),
    optionsToggleMetadata = extractQuestionMetadata(ChecklistQuestionTypeData.OPTIONS_TOGGLE, OptionsToggleMetadata::class.java),
    scoreMetadata = extractQuestionMetadata(ChecklistQuestionTypeData.SCORE, ScoreMetadata::class.java),
    textInputMetadata = extractQuestionMetadata(ChecklistQuestionTypeData.TEXT_INPUT, TextInputMetadata::class.java),

    answerMetadata = extractAnswerMetadata(AnswerMetadata::class.java)?.apply {
        weightedScore = (score ?: BigDecimal.ZERO).multiply(weight ?: BigDecimal.ZERO)
    },
)