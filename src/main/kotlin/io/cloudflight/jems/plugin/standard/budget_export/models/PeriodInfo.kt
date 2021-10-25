package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal

data class PeriodInfo(val periodNumber: Int, val periodAmount: BigDecimal) {
    fun toStringList() =
        periodAmount.toString()
}