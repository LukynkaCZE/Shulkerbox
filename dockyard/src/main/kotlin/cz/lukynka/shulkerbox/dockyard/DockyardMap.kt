package cz.lukynka.shulkerbox.dockyard

import PointType
import io.github.dockyardmc.entities.*
import io.github.dockyardmc.entities.EntityManager.despawnEntity
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.schematics.Schematic
import io.github.dockyardmc.schematics.placeSchematic
import io.github.dockyardmc.scroll.Component
import io.github.dockyardmc.scroll.extensions.toComponent
import io.github.dockyardmc.utils.vectors.Vector3d
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

data class DockyardMap(
    val id: String,
    var name: String,
    val bounds: MutableList<DockyardBoundingBox> = mutableListOf(),
    val points: MutableList<DockyardPoint> = mutableListOf(),
    var props: MutableList<DockyardProp> = mutableListOf(),
    var size: Vector3d,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var origin: Location,
    val schematic: Schematic,
) {
    val spawnedProps = mutableMapOf<DockyardProp, ItemDisplay>()

    fun getPointsByType(type: PointType): Collection<DockyardPoint> {
        return points.filter { it.type == type }
    }

    fun getPointsById(id: String): Collection<DockyardPoint> {
        return points.filter { it.id == id }
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

    fun spawnProps() {
        props.forEach { prop -> spawnedProps[prop] = prop.spawn() }
    }

    fun spawnPropsWithTransform(transform: (DockyardProp) -> DockyardProp) {
        props.forEach { prop -> spawnedProps[prop] = prop.spawnWithTransform(transform) }
    }

    fun despawnProps() {
        spawnedProps.forEach { it.value.world.despawnEntity(it.value) }
    }

    fun placeSchematic(thenRun: (() -> Unit)) {
        origin.world.placeSchematic(schematic, origin)
        origin.world.placeSchematic {
            schematic = this@DockyardMap.schematic
            location = origin
            then = thenRun
        }
    }
}