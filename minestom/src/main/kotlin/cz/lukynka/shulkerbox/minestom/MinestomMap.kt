package cz.lukynka.shulkerbox.minestom

import cz.lukynka.shulkerbox.common.PointType
import net.hollowcube.schem.Rotation
import net.hollowcube.schem.Schematic
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import java.util.concurrent.CompletableFuture

data class MinestomMap(
    val id: String,
    var name: String,
    val bounds: MutableList<MinestomBoundingBox> = mutableListOf(),
    val points: MutableList<MinestomPoint> = mutableListOf(),
    var props: MutableList<MinestomProp> = mutableListOf(),
    var size: Vec,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var origin: Point,
    var world: Instance,
    val schematic: Schematic,
) {
//    val spawnedProps = mutableMapOf<MinestomProp, ItemDisplay>()

    fun getPoint(id: String): MinestomPoint {
        return points.first { it.id == id }
    }

    fun getBound(id: String): MinestomBoundingBox {
        return bounds.first { it.id == id }
    }

    fun getPointOrNull(id: String): MinestomPoint? {
        return points.firstOrNull { it.id == id }
    }

    fun getPointsByType(type: PointType): Collection<MinestomPoint> {
        return points.filter { it.type == type }
    }

    fun getPointsById(id: String): Collection<MinestomPoint> {
        return points.filter { it.id == id }
    }

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

//    fun spawnProps() {
//        props.forEach { prop -> spawnedProps[prop] = prop.spawn() }
//    }
//
//    fun spawnPropsWithTransform(transform: (MinestomProp) -> MinestomProp) {
//        props.forEach { prop -> spawnedProps[prop] = prop.spawnWithTransform(transform) }
//    }

//    fun despawnProps() {
//        spawnedProps.forEach { it.value.world.despawnEntity(it.value) }
//    }

    fun placeSchematic() {
        schematic.build(Rotation.NONE, false).apply(world, origin, null)
    }

    fun placeSchematicAsync(): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        schematic.build(Rotation.NONE, false).apply(world, origin, Runnable { future.complete(Unit) })
        return future
    }
}