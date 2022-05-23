package io.cloudflight.jems.plugin.standard.budget_export.models

import java.math.BigDecimal

data class FundInfo(val fundAmount: BigDecimal?, val fundPercentage: BigDecimal?, val percentageOfTotal: BigDecimal?) {
    fun toStringList() =
        listOf((fundAmount ?: BigDecimal.ZERO), (fundPercentage ?: BigDecimal.ZERO), (percentageOfTotal ?: BigDecimal.ZERO))
}
