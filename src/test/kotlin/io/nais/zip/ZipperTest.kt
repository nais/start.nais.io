package io.nais.zip

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile

class ZipperTest {

   @Test
   fun `zip a directory recursively`() {
      val dirToZip = File(object {}.javaClass.getResource("/zipping").toURI())
      val zipFile = zipToFile(dirToZip)
      assertTrue(zipFile.exists())
      assertFileContents(zipFile)
   }

   private fun assertFileContents(file: File) {
      val zipFile = ZipFile(file)
      assertTrue(zipFile.getEntry("./zipping/").isDirectory)
      assertFalse(zipFile.getEntry("./zipping/test.txt").isDirectory)
      assertTrue(zipFile.getEntry("./zipping/sub1/").isDirectory)
      assertTrue(zipFile.getEntry("./zipping/sub1/sub2/").isDirectory)
      assertFalse(zipFile.getEntry("./zipping/sub1/sub2/anothertest.txt").isDirectory)
      assertTrue(zipFile.getEntry("./zipping/sub1/sub2/anothertest.txt").size > 0)
   }

}
