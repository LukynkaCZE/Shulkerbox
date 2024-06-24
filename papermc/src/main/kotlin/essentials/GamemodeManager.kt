package essentials

import devMessage
import org.bukkit.GameMode
import org.bukkit.entity.Player

fun Player.setGamemode(gameMode: GameMode) {
    this.gameMode = gameMode
    devMessage("Set gamemode of player <yellow>${this.name}<gray> to <aqua>${gameMode.name}")
}

fun MutableList<Player>.setGamemode(gameMode: GameMode) {
    this.forEach { it.setGamemode(gameMode) }
}