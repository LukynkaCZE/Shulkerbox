package cz.lukynka.shulkerbox.minestom

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.item.ItemStack

data class MinestomProp(
    var uid: String,
    var location: Point,
    var world: Instance,
    var yaw: Float,
    var pitch: Float,
    var meta: MutableMap<String, String> = mutableMapOf(),
    val translation: Vec,
    val leftRotation: List<Float>,
    val scale: Vec,
    val rightRotation: List<Float>,
    var brightness: Int?,
    var itemStack: ItemStack,
    var youkaiModelId: String? = null,
) {

    fun getItem(): ItemStack {
        return itemStack
    }

//    fun spawn(): ItemDisplay {
//        val display = location.world.spawnEntity(ItemDisplay(location, location.world)) as ItemDisplay
//        display.autoViewable = true
//
//        display.item.value = getItem()
//        display.scale.value = scale.toVector3f()
//        display.renderType.value = ItemDisplayRenderType.HEAD
//
//        val rotKey = EntityMetadataType.DISPLAY_ROTATION_RIGHT
//        display.metadata[rotKey] = EntityMetadata(rotKey, EntityMetaValue.QUATERNION, rightRotation)
//
//        val transKey = EntityMetadataType.DISPLAY_TRANSLATION
//        display.metadata[transKey] =
//            EntityMetadata(transKey, EntityMetaValue.VECTOR3, translation.toVector3f())
//
//        if (brightness != null) display.brightness.value = brightness!!
//        return display
//    }

//    fun spawnWithTransform(prop: (MinestomProp) -> MinestomProp): ItemDisplay {
//        prop.invoke(this)
//        return spawn()
//    }

    inline fun <reified T> getMeta(key: String): T {
        return getMetaOrNull<T>(key) ?: throw IllegalStateException("No value for key $key was found in map metadata!")
    }

    inline fun <reified T> getMetaOrNull(key: String): T? {
        val value = meta[key] ?: return null
        return when (T::class) {
            String::class -> value as T
            Boolean::class -> value.toBoolean() as T
            Int::class -> value.toInt() as T
            Float::class -> value.toFloat() as T
            Long::class -> value.toLong() as T
            Double::class -> value.toDouble() as T
            Byte::class -> value.toByte() as T
            else -> throw IllegalArgumentException("No deserializer for generic ${T::class.simpleName} for map metadata found!")
        }
    }
}