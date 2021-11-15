package io.cloudflight.jems.plugin.config

import com.openhtmltopdf.extend.FSStream
import com.openhtmltopdf.extend.FSStreamFactory
import java.net.URI

class ClassPathStreamFactory : FSStreamFactory {
    override fun getUrl(uri: String): FSStream =
        PdfClassPathStream(URI(uri).path)
}