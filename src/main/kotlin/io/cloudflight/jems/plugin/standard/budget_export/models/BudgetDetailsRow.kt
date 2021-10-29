package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal

open class BudgetDetailsRow(
    val partnerInfo: PartnerInfo,
    val costCategory: String = "",
    val unitCost: String = "N/A",
    val flatRate: Int? = null,
    val unitType: String = "",
    val numberOfUnits: BigDecimal? = null,
    val pricePerUnit: BigDecimal? = null,
    val description: String = "",
    val comment: String = "",
    val awardProcedure: String = "",
    val investmentNumber: String = "",
    val periodAmounts: List<PeriodInfo>,
    val total: BigDecimal
)