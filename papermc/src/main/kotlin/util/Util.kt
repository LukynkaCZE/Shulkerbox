package util

import map.ShulkerboxMap
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

fun generateRandomString(length: Int = 3): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { characters.random() }
        .joinToString("")
}

fun generateUid(map: ShulkerboxMap): String {
    while (true) {
        val id = generateRandomString(3)
        if (!map.points.containsKey(id)) {
            return id
        }
    }
}


fun simpleSuggestion(vararg string: String): SuggestionProvider<CommandSender> {
    return SuggestionProvider.suggesting(string.map { Suggestion.suggestion(it) })
}

fun Location.toXYZString(): String {
    return "x: ${this.x}, x: ${this.y}, x: ${this.z}, yaw: ${this.yaw}, pitch: ${this.pitch}"
}