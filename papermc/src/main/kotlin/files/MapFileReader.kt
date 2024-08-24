@file:Suppress("UnstableApiUsage")

package files

import org.bukkit.Bukkit
import java.io.File
import java.util.logging.Level
import java.util.zip.ZipFile

object MapFileReader {

    fun load(file: File): PacketShulkerboxMap {
        Bukkit.getLogger().log(Level.INFO, "Loading ${file.name}")
        require(file.name.contains(".shulker")) { "file is not in the .shulker format!" }
        val zip = ZipFile(file)
        var json: String? = null
        zip.entries().toList().forEach { entry ->
            if(entry.name == "map.json") {
                zip.getInputStream(entry).use { stream ->
                    json = stream.bufferedReader().use { it.readText() }
                }
            }
        }

        if(json == null) throw Exception("map.json was not found in the map file!")

        return PacketShulkerboxMap(json!!)
    }
}

data class PacketShulkerboxMap(val json: String)