package io.cloudflight.jems.plugin.standard.common.excel.model

import org.apache.poi.ss.util.WorkbookUtil

class ExcelData(val sheets: MutableList<ExcelSheetData> = mutableListOf()) {
    fun addSheet(name: String) =
        ExcelSheetData(
            name = WorkbookUtil.createSafeSheetName(name, '_'),
            data = mutableListOf()
        ).also { sheets.add(it) }
}