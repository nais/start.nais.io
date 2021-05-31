package io.nais

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipTo(stream: OutputStream, contents: Map<String, String>) = ZipOutputStream(stream).use { zip ->
   contents.forEach { (path, content) ->
      zip.putNextEntry(ZipEntry(path))
      zip.write(content.encodeToByteArray())
      zip.closeEntry()
   }
}

