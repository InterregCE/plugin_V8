package io.cloudflight.jems.plugin.standard.common.pdf

import com.openhtmltopdf.bidi.support.ICUBidiReorderer
import com.openhtmltopdf.bidi.support.ICUBidiSplitter
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import io.cloudflight.jems.plugin.config.ClassPathStreamFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


@Service
open class PdfServiceDefaultImpl() : PdfService {
    override fun generatePdfFromHtml(html: String): ByteArray =
        with(ByteArrayOutputStream()) {
            OutputStreamWriter(this).use {
                PdfRendererBuilder().let { builder ->
                    builder.useFastMode()
                    builder.useUnicodeBidiSplitter(ICUBidiSplitter.ICUBidiSplitterFactory())
                    builder.useUnicodeBidiReorderer(ICUBidiReorderer())
                    builder.defaultTextDirection(BaseRendererBuilder.TextDirection.LTR)
                    builder.useProtocolsStreamImplementation(ClassPathStreamFactory(), "classpath")
                    builder.withHtmlContent(html, "classpath:/templates/")
                    builder.useSVGDrawer(BatikSVGDrawer())
                    builder.toStream(this)
                    builder.buildPdfRenderer().use {
                        it.layout()
                        it.createPDF()
                    }
                }
            }
            toByteArray()
        }

}
