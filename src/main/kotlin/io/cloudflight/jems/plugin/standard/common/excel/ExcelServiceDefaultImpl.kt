package io.cloudflight.jems.plugin.standard.common.excel

import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Date
import java.util.Calendar

@Service
class ExcelServiceDefaultImpl : ExcelService {
    override fun generateExcel(data: ExcelData): ByteArray =
        with(ByteArrayOutputStream()) {
            val workbook = SXSSFWorkbook()
            val colIndexes = mutableSetOf<Int>()
            data.sheets.forEach { sheetData ->
                workbook.createSheet(sheetData.name).let { sheet ->
                    sheetData.data.forEachIndexed { rowIndex, rowData ->
                        sheet.createRow(rowIndex).let { row ->
                            rowData.cellData.forEachIndexed { cellIndex, cellData ->
                                colIndexes.add(cellIndex)
                                row.createCell(cellIndex).let { cell ->
                                    writeCellValue(cell, cellData)
                                    setCellStyles(workbook, cell, cellData)
                                }
                            }
                        }
                    }
                    sheet.trackAllColumnsForAutoSizing()
                    colIndexes.forEach { index ->  sheet.autoSizeColumn(index)}
                }
            }
            workbook.write(this)
            this.toByteArray()
        }

    private fun writeCellValue(cell: Cell, cellData: CellData) {
        when (cellData.value) {
            is BigDecimal -> cell.setCellValue(cellData.value.toDouble())
            is Int -> cell.setCellValue(cellData.value.toDouble())
            is Double -> cell.setCellValue(cellData.value)
            is Date -> cell.setCellValue(cellData.value)
            is LocalDateTime -> cell.setCellValue(cellData.value)
            is ZonedDateTime -> cell.setCellValue(cellData.value.toLocalDateTime())
            is LocalDate -> cell.setCellValue(cellData.value)
            is Calendar -> cell.setCellValue(cellData.value)
            is String -> cell.setCellValue(cellData.value)
            is Boolean -> cell.setCellValue(cellData.value)
            else -> cell.setCellValue(cellData.value.toString())
        }
    }

    private fun setCellStyles(workbook: Workbook, cell: Cell, cellData: CellData) {
        workbook.createCellStyle().let { it as XSSFCellStyle }.also { style ->
            style.fillPattern = FillPatternType.SOLID_FOREGROUND
            style.setFillForegroundColor(cellData.cellStyle.backgroundColor.toXSSFColor())

            style.borderBottom = BorderStyle.valueOf(cellData.cellStyle.borderBottomStyle.code)
            style.borderTop = BorderStyle.valueOf(cellData.cellStyle.borderTopStyle.code)
            style.borderRight = BorderStyle.valueOf(cellData.cellStyle.borderRightStyle.code)
            style.borderLeft = BorderStyle.valueOf(cellData.cellStyle.borderLeftStyle.code)

            style.setBottomBorderColor(cellData.cellStyle.borderBottomColor.toXSSFColor())
            style.setTopBorderColor(cellData.cellStyle.borderTopColor.toXSSFColor())
            style.setRightBorderColor(cellData.cellStyle.borderRightColor.toXSSFColor())
            style.setLeftBorderColor(cellData.cellStyle.borderLeftColor.toXSSFColor())

            if (cellData.value is LocalDate || cellData.value is LocalDateTime || cellData.value is ZonedDateTime || cellData.value is Date)
                style.dataFormat = BuiltinFormats.getBuiltinFormat("m/d/yy h:mm").toShort()

            cell.cellStyle = style
        }

    }

    private fun Color.toXSSFColor() =
        XSSFColor(byteArrayOf(this.red.toByte(), this.green.toByte(), this.blue.toByte()))
}
