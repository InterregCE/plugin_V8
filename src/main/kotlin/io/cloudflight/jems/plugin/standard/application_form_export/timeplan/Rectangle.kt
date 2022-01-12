package io.cloudflight.jems.plugin.standard.application_form_export.timeplan

data class Rectangle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rx: Int,
    val fill: String,
    val stroke: String = "#000000",
    val strokeWidth: Int = 0,
    var text: RectangleText? = null,
)
