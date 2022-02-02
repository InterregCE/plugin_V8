package io.cloudflight.jems.plugin.standard.common.excel.model

class CellData constructor(val value: Any?, val cellStyle: CellStyle = CellStyle()) {

    fun backgroundColor(color: Color) = this.apply { cellStyle.backgroundColor = color }

    fun borderBottom(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            cellStyle.borderBottomStyle = borderStyle
            cellStyle.borderBottomColor = color
        }

    fun borderTop(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            cellStyle.borderTopStyle = borderStyle
            cellStyle.borderTopColor = color
        }

    fun borderLeft(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            cellStyle.borderLeftStyle = borderStyle
            cellStyle.borderLeftColor = color
        }

    fun borderRight(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            cellStyle.borderRightStyle = borderStyle
            cellStyle.borderRightColor = color
        }

    fun border(borderStyle: BorderStyle = BorderStyle.THICK, color: Color = Color.BLACK) =
        this.also {
            borderBottom(borderStyle, color)
            borderTop(borderStyle, color)
            borderRight(borderStyle, color)
            borderLeft(borderStyle, color)
        }
}