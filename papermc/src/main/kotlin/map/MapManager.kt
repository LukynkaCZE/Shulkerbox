package map

import org.bukkit.Sound
import org.bukkit.entity.Player
import sendPrefixed

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
}