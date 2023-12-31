package com.jb.integrational

import com.jb.FileSystem.Companion.FsPath
import com.jb.Tests.iterateFolderContents
import com.jb.Tests.originalPaths
import com.jb.Tests.originalSize
import com.jb.fs.InFileFS
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File

class IntegrationSpec : FunSpec({

    val zipFilePath = "/tmp/arch.zip"

    afterEach {
        File(zipFilePath).delete()
    }

    test("should save src files") {
        val projectFolder = File("src")

        val originalSize = originalSize(projectFolder)
        val originalPaths = originalPaths(projectFolder)

        InFileFS(FsPath(zipFilePath)).use { fs ->
            iterateFolderContents(projectFolder, fs)
            fs.allEntities().size shouldBe originalPaths.size
        }

        val zipSize = File(zipFilePath).length()
        zipSize shouldBeGreaterThanOrEqual originalSize
    }

    test("save and remove all files") {
        val projectFolder = File("src")

        val originalSize = originalSize(projectFolder)

        InFileFS(FsPath(zipFilePath)).use { fs ->
            iterateFolderContents(projectFolder, fs)
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

    test("should save, move and save again src files") {
        val projectFolder = File("src")

        val originalSize = originalSize(projectFolder)
        val originalPaths = originalPaths(projectFolder)

        InFileFS(FsPath(zipFilePath)).use { fs ->
            iterateFolderContents(projectFolder, fs)
            fs.allEntities().size shouldBe originalPaths.size

            fs.move(FsPath("/src"), FsPath("/copy"))
            iterateFolderContents(projectFolder, fs)
        }

        val zipSize = File(zipFilePath).length()
        zipSize shouldBeGreaterThanOrEqual (originalSize * 2)
    }
})
