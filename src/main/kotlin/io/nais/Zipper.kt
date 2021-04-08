package io.nais

import java.io.OutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipTo(stream: OutputStream, contents: Map<Path, String>) = ZipOutputStream(stream).use { zip ->
   contents.forEach { (path, content) ->
      zip.putNextEntry(ZipEntry(path.toString()))
      zip.write(content.encodeToByteArray())
      zip.closeEntry()
   }
}

