package com.jb.fs

import com.jb.FileSystem.Companion.FsPath
import com.jb.InFileFS
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.io.RandomAccessFile

class InFileFSSpec: FunSpec({

    val zipFilePath = "src/main/resources/archive.zip"

    afterEach {
        File(zipFilePath).delete()
    }

    test("should save and read file") {

        fun createFile(size: Long, filename: String): File {
            val file = File(filename)
            file.createNewFile()

            val raf = RandomAccessFile(file, "rw")
            raf.setLength(size)
            raf.close()
            return file
        }

        InFileFS(FsPath(zipFilePath)).use { fs ->
            val file = createFile(1000L, "src/main/resources/test")
            fs.save(File("src/main/resources/test"), FsPath("./new_file"))

            fs.read(FsPath("./new_file")) shouldBe file.readBytes()
        }
    }
})
