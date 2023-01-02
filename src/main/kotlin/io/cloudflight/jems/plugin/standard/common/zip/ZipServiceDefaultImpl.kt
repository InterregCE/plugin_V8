package io.cloudflight.jems.plugin.standard.common.zip

import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class ZipServiceDefaultImpl : ZipService {
    @Throws(Exception::class)
    override fun createZipFile(entries: List<ZipItem>): ByteArray {
        val bytes = ByteArrayOutputStream()
        val zip = ZipOutputStream(bytes)
        entries.forEach { addZipEntry(zip, it) }
        zip.close()
        return bytes.toByteArray()
    }

    @Throws(Exception::class)
    fun addZipEntry(zip: ZipOutputStream, zipItem: ZipItem) {
        val zipEntry = ZipEntry(zipItem.path)
        zip.putNextEntry(zipEntry)
        zip.write(zipItem.bytes, 0, zipItem.bytes.size)
        zip.closeEntry()
    }

}
