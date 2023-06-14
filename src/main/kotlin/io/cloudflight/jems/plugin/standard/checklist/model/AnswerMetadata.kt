package io.cloudflight.jems.plugin.standard.checklist.model

import java.math.BigDecimal

class AnswerMetadata(
    val answer: String?,
    val justification: String?,
    val explanation: String?,
    val score: BigDecimal?,
    var weightedScore: BigDecimal?
)