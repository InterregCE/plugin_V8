package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal

data class BudgetTotalCostInfo(
    val total: BigDecimal,
    val flatRateTotal: BigDecimal,
    val realCostTotal: BigDecimal,
    val unitCostTotal: BigDecimal
) {
    fun toStringList() =
        listOf(total.toString(), flatRateTotal.toString(), realCostTotal.toString(), unitCostTotal.toString())
}

data class GeneralBudgetTotalCostInfo(
    val realCostTotal: BigDecimal,
    val unitCostTotal: BigDecimal
){
    fun getTotal() = realCostTotal.plus(unitCostTotal)

    fun toStringList() =
        listOf(getTotal().toString(), realCostTotal.toString(), unitCostTotal.toString())
}
