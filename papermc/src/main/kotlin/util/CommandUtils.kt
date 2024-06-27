package util

import org.bukkit.Sound
import org.bukkit.entity.Player
import selection.SelectionManager
import send

fun error(player: Player, error: String) {
    player.send("${SelectionManager.prefix} <red>$error!")
    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 1f)
}