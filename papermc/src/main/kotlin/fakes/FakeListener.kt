package fakes

import io.papermc.paper.event.player.PlayerPickEntityEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class FakeListener: Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, ShulkerboxPaper.instance)
    }

    @EventHandler
    fun onPick(event: PlayerPickEntityEvent) {
//        event.player.send("<lime>clicked: ${event.entity}")
    }

}