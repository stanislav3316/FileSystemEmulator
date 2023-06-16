package com.jb.fs

import com.jb.FileSystem.Companion.FsPath
import com.jb.FileSystem.Companion.FsProblems.FileNotFoundProblem
import com.jb.FileSystem.Companion.FsProblems.PathAlreadyReservedProblem
import com.jb.FileSystem.Companion.FsProblems.PathDoesNotExistProblem
import com.jb.InFileFS
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.RandomAccessFile

class InFileFSSpec: FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"

    afterEach {
        File(zipFilePath).delete()
    }

    test("should save and read file") {

        fun createFile(size: Long, filename: String): File {
            val file = File(filename)
            file.createNewFile()

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(size)
            raf.close()
            return file
        }

        InFileFS(FsPath(zipFilePath)).use { fs ->
            val file = createFile(1000L, "src/main/resources/test")
            fs.save(File("src/main/resources/test"), FsPath("./new_file"))

            fs.read(FsPath("./new_file")) shouldBe file.readBytes()
        }
    }

    test("should not save if file doesn't exist") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            shouldThrow<FileNotFoundProblem> {
                fs.save(File("src/main/resources/some-file"), FsPath("./new_file"))
            }
        }
    }

    test("should not save if path already reserved") {

        fun createFile(size: Long, filename: String): File {
            val file = File(filename)
            file.createNewFile()

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(size)
            raf.close()
            return file
        }

        InFileFS(FsPath(zipFilePath)).use { fs ->
            createFile(1000L, "src/main/resources/test")
            createFile(1000L, "src/main/resources/test2")
            fs.save(File("src/main/resources/test"), FsPath("./new_file"))

            shouldThrow<PathAlreadyReservedProblem> {
                fs.save(File("src/main/resources/test2"), FsPath("./new_file"))
            }
        }
    }

    test("should save byte array") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            fs.read(path) shouldBe content
        }
    }

    test("should not save byte array if path already reserved") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)

            shouldThrow<PathAlreadyReservedProblem> {
                fs.save(content, path)
            }
        }
    }

    test("should append byte array") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            fs.append(path, content)

            fs.read(path) shouldBe (content + content)
        }
    }

    test("should not append if path doesn't exist") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            shouldThrow<PathDoesNotExistProblem> {
                fs.append(path, content)
            }
        }
    }
})
