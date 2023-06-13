package com.jb

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CompressionSpec: FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"
    val fileToStore = "src/main/resources/test"

    afterEach {
        File(zipFilePath).delete() shouldBe true
        File(fileToStore).delete() shouldBe true
    }

    test("should not compress files") {

        fun createFile(size: Long, filename: String): Unit {
            val file = File(filename)
            file.createNewFile()

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(size)
            raf.close()
        }

        val size = 104857600L
        createFile(size, "src/main/resources/test")

        try {
            val fos = FileOutputStream(zipFilePath)
            val zos = ZipOutputStream(fos)
            zos.setMethod(ZipOutputStream.STORED)
            zos.setLevel(Deflater.NO_COMPRESSION)

            // Read the file content and calculate the uncompressed size, compressed size, and CRC-32 checksum
            var fis = FileInputStream(fileToStore)
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

            // Create a new entry with the required metadata
            val entry = ZipEntry(File(fileToStore).name)
            entry.size = uncompressedSize
            entry.compressedSize = compressedSize
            entry.crc = crc32.value

            // Add the entry to the ZIP archive
            zos.putNextEntry(entry)

            // Reset the input stream and write the file content to the archive
            fis.close()
            fis = FileInputStream(fileToStore)
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                zos.write(buffer, 0, bytesRead)
            }

            // Close the input stream and the entry
            fis.close()
            zos.closeEntry()

            // Close the ZIP output stream
            zos.close()
            println("Stored entry created successfully in the ZIP archive.")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        File(zipFilePath).length() shouldBeGreaterThanOrEqual size
    }
})