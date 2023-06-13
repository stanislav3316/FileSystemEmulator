package com.jb

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.name


class CompressionFsSpec: FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"
    val fileToStore = "src/main/resources/test"

    afterEach {
        File(zipFilePath).delete()
        File(fileToStore).delete()
        File("$fileToStore-2").delete()
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
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())
        }

        File(absolutePath(zipFilePath)).length() shouldBeGreaterThanOrEqual size
    }

    test("should remove file from zip FS") {

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
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())
            Files.deleteIfExists(path)
        }

        File(absolutePath("$zipFilePath")).length() shouldBeLessThan 25
    }

    test("should list files from zip FS") {

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
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            val rootDirectory= zipfs.getPath(".")
            Files.newDirectoryStream(rootDirectory).use { directoryStream ->
                directoryStream.toList().map { it.name } shouldBe listOf("file")
            }
        }
    }

    test("should replace existing file from zip FS") {

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

        val size = 52428800L
        createFile(size, "src/main/resources/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            val newFilePath = "src/main/resources/test-2"
            createFile(size * 2, newFilePath)

            FileInputStream(newFilePath).use { inputStream ->
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        File(absolutePath(zipFilePath)).length() shouldBeGreaterThanOrEqual (size * 2)
    }
})