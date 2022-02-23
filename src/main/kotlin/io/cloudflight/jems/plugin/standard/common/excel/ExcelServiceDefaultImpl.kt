package io.cloudflight.jems.plugin.standard.common.excel

import io.cloudflight.jems.plugin.standard.common.excel.model.CellData
import io.cloudflight.jems.plugin.standard.common.excel.model.Color
import io.cloudflight.jems.plugin.standard.common.excel.model.ExcelData
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

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
                    colIndexes.forEach { index -> sheet.autoSizeColumn(index) }
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
            is Any -> cell.setCellValue(cellData.value.toString())
            else -> cell.setBlank()
        }
    }

    private fun setCellStyles(workbook: Workbook, cell: Cell, cellData: CellData) {
        getCellStyleIfItAlreadyExists(workbook, cellData).let { style ->
            style
                ?: workbook.createCellStyle().let { it as XSSFCellStyle }.also { newStyle ->
                    newStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
                    newStyle.setFillForegroundColor(cellData.cellStyle.backgroundColor.toXSSFColor())

                    newStyle.borderBottom = BorderStyle.valueOf(cellData.cellStyle.borderBottomStyle.code)
                    newStyle.borderTop = BorderStyle.valueOf(cellData.cellStyle.borderTopStyle.code)
                    newStyle.borderRight = BorderStyle.valueOf(cellData.cellStyle.borderRightStyle.code)
                    newStyle.borderLeft = BorderStyle.valueOf(cellData.cellStyle.borderLeftStyle.code)

                    newStyle.setBottomBorderColor(cellData.cellStyle.borderBottomColor.toXSSFColor())
                    newStyle.setTopBorderColor(cellData.cellStyle.borderTopColor.toXSSFColor())
                    newStyle.setRightBorderColor(cellData.cellStyle.borderRightColor.toXSSFColor())
                    newStyle.setLeftBorderColor(cellData.cellStyle.borderLeftColor.toXSSFColor())
                    newStyle.dataFormat = getCellFormat(cellData)
                }
        }.also { style -> cell.cellStyle = style }
    }

    private fun getCellStyleIfItAlreadyExists(workbook: Workbook, cellData: CellData) =
        IntRange(0, workbook.numCellStyles - 1).map { workbook.getCellStyleAt(it) as XSSFCellStyle }
            .firstOrNull { style ->
                style.fillForegroundXSSFColor == cellData.cellStyle.backgroundColor.toXSSFColor() &&
                        style.borderBottom == BorderStyle.valueOf(cellData.cellStyle.borderBottomStyle.code) &&
                        style.borderTop == BorderStyle.valueOf(cellData.cellStyle.borderTopStyle.code) &&
                        style.borderRight == BorderStyle.valueOf(cellData.cellStyle.borderRightStyle.code) &&
                        style.borderLeft == BorderStyle.valueOf(cellData.cellStyle.borderLeftStyle.code) &&
                        style.bottomBorderXSSFColor == cellData.cellStyle.borderBottomColor.toXSSFColor() &&
                        style.topBorderXSSFColor == cellData.cellStyle.borderTopColor.toXSSFColor() &&
                        style.rightBorderXSSFColor == cellData.cellStyle.borderRightColor.toXSSFColor() &&
                        style.leftBorderXSSFColor == cellData.cellStyle.borderLeftColor.toXSSFColor() &&
                        style.dataFormat == getCellFormat(cellData)
            }

    private fun Color.toXSSFColor() =
        XSSFColor(byteArrayOf(this.red.toByte(), this.green.toByte(), this.blue.toByte()))

    private fun getCellFormat(cellData: CellData) =
        when (cellData.value) {
            is LocalDateTime, is ZonedDateTime -> BuiltinFormats.getBuiltinFormat("m/d/yy h:mm").toShort()
            is LocalDate, is Date -> BuiltinFormats.getBuiltinFormat("m/d/yy").toShort()
            else -> BuiltinFormats.getBuiltinFormat("General").toShort()
        }
}
