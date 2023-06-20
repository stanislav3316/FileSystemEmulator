## File System Emulator

### Design overview

The solution is based on `java.nio.file.FileSystem` using ZIP implementation without compression (noCompression=true).    

```
    fun build(fsPath: FsPath): FileSystem {
        val env = mapOf(
            ("create" to "true"),
            ("compressionMethod" to "STORED"),
            ("noCompression" to "true"),
        )
        val uri = URI.create("jar:file:${File(fsPath.value).absolutePath}")
        return FileSystems.newFileSystem(uri, env)
    }
```

This helps us to use `java.nio.file.Files` methods on emulated zip file system.

This solution is extensible, we can change zip implementation to our own by implementing `java.nio.file.FileSystem` (as example, for concurrent cases).

All operations are based on `nio` package where `Files` provides a convenient and high-level API for performing file-related operations in Java. 
It abstracts away the underlying complexities of interacting with the file system by leveraging the capabilities of the 
Java NIO framework and the specific file system providers.

### Simple load test
- total time is 53_118 ms
- total volume is 5_840_574_700 bytes
- rate is 110_199_522 op/sec

### Notes
1. right now this decision is suitable for single-thread usage (was not tested in concurrent environments).
2. all tests (unit + integration + simple load-test) are located into `test.con.jb` package

