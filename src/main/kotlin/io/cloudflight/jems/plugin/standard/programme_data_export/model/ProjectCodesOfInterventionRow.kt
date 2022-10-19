package io.cloudflight.jems.plugin.standard.programme_data_export.model

import java.math.BigDecimal

open class ProjectCodesOfInterventionRow(
    val projectId: String,
    val projectAcronym: String?,
    val projectTotalBudget: BigDecimal,
    val dimensionCodesBudgetShare: List<ProjectCodeOfInterventionBudgetShare>
)