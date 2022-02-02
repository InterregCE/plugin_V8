package io.cloudflight.jems.plugin.standard.common.excel

import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData

interface ExcelService {
    fun generateExcel(data: ExcelData): ByteArray
}
