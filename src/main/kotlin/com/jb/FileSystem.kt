package com.jb

import java.io.Closeable
import java.io.File

interface FileSystem: Closeable {

    fun save(file: File, path: FsPath): Unit

    fun save(bytes: ByteArray, path: FsPath): Unit

    fun append(path: FsPath, bytes: ByteArray): Unit

    fun delete(path: FsPath): Unit

    fun move(path: FsPath, moveTo: FsPath): Unit

    fun read(path: FsPath): ByteArray

    fun ls(path: FsPath): List<FsEntity>

    fun allEntities(): List<FsEntity>

    companion object {

        @JvmInline
        value class FsPath(val value: String)

        data class FsEntity(
            val name: String,
            val fullPath: String,
            val isDirectory: Boolean,
            val size: Long
        )

        sealed class FsProblems(msg: String): RuntimeException(msg) {
            data class PathNotValid(val path: FsPath): FsProblems("path not valid $path")
            data class FileNotFoundProblem(val path: FsPath): FsProblems("file not found $path")
            data class PathAlreadyReservedProblem(val path: FsPath): FsProblems("Path is already reserved $path")
            data class PathDoesNotExistProblem(val path: FsPath): FsProblems("Path doesn't exits $path")
            data class PathIsNotDirectoryProblem(val path: FsPath): FsProblems("Path is not directory $path")
            data class ReadDirectoryProblem(val path: FsPath): FsProblems("read dir is not possible $path")
            data class DirectoryIsNotEmptyProblem(val path: FsPath): FsProblems("Directory is not empty $path")
            data class GenericProblem(val msg: String): FsProblems(msg)
        }
    }
}
