package com.jb

import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsFileName
import com.jb.FileSystem.Companion.FsPath
import java.io.File
import java.net.URI
import java.nio.file.FileSystems

class InFileFS(private val fsPath: FsPath): FileSystem {

    private val fileSystem = run {
        val env = mapOf(
            ("create" to "true"),
            ("compressionMethod" to "STORED"),
            ("noCompression" to "true"),
        )
        val uri = URI.create("jar:file:${File(fsPath.value).absolutePath}")
        FileSystems.newFileSystem(uri, env)
    }

    override fun save(file: File) {
        TODO("Not yet implemented")
    }

    override fun save(path: FsPath, bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun append(path: FsPath, bytes: Array<Byte>) {
        TODO("Not yet implemented")
    }

    override fun delete(path: FsPath) {
        TODO("Not yet implemented")
    }

    override fun rename(path: FsPath, newName: FsFileName) {
        TODO("Not yet implemented")
    }

    override fun move(path: FsPath, newPath: FsPath) {
        TODO("Not yet implemented")
    }

    override fun read(path: FsPath): Array<Byte> {
        TODO("Not yet implemented")
    }

    override fun ls(path: FsPath): List<FsEntity> {
        TODO("Not yet implemented")
    }

    override fun close() {
        fileSystem.close()
    }
}
