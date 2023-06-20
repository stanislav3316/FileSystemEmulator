package com.jb.integrational

import com.jb.FileSystem.Companion.FsPath
import com.jb.fs.InFileFS
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import kotlin.io.path.pathString
import kotlin.streams.toList

class IntegrationSpec: FunSpec({

    val zipFilePath = "/tmp/arch.zip"

    afterEach {
        File(zipFilePath).delete()
    }

    test("should save src files") {
        val projectFolder = File("src")

        val originalSize =
            Files
                .walk(projectFolder.toPath())
                .filter { path -> path.toFile().isFile }
                .mapToLong { path -> path.toFile().length() }
                .sum()

        val originalPaths =
            Files
                .walk(projectFolder.toPath())
                .map { path -> path.pathString }
                .toList<String>()

        InFileFS(FsPath(zipFilePath)).use { fs ->
            fun iterateFolderContents(folder: File) {
                if (folder.isDirectory) {
                    val files = folder.listFiles()
                    if (files != null) {
                        for (file in files) {
                            if (file.isDirectory)
                                iterateFolderContents(file)
                            else {
                                val path = file.toPath()
                                val fsPath = FsPath(path.pathString)
                                fs.save(file, fsPath)
                            }
                        }
                    }
                }
            }

            iterateFolderContents(projectFolder)

            fs.allEntities().size shouldBe originalPaths.size
        }

        val zipSize = File(zipFilePath).length()
        zipSize shouldBeGreaterThanOrEqual originalSize
    }

    test("save and remove all files") {
        val projectFolder = File("src")

        val originalSize =
            Files
                .walk(projectFolder.toPath())
                .filter { path -> path.toFile().isFile }
                .mapToLong { path -> path.toFile().length() }
                .sum()

        InFileFS(FsPath(zipFilePath)).use { fs ->
            fun iterateFolderContents(folder: File) {
                if (folder.isDirectory) {
                    val files = folder.listFiles()
                    if (files != null) {
                        for (file in files) {
                            if (file.isDirectory)
                                iterateFolderContents(file)
                            else {
                                val path = file.toPath()
                                val fsPath = FsPath(path.pathString)
                                fs.save(file, fsPath)
                            }
                        }
                    }
                }
            }

            iterateFolderContents(projectFolder)
        }

        var zipSize = File(zipFilePath).length()
        zipSize shouldBeGreaterThanOrEqual originalSize

        InFileFS(FsPath(zipFilePath)).use { fs ->
            val entities = fs.allEntities()
            entities
                .filter { !it.isDirectory }
                .forEach { e -> fs.delete(FsPath(e.fullPath)) }

            entities
                .filter { it.isDirectory }
                .sortedByDescending { it.fullPath }
                .filter { it.fullPath != "/" }
                .forEach { e -> fs.delete(FsPath(e.fullPath)) }
        }

        zipSize = File(zipFilePath).length()
        zipSize shouldBeLessThan 50
    }
})
