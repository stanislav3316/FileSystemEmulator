package com.jb

import java.io.File

interface FileSystem {

    //todo: declare domain exceptions
    //todo: should we implement `edit` operations?

    fun create(file: File): Unit

    fun read(path: Path): Array<Byte>

    fun append(path: Path, content: Array<Byte>): Unit

    fun delete(path: Path): Unit

    fun rename(path: Path, newName: FileName): Unit

    fun move(path: Path, newPath: Path): Unit

    companion object {

        @JvmInline
        value class Path(val value: String)

        @JvmInline
        value class FileName(val value: String)
    }
}
