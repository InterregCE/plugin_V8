package io.cloudflight.jems.plugin.standard.application_form_export.timeplan

data class RectangleText(
    val x: Float,
    val y: Int,
    val value: String,
    val dominantBaseline: String = "middle",
    val fontStyle: String = "normal",
)
