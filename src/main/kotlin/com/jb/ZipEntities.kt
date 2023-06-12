package com.jb

import com.jb.FileSystem.Companion.Path
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

//TODO: single thread
class ZipEntities(val fs: Path): Closeable {

    private val fos = FileOutputStream(fs.value)
    private val zos =
        ZipOutputStream(fos).apply {
            this.setMethod(ZipOutputStream.STORED)
            this.setLevel(Deflater.NO_COMPRESSION)
        }

    private fun writeToFs(fileToStore: File) {
        val entry = fillEntry(fileToStore)
        zos.putNextEntry(entry)

        val fis = FileInputStream(fileToStore)
        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (fis.read(buffer).also { bytesRead = it } != -1) {
            zos.write(buffer, 0, bytesRead)
        }

        fis.close()
        zos.closeEntry()
    }

    private fun fillEntry(fileToStore: File): ZipEntry {
        val fis = FileInputStream(fileToStore)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var uncompressedSize: Long = 0
        var compressedSize: Long = 0
        val crc32 = CRC32()

        while (fis.read(buffer).also { bytesRead = it } != -1) {
            uncompressedSize += bytesRead.toLong()
            crc32.update(buffer, 0, bytesRead)
        }
        compressedSize = uncompressedSize

        val entry = ZipEntry(File(fileToStore.path).name)
        entry.size = uncompressedSize
        entry.compressedSize = compressedSize
        entry.crc = crc32.value

        fis.close()

        return entry
    }

    fun entriesList(): List<String> {
        val zipFile = ZipFile(fs.value)
        val entries = zipFile.entries().toList().map { it.name }
        zipFile.close()
        return entries
    }

    override fun close() {
        zos.close()
    }
}
