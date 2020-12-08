package io.nais.zip

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files.createTempFile
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipToFile(toZip: File) = createTempFile("${UUID.randomUUID()}", ".zip").toFile().apply {
   ZipOutputStream(FileOutputStream(this)).use {
      zipIt(toZip, it)
   }
}

private fun zipIt(toZip: File, zipOut: ZipOutputStream, relativePath: String = "./"): Unit = with(zipOut) {
   if (toZip.isDirectory) {
      zipDirectory(toZip, this, relativePath)
   } else {
      zipFile(toZip, this, relativePath)
   }
}

private fun zipDirectory(dir: File, zipOut: ZipOutputStream, relativePath: String): Unit =
   with(zipOut) {
      val newPath = "${relativePath}${dir.name.appendSlash()}"
      zipOut.putNextEntry(ZipEntry(newPath))
      dir.listFiles()?.forEach { file -> zipIt(file, this, newPath) }
      zipOut.closeEntry()
   }

private fun zipFile(file: File, zipOut: ZipOutputStream, relativePath: String) {
   zipOut.putNextEntry(ZipEntry("${relativePath.appendSlash()}${file.name}"))
   zipOut.write(file.readBytes())
   zipOut.closeEntry()
}

private fun String.appendSlash() = if (this.endsWith("/")) this else "$this/"
