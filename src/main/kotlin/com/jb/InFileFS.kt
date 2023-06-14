package com.jb

import com.jb.FileSystem.Companion.FsEntity
import com.jb.FileSystem.Companion.FsFileName
import com.jb.FileSystem.Companion.FsPath
import java.io.File

class InFileFS(val fs: FsPath) : FileSystem {

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
}