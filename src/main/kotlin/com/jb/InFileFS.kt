package com.jb

import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsFileName
import com.jb.FileSystem.Companion.FsPath
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption
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
        Files.write(localPath, file.readBytes())
    }

    override fun save(bytes: ByteArray, path: FsPath) {
        val localPath = zipfs.getPath(path.value)
        Files.write(localPath, bytes)
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
}
