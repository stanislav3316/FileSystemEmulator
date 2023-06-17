package com.jb

import java.nio.file.Files
import java.nio.file.Path

fun Path.createParentDirectoriesIfNeed() {
    val parentDirectories = this.parent
    if (parentDirectories != null) {
        Files.createDirectories(parentDirectories)
    }
}
