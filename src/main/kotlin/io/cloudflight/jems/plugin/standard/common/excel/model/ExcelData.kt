package io.cloudflight.jems.plugin.standard.common.excel.model

class ExcelData(val sheets: MutableList<ExcelSheetData> = mutableListOf()) {
    fun addSheet(name: String) =
        ExcelSheetData(name = name, data = mutableListOf()).also { sheets.add(it) }
}