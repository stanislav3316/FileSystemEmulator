package com.jb.investigations

import com.jb.Tests.absolutePath
import com.jb.Tests.createFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.name

class CompressionFsSpec : FunSpec({

    val zipFilePath = "/tmp/archive.zip"
    val fileToStore = "/tmp/test"

    afterEach {
        File(zipFilePath).delete()
        File(fileToStore).delete()
        File("$fileToStore-2").delete()
    }

    test("should not compress files using nio.FileSystems") {

        val size = 104857600L
        createFile(size, "/tmp/test")

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

        val size = 104857600L
        createFile(size, "/tmp/test")

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

        File(absolutePath(zipFilePath)).length() shouldBeLessThan 25
    }

    test("should list files from zip FS") {

        val size = 104857600L
        createFile(size, "/tmp/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            val rootDirectory = zipfs.getPath(".")
            Files.newDirectoryStream(rootDirectory).use { directoryStream ->
                directoryStream.toList().map { it.name } shouldBe listOf("file")
            }
        }
    }

    test("should replace existing file from zip FS") {

        val size = 52428800L
        createFile(size, "/tmp/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            val newFilePath = "/tmp/test-2"
            createFile(size * 2, newFilePath)

            FileInputStream(newFilePath).use { inputStream ->
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        File(absolutePath(zipFilePath)).length() shouldBeGreaterThanOrEqual (size * 2)
    }

    test("should read file from zip FS") {

        val size = 104857600L
        createFile(size, "/tmp/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            val fileBytes = Files.readAllBytes(path)
            fileBytes.size shouldBe size
        }
    }

    test("should append data to file in zip FS") {

        val size = 104857600L
        val delta = 1000
        createFile(size, "/tmp/test")

        val zipURI = URI.create("jar:file:${absolutePath(zipFilePath)}")

        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        env["compressionMethod"] = "STORED"
        env["noCompression"] = "true"

        FileSystems.newFileSystem(zipURI, env).use { zipfs ->
            val path = zipfs.getPath("file")
            Files.write(path, File(fileToStore).readBytes())

            Files.write(path, ByteArray(delta), StandardOpenOption.APPEND)
        }

        File(absolutePath(zipFilePath)).length() shouldBeGreaterThanOrEqual (size + delta)
    }
})
