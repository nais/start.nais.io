package io.nais

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipFile

class ZipperTest {

   @Test
   fun `write some elements to a zip file`() {
      val tmpZipFile = Files.createTempFile("zipped", ".zip").toFile()
      FileOutputStream(tmpZipFile).use { fileStream ->
         filesToZip.zipToStream(fileStream)
      }
      assertFileContents(tmpZipFile)
   }

   private fun assertFileContents(file: File) {
      val zipFile = ZipFile(file)
      filesToZip.forEach { (key, _) ->
         assertNotNull(zipFile.getEntry(key))
      }
   }

   private val filesToZip = mapOf(
      "relative/path/file1.txt" to "file1 contents",
      "relative/path/file2.txt" to "file2 contents"
   )
}
