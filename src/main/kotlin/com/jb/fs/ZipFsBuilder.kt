package com.jb.fs

import com.jb.FileSystem.Companion.FsPath
import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems

object ZipFsBuilder {

    fun build(fsPath: FsPath): FileSystem {
        val env = mapOf(
            ("create" to "true"),
            ("compressionMethod" to "STORED"),
            ("noCompression" to "true"),
        )
        val uri = URI.create("jar:file:${File(fsPath.value).absolutePath}")
        return FileSystems.newFileSystem(uri, env)
    }
}
