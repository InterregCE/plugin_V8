package io.cloudflight.jems.plugin.standard.programme_data_export.model

import java.math.BigDecimal

class ProjectCodeOfInterventionBudgetShare(
    val dimensionCode: String,
    val projectBudgetAmountShare: BigDecimal,
    val projectBudgetPercentShare: BigDecimal,
)