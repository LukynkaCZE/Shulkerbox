package util

import ShulkerboxMap
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import org.joml.Quaternionf
import org.joml.Vector3f

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

fun snapRotationToAxis(rotation: Quaternionf, snap: Double = 25.0): Quaternionf {
    val angles = rotation.getEulerAnglesXYZ(Vector3f())
    val pitch = angles.x
    val yaw = angles.y
    val roll = angles.z

    val snapIncrement = Math.toRadians(snap).toFloat()
    val snappedPitch = Math.round(pitch / snapIncrement) * snapIncrement
    val snappedYaw = Math.round(yaw / snapIncrement) * snapIncrement
    val snappedRoll = Math.round(roll / snapIncrement) * snapIncrement

    return Quaternionf().rotationXYZ(snappedPitch, snappedYaw, snappedRoll)
}

fun simpleSuggestion(vararg string: String): SuggestionProvider<CommandSender> {
    return SuggestionProvider.suggesting(string.map { Suggestion.suggestion(it) })
}

fun Location.toXYZString(): String {
    return "x: ${this.x}, x: ${this.y}, x: ${this.z}, yaw: ${this.yaw}, pitch: ${this.pitch}"
}