package selection

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import toMiniMessage

object SelectionManager {

    val selectionMap = mutableMapOf<Player, Selection>()

    val selectionItem = ItemStack(Material.LEAD, 1)
    val prefix = "<#884dff>Shulkerbox <dark_gray>|<gray>"

    init {
        selectionItem.editMeta {
            it.displayName("<green><underlined>Shulkerbox Selection Tool".toMiniMessage())
            it.lore(mutableListOf(
                " ".toMiniMessage(),
                "<gray>Select two positions by <aqua>Right-Clicking <gray>and".toMiniMessage(),
                "<aqua>Left-Clicking".toMiniMessage(),
                " ".toMiniMessage(),
                "<dark_gray>Shulkerbox Map Manager Item".toMiniMessage()
            ))
            it.setEnchantmentGlintOverride(true)
            it.setMaxStackSize(1)
        }
    }

    fun remove(player: Player) {
        val selection = selectionMap[player]
        if(selection != null) {
            selection.dispose()
            selectionMap.remove(player)
        }
    }

    fun create(player: Player, location: Location): Selection {
        val selection = if(!selectionMap.containsKey(player)) {
            val sel = Selection(location, player)
            sel.setSecondPoint(location)
            selectionMap[player] = sel
            sel

        } else selectionMap[player]
        return selection!!
    }
}