package com.jb.loadtest

import com.jb.FileSystem
import com.jb.Tests.iterateFolderContents
import com.jb.fs.InFileFS
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.FunSpec
import java.io.File
import java.nio.file.Files

@Ignored("run manually")
class LoadTestSpec : FunSpec({

    val zipFilePath = "/tmp/loadtest.zip"
    val filesFolder = File("/Users/stanislavbolsun/Downloads/").normalize() // TODO: replace with yours

    afterEach {
        File(zipFilePath).delete()
    }

    /**
     * (prev results)
     *
     * total time is 53_118 ms
     * total volume is 5_840_574_700 bytes
     * rate is 110_199_522 op/sec
     * */
    test("simple load test with rate") {
        val originalSize =
            Files
                .walk(filesFolder.toPath())
                .filter { path -> path.toFile().isFile }
                .mapToLong { path -> path.toFile().length() }
                .sum()

        val start = System.currentTimeMillis()
        InFileFS(FileSystem.Companion.FsPath(zipFilePath)).use { fs ->
            iterateFolderContents(filesFolder, fs)
        }
        val end = System.currentTimeMillis()

        val delta = end - start

        println("total time is $delta ms")
        println("total volume is $originalSize bytes")
        println("rate is ${originalSize / (delta / 1000)} op/sec")
    }
})
