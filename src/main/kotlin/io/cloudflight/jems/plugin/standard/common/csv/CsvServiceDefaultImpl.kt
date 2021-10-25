package io.cloudflight.jems.plugin.standard.common.csv

import com.opencsv.CSVWriter
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


@Service
open class CsvServiceDefaultImpl() : CsvService {

    override fun generateCsv(content: List<List<String?>>): ByteArray =
        with(ByteArrayOutputStream()) {
            OutputStreamWriter(this).use {
                CSVWriter(it).writeAll(content.map { it.toTypedArray() })
            }
            toByteArray()
        }
}
