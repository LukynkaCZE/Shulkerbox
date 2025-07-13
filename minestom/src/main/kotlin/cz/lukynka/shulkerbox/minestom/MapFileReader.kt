package cz.lukynka.shulkerbox.minestom

import cz.lukynka.shulkerbox.common.ShulkerboxMap
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.hollowcube.schem.Schematic
import net.hollowcube.schem.SchematicReader
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile

object MapFileReader {

    init {
        ShulkerboxConfigManager.load()
    }

    fun read(file: File): MinestomShulkerboxMap {
        val zip = ZipFile(file)
        var json: String? = null
        var schematic: Schematic? = null
        zip.entries().toList().forEach { entry ->
            if (entry.name == "map.json") {
                zip.getInputStream(entry).use { stream ->
                    json = stream.bufferedReader().use { it.readText() }
                }
            }
            if (entry.name == "map.schem") {
                zip.getInputStream(entry).use { stream ->
                    schematic = SchematicReader().read(ByteArrayInputStream(stream.readAllBytes()))
                }
            }
        }
        if (json == null) throw IllegalStateException("map metadata entry was not found")
        if (schematic == null) throw IllegalStateException("map schematic entry was not found")

        val shulkerboxMap = Json.decodeFromString<ShulkerboxMap>(json!!)

        return MinestomShulkerboxMap(shulkerboxMap, schematic!!)
    }
}

data class MinestomShulkerboxMap(val map: ShulkerboxMap, val schematic: Schematic)