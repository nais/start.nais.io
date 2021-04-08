package io.nais

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile

class ZipperTest {

   @Test
   fun `zip some files to a stream`() {
      val tmpZipFile = Files.createTempFile("zipped", ".zip").toFile()
      FileOutputStream(tmpZipFile).use {
         zipTo(it, filesToZip)
      }
      assertFileContents(tmpZipFile)
   }

   private fun assertFileContents(file: File) {
      val zipFile = ZipFile(file)
      assertTrue(zipFile.getEntry("relative/path/file1.txt").size > 0)
      assertTrue(zipFile.getEntry("relative/path/file2.txt").size > 0)
   }

   private val filesToZip = mapOf(
      Paths.get("relative", "path", "file1.txt") to "file1 contents",
      Paths.get("relative", "path", "file2.txt") to "file2 contents"
   )
}
