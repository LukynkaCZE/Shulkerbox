package props

import Prop
import map.ActiveMapSession
import map.commands.playSuccessSound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import toMiniMessage
import util.error

object PropManager {

    val propSelections: MutableMap<Player, Prop> = mutableMapOf()
    val propMoveToolItem = ItemStack(Material.SPECTRAL_ARROW, 1)

    fun getPropEntityFromProp(prop: Prop, map: ActiveMapSession): PropEntity? {
        return map.drawableProps.firstOrNull { it.prop.uid == prop.uid }
    }

    init {
        propMoveToolItem.editMeta {
            it.displayName("<yellow><underlined>Prop Move Tool".toMiniMessage())
            it.lore(mutableListOf(
                " ".toMiniMessage(),
                "<gray>When holding, your selected prop will move in-front of you".toMiniMessage(),
                "<gray>You can cycle modes by dropping the item <gold>(<key:key.drop>)".toMiniMessage(),
                " ".toMiniMessage(),
                "<dark_gray>Shulkerbox Map Manager Item".toMiniMessage()
            ))
            it.setEnchantmentGlintOverride(true)
            it.setMaxStackSize(1)
        }
    }

    fun select(player: Player, prop: Prop) {
        if(propSelections[player] != null) unselect(player, false)
        propSelections[player] = prop
        player.playSuccessSound()
    }

    fun unselect(player: Player, sound: Boolean = true) {
        if(propSelections[player] == null) {
            error(player, "You do not have any prop selected!")
            return
        }
        propSelections.remove(player)
        if(sound) player.playSuccessSound(0.6f)
    }
}