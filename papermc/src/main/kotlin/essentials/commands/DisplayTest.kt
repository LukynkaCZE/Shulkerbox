package essentials.commands

import fakes.FakeItemDisplay
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.bukkit.parser.MaterialParser.materialParser
import org.incendo.cloud.bukkit.parser.PlayerParser.playerParser

class DisplayTest {
    var cm = ShulkerboxPaper.instance.commandManager

    private val fake = cm.commandBuilder("fake")
    var entity: FakeItemDisplay? = null

    init {
        cm.command(fake.literal("create")
            .required("item", materialParser())
            .handler {ctx ->
                val player = ctx.sender() as Player
                val item = ItemStack(ctx.get<Material>("item"))
                entity = FakeItemDisplay(player.location)
                entity!!.setItem(item)
            }
        )

        cm.command(fake.literal("add_viewer")
            .required("player", playerParser())
            .handler {ctx ->
                val player = ctx.get<Player>("player")
                entity?.addViewer(player)
            }
        )

        cm.command(fake.literal("remove_viewer")
            .required("player", playerParser())
            .handler {ctx ->
                val player = ctx.get<Player>("player")
                entity?.removeViewer(player)
            }
        )
    }
}