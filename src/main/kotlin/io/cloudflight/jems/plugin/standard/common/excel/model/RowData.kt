package io.cloudflight.jems.plugin.standard.common.excel.model

class RowData(val cellData: List<CellData>) {

    fun borderBottom(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also { cellData.forEach { it.borderBottom(borderStyle, color) } }

    fun borderTop(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also { cellData.forEach { it.borderTop(borderStyle, color) } }

    fun borderLeft(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also { cellData.forEach { it.borderLeft(borderStyle, color) } }

    fun borderRight(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            cellData.forEach { it.borderRight(borderStyle, color) }
        }

    fun border(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            borderBottom(borderStyle, color)
            borderTop(borderStyle, color)
            borderRight(borderStyle, color)
            borderLeft(borderStyle, color)
        }
}