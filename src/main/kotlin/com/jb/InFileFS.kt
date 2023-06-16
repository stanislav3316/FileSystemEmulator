package com.jb

import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsFileName
import com.jb.FileSystem.Companion.FsPath
import com.jb.FileSystem.Companion.FsProblems.FileNotFoundProblem
import com.jb.FileSystem.Companion.FsProblems.GenericProblem
import com.jb.FileSystem.Companion.FsProblems.PathAlreadyReservedProblem
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString

class InFileFS(private val fsPath: FsPath): FileSystem {

    private val zipfs = run {
        val env = mapOf(
            ("create" to "true"),
            ("compressionMethod" to "STORED"),
            ("noCompression" to "true"),
        )
        val uri = URI.create("jar:file:${File(fsPath.value).absolutePath}")
        FileSystems.newFileSystem(uri, env)
    }

    override fun save(file: File, path: FsPath) {
        val localPath = zipfs.getPath(path.value)

        ensureZipPathIsNotReserved(localPath)
        ensureOuterFileExists(file)

        try {
            Files.write(localPath, file.readBytes())
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not save file")
        }
    }

    override fun save(bytes: ByteArray, path: FsPath) {
        val localPath = zipfs.getPath(path.value)

        ensureZipPathIsNotReserved(localPath)

        try {
            Files.write(localPath, bytes)
        } catch (ex: Exception) {
            throw GenericProblem(ex.message ?: "could not save content")
        }
    }

    override fun append(path: FsPath, bytes: ByteArray) {
        val localPath = zipfs.getPath(path.value)
        Files.write(localPath, bytes, StandardOpenOption.APPEND)
    }

    override fun delete(path: FsPath) {
        val localPath = zipfs.getPath(path.value)
        Files.delete(localPath)
    }

    override fun rename(path: FsPath, newName: FsFileName) {
        val localPath = zipfs.getPath(path.value)
        val newPath = localPath.parent.resolve(newName.value)
        Files.move(localPath, newPath)
    }

    override fun move(path: FsPath, newPath: FsPath) {
        val localPath = zipfs.getPath(path.value)
        val newLocalPath = zipfs.getPath(newPath.value)
        Files.move(localPath, newLocalPath)
    }

    override fun read(path: FsPath): ByteArray {
        val localPath = zipfs.getPath(path.value)
        return Files.readAllBytes(localPath)
    }

    override fun ls(path: FsPath): List<FsEntity> {
        val localPath = zipfs.getPath(path.value)
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
    }

    override fun close() {
        zipfs.close()
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
}
