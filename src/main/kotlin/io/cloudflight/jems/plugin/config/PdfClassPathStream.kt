package io.cloudflight.jems.plugin.config

import com.openhtmltopdf.extend.FSStream
import org.springframework.core.io.ClassPathResource
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

class PdfClassPathStream(uri: String) : FSStream {
    private val classPathResource: ClassPathResource = ClassPathResource(uri)

    override fun getStream(): InputStream =
            classPathResource.inputStream

    override fun getReader(): Reader =
            InputStreamReader(classPathResource.inputStream, StandardCharsets.UTF_8)
}