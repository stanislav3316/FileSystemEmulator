package com.jb

import java.io.Closeable
import java.io.File

//todo: declare domain exceptions
interface FileSystem: Closeable {

    fun save(file: File): Unit

    fun save(path: FsPath, bytes: ByteArray): Unit

    fun append(path: FsPath, bytes: Array<Byte>): Unit

    fun delete(path: FsPath): Unit

    fun rename(path: FsPath, newName: FsFileName): Unit

    fun move(path: FsPath, newPath: FsPath): Unit

    fun read(path: FsPath): Array<Byte>

    fun ls(path: FsPath): List<FsEntity>

    companion object {

        @JvmInline
        value class FsPath(val value: String)

        @JvmInline
        value class FsFileName(val value: String)

        data class FsEntity(
            val name: String,
            val fullPath: String,
            val isDirectory: Boolean,
            val size: Long
        )
    }
}
