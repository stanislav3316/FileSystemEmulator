package com.jb.fs

import com.jb.FileSystem
import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsPath
import com.jb.FileSystem.Companion.FsProblems.DirectoryIsNotEmptyProblem
import com.jb.FileSystem.Companion.FsProblems.FileNotFoundProblem
import com.jb.FileSystem.Companion.FsProblems.GenericProblem
import com.jb.FileSystem.Companion.FsProblems.PathAlreadyReservedProblem
import com.jb.FileSystem.Companion.FsProblems.PathDoesNotExistProblem
import com.jb.FileSystem.Companion.FsProblems.PathIsNotDirectoryProblem
import com.jb.FileSystem.Companion.FsProblems.PathNotValid
import com.jb.FileSystem.Companion.FsProblems.ReadDirectoryProblem
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.streams.toList


class InFileFS(fsPath: FsPath): FileSystem {

    private val zipfs = ZipFsBuilder.build(fsPath)

    override fun save(file: File, path: FsPath) {
        val localPath = resolveZipPath(path)

        ensureZipPathIsNotReserved(localPath)
        ensureOuterFileExists(file)

        try {
            localPath.createParentDirectoriesIfNeed()
            Files.write(localPath, file.readBytes())
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not save file")
        }
    }

    override fun save(bytes: ByteArray, path: FsPath) {
        val localPath = resolveZipPath(path)

        ensureZipPathIsNotReserved(localPath)

        try {
            localPath.createParentDirectoriesIfNeed()
            Files.write(localPath, bytes)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not save content")
        }
    }

    override fun append(path: FsPath, bytes: ByteArray) {
        val localPath = resolveZipPath(path)

        ensurePathExists(localPath)

        try {
            localPath.createParentDirectoriesIfNeed()
            Files.write(localPath, bytes, StandardOpenOption.APPEND)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not append content")
        }
    }

    override fun delete(path: FsPath) {
        val localPath = resolveZipPath(path)

        ensurePathExists(localPath)

        if (localPath.isDirectory()) {
            ensureDirectoryIsEmpty(localPath)
        }

        try {
            Files.delete(localPath)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not delete file")
        }
    }

    override fun move(path: FsPath, moveTo: FsPath) {
        val localPath = resolveZipPath(path)
        val newPath = resolveZipPath(moveTo)

        ensurePathExists(localPath)
        ensureZipPathIsNotReserved(newPath)

        try {
            newPath.createParentDirectoriesIfNeed()
            Files.move(localPath, newPath)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not move file")
        }
    }

    override fun read(path: FsPath): ByteArray {
        val localPath = resolveZipPath(path)

        ensurePathExists(localPath)
        ensurePathNotDirectory(localPath)

        try {
            return Files.readAllBytes(localPath)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not rename file")
        }
    }

    override fun ls(path: FsPath): List<FsEntity> {
        val localPath = resolveZipPath(path)

        ensurePathExists(localPath)
        ensurePathIsDirectory(localPath)

        try {
            return Files.newDirectoryStream(localPath).use { directoryStream ->
                directoryStream
                    .toList()
                    .map { entity ->
                        FsEntity(
                            name = entity.name,
                            fullPath = entity.pathString,
                            isDirectory = entity.isDirectory(),
                            size = entity.fileSize()
                        )
                    }
            }
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not rename file")
        }
    }

    override fun allEntities(): List<FsEntity> {
        val root = zipfs.rootDirectories.iterator().next()
        return Files
            .walk(root)
            .map { path ->
                path.pathString
                FsEntity(
                    name = path.name,
                    fullPath = path.pathString,
                    isDirectory = path.isDirectory(),
                    size = path.fileSize()
                )
            }
            .toList<FsEntity>()
    }

    override fun close() {
        zipfs.close()
    }

    private fun resolveZipPath(path: FsPath): Path {
        try {
            return zipfs.getPath(path.value)
        } catch (ex: InvalidPathException) {
            throw PathNotValid(path)
        }
    }

    private fun ensureZipPathIsNotReserved(localPath: Path) {
        if (localPath.exists()) {
            throw PathAlreadyReservedProblem(FsPath(localPath.pathString))
        }
    }

    private fun ensureOuterFileExists(file: File) {
        if (!file.exists()) {
            throw FileNotFoundProblem(FsPath(file.path))
        }
    }

    private fun ensurePathExists(path: Path) {
        if (!path.exists()) {
            throw PathDoesNotExistProblem(FsPath(path.pathString))
        }
    }

    private fun ensurePathIsDirectory(path: Path) {
        if (!path.isDirectory()) {
            throw PathIsNotDirectoryProblem(FsPath(path.pathString))
        }
    }

    private fun ensurePathNotDirectory(path: Path) {
        if (path.isDirectory()) {
            throw ReadDirectoryProblem(FsPath(path.pathString))
        }
    }

    private fun ensureDirectoryIsEmpty(path: Path) {
        if (path.listDirectoryEntries().isNotEmpty()) {
            throw DirectoryIsNotEmptyProblem(FsPath(path.pathString))
        }
    }
}
