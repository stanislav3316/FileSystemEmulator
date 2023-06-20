package com.jb

import com.jb.fs.InFileFS
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.streams.toList

object Tests {

    fun absolutePath(pathToFile: String): String {
        return File(pathToFile).absolutePath
    }

    fun createFile(size: Long, filename: String): File {
        Files.createFile(Path.of(filename))

        val file = File(filename)
        val raf = RandomAccessFile(file, "rw")
        raf.setLength(size)
        raf.close()
        return file
    }

    fun iterateFolderContents(folder: File, fs: InFileFS) {
        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        iterateFolderContents(file, fs)
                    } else if (!file.isHidden) {
                        val path = file.toPath()
                        val fsPath = FileSystem.Companion.FsPath(path.pathString)
                        fs.save(file, fsPath)
                    }
                }
            }
        }
    }

    fun originalSize(projectFolder: File): Long {
        return Files
            .walk(projectFolder.toPath())
            .filter { path -> path.toFile().isFile && !path.toFile().isHidden }
            .mapToLong { path -> path.toFile().length() }
            .sum()
    }

    fun originalPaths(projectFolder: File): List<String> {
        return Files
            .walk(projectFolder.toPath())
            .filter { path ->
                val isNotHidden = !path.isHidden()
                val nonEmpyDir =
                    if (path.isDirectory()) {
                        path.listDirectoryEntries().any { !it.isHidden() }
                    } else {
                        true
                    }
                isNotHidden && nonEmpyDir
            }
            .map { path -> path.pathString }
            .toList<String>()
    }
}
