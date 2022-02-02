package io.cloudflight.jems.plugin.standard.common.excel.model

class CellStyle constructor(
    var backgroundColor: Color = Color.WHITE,
    var borderBottomStyle: BorderStyle = BorderStyle.THIN,
    var borderBottomColor: Color = Color.BLACK,
    var borderTopStyle: BorderStyle = BorderStyle.THIN,
    var borderTopColor: Color = Color.BLACK,
    var borderLeftStyle: BorderStyle = BorderStyle.THIN,
    var borderLeftColor: Color = Color.BLACK,
    var borderRightStyle: BorderStyle = BorderStyle.THIN,
    var borderRightColor: Color = Color.BLACK
)