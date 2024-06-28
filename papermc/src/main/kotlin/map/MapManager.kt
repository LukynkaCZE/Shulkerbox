package map

import ShulkerboxPaper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import sendPrefixed
import java.io.File

object MapManager {
    val maps = mutableMapOf<String, ShulkerboxMap>()
    val mapSelections = mutableMapOf<Player, ActiveMap>()

    fun select(player: Player, map: ShulkerboxMap) {
        if(hasMapSelected(player)) unselect(player, true)
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1.5f)
        player.sendPrefixed("<gray>Selected map <yellow>${map.name}<gray>!")
        mapSelections[player] = ActiveMap(player, map)
    }

    fun unselect(player: Player, silent: Boolean = false) {
        val activeMap = mapSelections[player]!!
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
        val folder = File("Shulkerbox/maps/${map.id}/")
        folder.mkdirs()
        val file = File("Shulkerbox/maps/${map.id}/map.json")
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
    }

    fun loadMapsFromBuildServerRegistry() {
        val registryFile = File("Shulkerbox/build_server_registry.json")
        val registry = Json.decodeFromString<ShulkerboxBuildServerRegistry>(registryFile.readText())
        registry.entries.forEach { entry ->
            val file = File("Shulkerbox/maps/${entry.mapId}/map.json")
            val map = fromJson(file.readText())
            map.origin = entry.location.toBukkitLocation()
            if(map.origin!!.world == null) {
                throw Exception("uhh this should not be null: ${map.id} (${entry.location.world} | ${Bukkit.getWorlds().map { it.name }})")
            }
            maps[map.id] = map
        }
    }
}