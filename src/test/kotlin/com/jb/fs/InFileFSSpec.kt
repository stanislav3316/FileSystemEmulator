package com.jb.fs

import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsPath
import com.jb.FileSystem.Companion.FsProblems.DirectoryIsNotEmptyProblem
import com.jb.FileSystem.Companion.FsProblems.FileNotFoundProblem
import com.jb.FileSystem.Companion.FsProblems.PathAlreadyReservedProblem
import com.jb.FileSystem.Companion.FsProblems.PathDoesNotExistProblem
import com.jb.FileSystem.Companion.FsProblems.PathIsNotDirectoryProblem
import com.jb.FileSystem.Companion.FsProblems.ReadDirectoryProblem
import com.jb.Tests.createFile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class InFileFSSpec : FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"

    afterEach {
        File(zipFilePath).delete()
        File("src/main/resources/test").delete()
        File("src/main/resources/test2").delete()
    }

    test("should save and read file") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val file = createFile(1000L, "src/main/resources/test")
            fs.save(File("src/main/resources/test"), FsPath("./new_file"))

            fs.read(FsPath("./new_file")) shouldBe file.readBytes()
        }
    }

    test("should not read dir") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            createFile(1000L, "src/main/resources/test")
            fs.save(File("src/main/resources/test"), FsPath("./dir/new_file"))

            shouldThrow<ReadDirectoryProblem> {
                fs.read(FsPath("./dir"))
            }
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

    test("should delete file") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            fs.delete(path)
            fs.save(content, path) shouldBe Unit
        }
    }

    test("should not delete non empty dir") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./dir/new_file")
            fs.save(content, path)

            shouldThrow<DirectoryIsNotEmptyProblem> {
                fs.delete(FsPath("./dir"))
            }
        }
    }

    test("should not delete file if file doesn't exist") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            shouldThrow<PathDoesNotExistProblem> {
                fs.delete(path)
            }
        }
    }

    test("should move file") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            val newPath = FsPath("./folder/new_file")
            fs.move(path, newPath)

            fs.save(content, path) shouldBe Unit
        }
    }

    test("should move dir") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./dir/new_file")
            fs.save(content, path)
            fs.move(FsPath("./dir"), FsPath("./dir2"))

            fs.save(content, path) shouldBe Unit
        }
    }

    test("should rename file") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            val newPath = FsPath("./new_file_2")
            fs.move(path, newPath)

            fs.save(content, path) shouldBe Unit
        }
    }

    test("should not move file if file doesn't exist") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            val newPath = FsPath("./folder/new_file")
            shouldThrow<PathDoesNotExistProblem> {
                fs.move(path, newPath)
            }
        }
    }

    test("should not move file if new path is already reserved") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            val newPath = FsPath("./folder/new_file")
            fs.save(content, path)
            fs.save(content, newPath)
            shouldThrow<PathAlreadyReservedProblem> {
                fs.move(path, newPath)
            }
        }
    }

    test("should not read file if file doesn't exist") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            shouldThrow<PathDoesNotExistProblem> {
                fs.read(path)
            }
        }
    }

    test("should ls files") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            fs.ls(FsPath(".")) shouldBe listOf(
                FsEntity(
                    name = "new_file",
                    fullPath = "./new_file",
                    isDirectory = false,
                    size = 10000L
                )
            )
        }
    }

    test("should not ls files if path doesn't exist") {
        InFileFS(FsPath(zipFilePath)).use { fs ->
            shouldThrow<PathDoesNotExistProblem> {
                fs.ls(FsPath("./folder"))
            }
        }
    }

    test("should not ls files if path is not directory") {
        val content = ByteArray(10_000) { _ -> 1 }
        InFileFS(FsPath(zipFilePath)).use { fs ->
            val path = FsPath("./new_file")
            fs.save(content, path)
            shouldThrow<PathIsNotDirectoryProblem> {
                fs.ls(path)
            }
        }
    }
})
