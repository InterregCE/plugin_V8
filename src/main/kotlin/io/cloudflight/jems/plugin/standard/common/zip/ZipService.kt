package io.cloudflight.jems.plugin.standard.common.zip

interface ZipService {
    fun createZipFile(entries: List<ZipItem>): ByteArray
}
