package io.cloudflight.jems.plugin.standard.common.pdf


interface PdfService {
    fun generatePdfFromHtml(html: String): ByteArray
}
