package util

import ShulkerboxPaper
import org.bukkit.Bukkit
import org.bukkit.event.Listener

open class SelfListener: Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }
}