package com.jb

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import java.io.File
import java.io.RandomAccessFile
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files

class CompressionFsSpec: FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"
    val fileToStore = "src/main/resources/test"

    afterEach {
        File(zipFilePath).delete()
        File(fileToStore).delete()
    }

    test("should not compress files using nio.FileSystems") {

        fun createFile(size: Long, filename: String): Unit {
            val file = File(filename)
            file.createNewFile()

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(size)
            raf.close()
        }

        fun absolutePath(pathToFile: String): String {
            return File(pathToFile).absolutePath
        }

        val size = 104857600L
        createFile(size, "src/main/resources/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())
        }

        File(absolutePath(zipFilePath)).length() shouldBeGreaterThanOrEqual size
    }
})