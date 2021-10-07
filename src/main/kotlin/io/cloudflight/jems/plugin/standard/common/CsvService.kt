package io.cloudflight.jems.plugin.standard.common


interface CsvService {
    fun generateCsv(content: List<List<String?>>): ByteArray
}
