package util

import map.ShulkerboxMap
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

fun simpleSuggestion(string: String): SuggestionProvider<CommandSender> {
    return SuggestionProvider.suggesting(Suggestion.suggestion(string))
}