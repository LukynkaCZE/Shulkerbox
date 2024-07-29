package props

import map.ActiveMapSession
import map.Prop
import map.ShulkerboxMap
import map.commands.successSound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import toMiniMessage
import util.error

object PropManager {

    val propSelections: MutableMap<Player, Prop> = mutableMapOf()
    val moveItem = ItemStack(Material.SPECTRAL_ARROW, 1)

    fun getPropEntityFromProp(prop: Prop, map: ActiveMapSession): PropEntity? {
        return map.drawableProps.firstOrNull { it.prop.uid == prop.uid }
    }

    init {
        moveItem.editMeta {
            it.displayName("<yellow><underlined>Prop Move Tool".toMiniMessage())
            it.lore(mutableListOf(
                " ".toMiniMessage(),
                "<gray>When holding, your selected prop will move infront of you".toMiniMessage(),
                "<aqua>Left-Click <gray>to disable (removes the tool)".toMiniMessage(),
                " ".toMiniMessage(),
                "<dark_gray>Shulkerbox Map Manager Item".toMiniMessage()
            ))
            it.setEnchantmentGlintOverride(true)
            it.setMaxStackSize(1)
            it.persistentDataContainer.set(ShulkerboxPaper.namespacedKey, PersistentDataType.BOOLEAN, true)
        }
    }

    fun select(player: Player, prop: Prop) {
        if(propSelections[player] != null) unselect(player, false)
        propSelections[player] = prop
        player.successSound()
    }

    fun unselect(player: Player, sound: Boolean = true) {
        if(propSelections[player] == null) {
            error(player, "You do not have any prop selected!")
            return
        }
        propSelections.remove(player)
        if(sound) player.successSound(0.6f)
    }
}