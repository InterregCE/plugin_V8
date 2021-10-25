package io.cloudflight.jems.plugin.standard.common.csv


interface CsvService {
    fun generateCsv(content: List<List<String?>>): ByteArray
}
