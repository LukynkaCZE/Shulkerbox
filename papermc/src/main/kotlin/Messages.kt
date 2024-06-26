import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import selection.SelectionManager

private var miniMessage = MiniMessage.miniMessage()

var debugColor = "<color:#ff6200>"
var systemColor = "<color:#a66bff>"

fun translatedMessage(message: String): Component {
    return miniMessage.deserialize(message)
}

fun devMessage(message: String) {
    Bukkit.getOnlinePlayers().forEach {
        if(!it.hasPermission("cc.admin")) return@forEach
        it.send("${debugColor}Dev <dark_gray>| <gray>$message")
    }
}

fun Player.send(message: String) {
    this.sendMessage(translatedMessage(message))
}

fun Player.sendPrefixed(message: String) {
    this.sendMessage(translatedMessage("${SelectionManager.prefix} <gray>$message"))
}

fun Player.sendDebugMessage(message: String) {
    this.send("${debugColor}Debug <dark_gray>| <gray>$message")
}

fun Player.sendSystemMessage(message: String) {
    this.send("${systemColor}System <dark_gray>| <gray>$message")
}

fun String.toMiniMessage(): Component {
    val rootComp = Component.text().decoration(TextDecoration.ITALIC, false).build()
    val comp = miniMessage.deserialize(this)

    return rootComp.append(comp)
}