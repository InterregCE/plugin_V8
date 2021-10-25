package io.cloudflight.jems.plugin.standard.budget_export.models


class CsvData {
    private val content = mutableListOf<List<String?>>()

    fun addRow(row: String) =
        content.add(listOf(row))

    fun addEmptyRow() =
        content.add(listOf(""))

    fun addRows(rows: List<List<String?>>) =
        content.addAll(rows.map { it.map { (it ?: "") } })

    fun getContent() =
        content
}
