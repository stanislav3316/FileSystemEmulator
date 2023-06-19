package com.jb.integrational

import com.jb.FileSystem.Companion.FsPath
import com.jb.fs.InFileFS
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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
            fun iterateFolderContents(folder: File, storeInto: Path) {
                if (folder.isDirectory) {
                    val files = folder.listFiles()
                    if (files != null) {
                        for (file in files) {
                            if (file.isDirectory)
                                iterateFolderContents(file, storeInto)
                            else {
                                val path = storeInto.resolve(file.toPath())
                                val fsPath = FsPath(path.pathString)
                                fs.save(file, fsPath)
                            }
                        }
                    }
                }
            }

            iterateFolderContents(projectFolder, Path.of("."))

            fs.allPaths().size shouldBe originalPaths.size
        }

        val zipSize = File(zipFilePath).length()
        zipSize shouldBeGreaterThanOrEqual originalSize
    }
})
