package cz.lukynka.shulkerbox.minestom.conversion

import cz.lukynka.shulkerbox.common.PropItemStack
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

fun PropItemStack.toItemStack(): ItemStack {
    var item = ItemStack.builder(Material.fromKey("minecraft:${material.lowercase()}") ?: throw IllegalStateException("could not load item from key $material"))
    if (enchanted) {
        item = item.glowing(true)
    }

    return item.customModelData(listOf(customModelData.toFloat()), listOf(), listOf(), listOf()).build()
}