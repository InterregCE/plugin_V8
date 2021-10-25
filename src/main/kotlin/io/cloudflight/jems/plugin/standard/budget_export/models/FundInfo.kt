package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal

data class FundInfo(val fundAmount: BigDecimal?, val fundPercentage: BigDecimal?) {
    fun toStringList() =
        listOf((fundAmount ?: BigDecimal.ZERO).toString(), (fundPercentage ?: BigDecimal.ZERO).toString())
}
