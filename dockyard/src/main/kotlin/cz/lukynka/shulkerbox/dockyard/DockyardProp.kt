package cz.lukynka.shulkerbox.dockyard

import io.github.dockyardmc.entity.*
import io.github.dockyardmc.entity.EntityManager.spawnEntity
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.scroll.Component
import io.github.dockyardmc.scroll.extensions.toComponent
import io.github.dockyardmc.utils.Quaternion
import io.github.dockyardmc.utils.vectors.Vector3d
import cz.lukynka.shulkerbox.dockyard.youkai.YoukaiPack
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

data class DockyardProp(
    var uid: String,
    var location: Location,
    var yaw: Float,
    var pitch: Float,
    var meta: MutableMap<String, String> = mutableMapOf(),
    val translation: Vector3d,
    val leftRotation: Quaternion,
    val scale: Vector3d,
    val rightRotation: Quaternion,
    var brightness: Int?,
    var itemStack: ItemStack,
    var youkaiModelId: String? = null,
) {

    fun getItem(): ItemStack {
        return if(youkaiModelId != null) YoukaiPack.getItem(youkaiModelId!!) else itemStack
    }

    fun spawn(): ItemDisplay {
        val display = location.world.spawnEntity(ItemDisplay(location, location.world)) as ItemDisplay
        display.autoViewable = true
        display.renderDistanceBlocks = 999

        display.item.value = getItem()
        display.scale.value = scale.toVector3f()
        display.renderType.value = ItemDisplayRenderType.HEAD

        val rotKey = EntityMetadataType.DISPLAY_ROTATION_RIGHT
        display.metadata[rotKey] = EntityMetadata(rotKey, EntityMetaValue.QUATERNION, rightRotation)

        val transKey = EntityMetadataType.DISPLAY_TRANSLATION
        display.metadata[transKey] =
            EntityMetadata(transKey, EntityMetaValue.VECTOR3, translation.toVector3f())

        if (brightness != null) display.brightness.value = brightness!!
        return display
    }

    fun spawnWithTransform(prop: (DockyardProp) -> DockyardProp): ItemDisplay {
        prop.invoke(this)
        return spawn()
    }

    inline fun <reified T> getMeta(key: String): T {
        return getMetaOrNull<T>(key) ?: throw IllegalStateException("No value for key $key was found in map metadata!")
    }

    inline fun <reified T> getMetaOrNull(key: String): T? {
        val value = meta[key] ?: return null
        return when(T::class) {
            String::class -> value as T
            Boolean::class -> value.toBoolean() as T
            Int::class -> value.toInt() as T
            Float::class -> value.toFloat() as T
            Long::class -> value.toLong() as T
            Double::class -> value.toDouble() as T
            Byte::class -> value.toByte() as T
            Component::class -> value.toComponent() as T
            else -> throw IllegalArgumentException("No deserializer for generic ${T::class.simpleName} for map metadata found!")
        }
    }
}