package com.jb

import com.jb.FileSystem.Companion.FileName
import com.jb.FileSystem.Companion.Path
import java.io.File

class InFileFS(val fs: Path) : FileSystem {

    override fun create(file: File) {
        TODO("Not yet implemented")
    }

    override fun read(path: Path): Array<Byte> {
        TODO("Not yet implemented")
    }

    override fun append(path: Path, content: Array<Byte>) {
        TODO("Not yet implemented")
    }

    override fun delete(path: Path) {
        TODO("Not yet implemented")
    }

    override fun rename(path: Path, newName: FileName) {
        TODO("Not yet implemented")
    }

    override fun move(path: Path, newPath: Path) {
        TODO("Not yet implemented")
    }
}