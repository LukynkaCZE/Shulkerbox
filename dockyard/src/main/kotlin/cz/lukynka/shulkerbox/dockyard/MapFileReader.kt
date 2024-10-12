package cz.lukynka.shulkerbox.dockyard

import ShulkerboxMap
import io.github.dockyardmc.schematics.Schematic
import io.github.dockyardmc.schematics.SchematicReader
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.IllegalStateException
import java.util.zip.ZipFile

object MapFileReader {

    init {
        ShulkerboxConfigManager.load()
    }

    fun read(file: File): DockyardShulkerboxMap {
        val zip = ZipFile(file)
        var json: String? = null
        var schematic: Schematic? = null
        zip.entries().toList().forEach { entry ->
            if(entry.name == "map.json") {
                zip.getInputStream(entry).use { stream ->
                    json = stream.bufferedReader().use { it.readText() }
                }
            }
            if(entry.name == "map.schem") {
                zip.getInputStream(entry).use { stream ->
                    schematic = SchematicReader.read(stream.readAllBytes())
                }
            }
        }
        if(json == null) throw IllegalStateException("map metadata entry was not found")
        if(schematic == null) throw IllegalStateException("map schematic entry was not found")

        val shulkerboxMap = Json.decodeFromString<ShulkerboxMap>(json!!)

        return DockyardShulkerboxMap(shulkerboxMap, schematic!!)
    }
}

data class DockyardShulkerboxMap(val map: ShulkerboxMap, val schematic: Schematic)