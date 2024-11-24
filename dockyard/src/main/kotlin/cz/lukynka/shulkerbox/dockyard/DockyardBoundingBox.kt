package cz.lukynka.shulkerbox.dockyard

import io.github.dockyardmc.bounds.Bound
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.scroll.Component
import io.github.dockyardmc.scroll.extensions.toComponent
import io.github.dockyardmc.utils.vectors.Vector3d
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

data class DockyardBoundingBox(
    var id: String,
    var origin: Location,
    var size: Vector3d,
    var meta: MutableMap<String, String> = mutableMapOf(),
) {

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

    fun toBound(): Bound {
        val firstPoint = origin
        val secondPoint = origin.clone().add(size)
        return Bound(firstPoint, secondPoint)
    }
}