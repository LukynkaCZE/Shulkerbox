package map

import ShulkerboxPaper
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import files.MapFileReader
import files.MapFileWriter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import props.PropManager
import sendPrefixed
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

object MapManager {
    val maps = mutableMapOf<String, ShulkerboxMap>()
    val mapSelections = mutableMapOf<Player, ActiveMapSession>()

    fun select(player: Player, map: ShulkerboxMap) {
        if(hasMapSelected(player)) unselect(player, true)
        PropManager.unselect(player)
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1.5f)
        player.sendPrefixed("<gray>Selected map <yellow>${map.name}<gray>!")
        mapSelections[player] = ActiveMapSession(player, map)
    }

    fun unselect(player: Player, silent: Boolean = false) {
        val activeMap = mapSelections[player]!!
        PropManager.unselect(player)
        player.sendPrefixed("<gray>Unselected map <red>${activeMap.map.name}<gray>!")
        activeMap.dispose()
        mapSelections.remove(player)
        if(!silent) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 0.5f)
    }

    fun hasMapSelected(player: Player): Boolean {
        return mapSelections.containsKey(player)
    }

    fun selectedShulkerboxMap(player: Player): ShulkerboxMap? {
        return mapSelections[player]?.map
    }

    fun fromJson(input: String): ShulkerboxMap {
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val map = json.decodeFromString<ShulkerboxMap>(input)
        return map
    }

    fun generateRegistryFileJson(): String {
        val entries = mutableListOf<ShulkerboxBuildServerRegistryEntry>()
        MapManager.maps.forEach { map ->
            entries.add(ShulkerboxBuildServerRegistryEntry(map.key, map.value.origin!!.toShulkerboxLocation()))
        }
        val registry = ShulkerboxBuildServerRegistry(entries)
        val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
        val out = json.encodeToString<ShulkerboxBuildServerRegistry>(registry)
        return out
    }

    fun save(map: ShulkerboxMap) {
        val folder = File("Shulkerbox/temp/${map.id}/")
        folder.mkdirs()
        val file = File("Shulkerbox/temp/${map.id}/map.json")
        file.delete()
        file.createNewFile()
        file.writeText(map.toJson())
        if(ShulkerboxPaper.isBuildServer) {
            val registryFolder = File("Shulkerbox/")
            registryFolder.mkdirs()
            val registryFile = File("Shulkerbox/build_server_registry.json")
            registryFile.delete()
            registryFile.createNewFile()
            registryFile.writeText(generateRegistryFileJson())
        }

        // schem
        val pos1 = BlockVector3.at(map.origin!!.x, map.origin!!.y, map.origin!!.z)
        val origin2 = map.origin!!.clone().add(map.size.toBukkitVector())
        val pos2 = BlockVector3.at(origin2.x, origin2.y, origin2.z)
        val region = CuboidRegion(pos1, pos2)
        println(map.origin.toString())
        region.world = BukkitAdapter.asBukkitWorld(BukkitWorld(map.origin!!.world))

        // get it,save to file
        val clipboard = BlockArrayClipboard(region)

        val forwardExtentCopy = ForwardExtentCopy(
            region.world, region, clipboard, region.minimumPoint
        )
        forwardExtentCopy.isCopyingEntities = false
        Operations.complete(forwardExtentCopy)

        val schematicFile = File("Shulkerbox/temp/${map.id}/map.schem")
        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(FileOutputStream(schematicFile)).use { writer ->
            writer.write(clipboard)
        }

        MapFileWriter.writeMap(map)
    }

    fun loadMapsFromBuildServerRegistry() {
        val registryFile = File("Shulkerbox/build_server_registry.json")
        val registry = Json.decodeFromString<ShulkerboxBuildServerRegistry>(registryFile.readText())
        registry.entries.forEach { entry ->
            val file = File("Shulkerbox/maps/${entry.mapId}.shulker")
            if(!file.exists()) {
                println("Error while loading ${entry.mapId}: Map file is not present in maps folder!")
                return@forEach
            }
            val mapJson = MapFileReader.load(file)
            val map = fromJson(mapJson.json)
            map.origin = entry.location.toBukkitLocation()
            if(map.origin!!.world == null) {
                throw Exception("uhh this should not be null: ${map.id} (${entry.location.world} | ${Bukkit.getWorlds().map { it.name }})")
            }
            maps[map.id] = map
            cleanupEntities()
            println("Loaded map ${map.id}")
        }
    }

    fun cleanupEntities() {
        maps.forEach {
            val mapLocation = it.value.origin!!

            val centerChunk = mapLocation.chunk
            val centerX = centerChunk.x
            val centerZ = centerChunk.z
            val loadRadius = 5

            for (x in centerX - loadRadius..centerX + loadRadius) {
                for (z in centerZ - loadRadius..centerZ + loadRadius) {
                    val distanceSquared = abs(x - centerX) * abs(x - centerX) + abs(z - centerZ) * abs(z - centerZ)
                    if (distanceSquared <= loadRadius * loadRadius) {
                        val chunk = mapLocation.world.getChunkAt(x, z)
                        if (!chunk.isLoaded) {
                            chunk.load()
                        }
                    }
                }
            }
            val entities = mapLocation.getNearbyEntities(30.0, 30.0, 30.0)
            entities.forEach { entity ->
                if(entity.persistentDataContainer.get(ShulkerboxPaper.shulkerboxBoundingBoxEntityTag, PersistentDataType.BOOLEAN) == true) {
                    entity.remove()
                }
            }
        }
    }
}