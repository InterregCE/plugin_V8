package io.cloudflight.jems.plugin.standard.common.excel.model

class ExcelSheetData(val name: String, val data: MutableList<RowData> = mutableListOf()) {
    fun addRow(vararg cellData: CellData) =
        RowData(cellData.toList()).also { data.add(it) }
}