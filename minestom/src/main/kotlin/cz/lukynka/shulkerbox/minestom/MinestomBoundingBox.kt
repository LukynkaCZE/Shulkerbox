package cz.lukynka.shulkerbox.minestom

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance

data class MinestomBoundingBox(
    var id: String,
    var origin: Point,
    var world: Instance,
    var size: Vec,
    var meta: MutableMap<String, String> = mutableMapOf(),
) {

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

//    fun toBound(): Bound {
//        val firstPoint = origin
//        val secondPoint = origin.clone().add(size)
//        return Bound(firstPoint, secondPoint)
//    }
}