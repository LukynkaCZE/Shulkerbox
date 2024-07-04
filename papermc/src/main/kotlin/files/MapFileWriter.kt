package files

import map.ShulkerboxMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object MapFileWriter {

    fun writeMap(map: ShulkerboxMap) {
        val path = "Shulkerbox/maps/"
        val outFile = File(path)
        outFile.mkdirs()

        val filesToZip = mutableListOf("Shulkerbox/temp/${map.id}/map.json", "Shulkerbox/temp/${map.id}/map.schem")
        FileOutputStream("$path${map.id}.shulker").use { fos ->
            ZipOutputStream(fos).use { zos ->
                filesToZip.forEach { filePath ->
                    val file = File(filePath)
                    if(!file.exists()) return@forEach
                    FileInputStream(file).use { fis ->
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)

                        val buffer = ByteArray(1024)
                        var length: Int
                        while (fis.read(buffer).also { length = it } >= 0) {
                            zos.write(buffer, 0, length)
                        }

                        zos.closeEntry()
                    }
                }
            }
        }
    }
}