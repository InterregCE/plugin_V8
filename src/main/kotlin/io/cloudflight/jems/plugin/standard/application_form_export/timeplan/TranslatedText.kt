package io.cloudflight.jems.plugin.standard.application_form_export.timeplan

data class TranslatedText(
    val x: Float,
    val y: Float,
    val value: String,
    val dominantBaseline: String = "middle",
    val fontStyle: String = "normal",
    val fill: String = "#000000",
    val fontWeight: Int = 400,
)
