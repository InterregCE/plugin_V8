package io.cloudflight.jems.plugin.standard.application_form_export.timeplan

data class TimeplanData(
    val periodColumns: List<PeriodColumn>,
    val columnDividers: List<String>,
    val rowDividers: List<String>,
    val width: Int,
    val height: Float,
    val lastPeriodNumber: Int,
    val firstRowHeight: Float,
    val borderAround: List<String>,
    val viewBox: String,
    val data: List<Rectangle>,
    val texts: List<TranslatedText>,
    val resultTitleYCoordinate: Float? = null,
)
