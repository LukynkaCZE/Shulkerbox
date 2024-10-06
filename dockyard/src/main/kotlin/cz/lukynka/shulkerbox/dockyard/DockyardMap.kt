package cz.lukynka.shulkerbox.dockyard

import PointType
import PropItemStack
import ShulkerboxQuaternionf
import ShulkerboxVector
import io.github.dockyardmc.DockyardServer
import io.github.dockyardmc.entities.EntityManager.despawnEntity
import io.github.dockyardmc.entities.EntityManager.spawnEntity
import io.github.dockyardmc.entities.ItemDisplay
import io.github.dockyardmc.entities.ItemDisplayRenderType
import io.github.dockyardmc.extentions.broadcastMessage
import io.github.dockyardmc.item.EnchantmentGlintOverrideItemComponent
import io.github.dockyardmc.item.ItemStack
import io.github.dockyardmc.location.Location
import io.github.dockyardmc.registry.registries.ItemRegistry
import io.github.dockyardmc.runnables.runLaterAsync
import io.github.dockyardmc.schematics.Schematic
import io.github.dockyardmc.schematics.placeSchematic
import io.github.dockyardmc.utils.Quaternion
import io.github.dockyardmc.utils.vectors.Vector3d

fun PropItemStack.toItemStack(): ItemStack {
    val baseItem = ItemRegistry["minecraft:${material.lowercase()}"]
    val item = ItemStack(baseItem)
    if(enchanted) item.components.add(EnchantmentGlintOverrideItemComponent(true))
    item.customModelData.value = customModelData
    return item
}

fun ShulkerboxVector.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

fun ShulkerboxVector.toLocation(origin: Location): Location {
    DockyardServer.broadcastMessage("adding ${this.toVector3d()} to $origin")
    return Location(
        origin.x + x,
        origin.y - y,
        origin.z + z,
        origin.world
    )
}

fun ShulkerboxQuaternionf.toQuaternion(): Quaternion {
    return Quaternion(x, y, z, w)
}

data class DockyardMap(
    val id: String,
    var name: String,
    val bounds: MutableList<DockyardBoundingBox> = mutableListOf(),
    val points: MutableList<DockyardPoint> = mutableListOf(),
    var props: MutableList<DockyardProp> = mutableListOf(),
    var size: Vector3d,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var origin: Location,
    val schematic: Schematic
) {

    val spawnedProps = mutableListOf<ItemDisplay>()

    fun getPointsByType(type: PointType): Collection<DockyardPoint> {
        return points.filter { it.type == type }
    }

    fun getPointsById(id: String): Collection<DockyardPoint> {
        return points.filter { it.id == id }
    }

    fun spawnProps() {
        props.forEach { prop ->
            val display = origin.world.spawnEntity(ItemDisplay(prop.location, origin.world)) as ItemDisplay
            display.autoViewable = true

            display.item.value = prop.itemStack
            display.scale.value = prop.scale.toVector3f()
            display.renderType.value = ItemDisplayRenderType.HEAD

            runLaterAsync(10) {
                DockyardServer.broadcastMessage("<orange>original: ${prop.rightRotation}, converted: ${quaternionToVector3f(prop.rightRotation)}")
                display.rotateTo(quaternionToVector3f(prop.rightRotation), 0)
                DockyardServer.broadcastMessage("<red>${prop.translation.toVector3f()}")
                display.translateTo(prop.translation.toVector3f(), 0)
            }

            spawnedProps.add(display)
        }
    }

    fun despawnProps() {
        spawnedProps.forEach {
            it.world.despawnEntity(it)
        }
    }

    fun placeSchematic() {
        origin.world.placeSchematic(schematic, origin)
    }

    companion object {
        fun fromShulkerboxMap(map: DockyardShulkerboxMap, origin: Location): DockyardMap {
            val sm = map.map

            val boundingBoxes = mutableListOf<DockyardBoundingBox>()
            sm.bounds.forEach {
                val bound = it.value
                val dockyardBound = DockyardBoundingBox(
                    bound.id,
                    bound.origin.toLocation(origin),
                    bound.size.toVector3d(),
                    bound.meta
                )
                boundingBoxes.add(dockyardBound)
            }

            val points = mutableListOf<DockyardPoint>()
            sm.points.forEach {
                val point = it.value
                val dockyardPoint = DockyardPoint(
                    point.id,
                    point.location.toLocation(origin).apply { yaw = point.yaw; pitch = point.pitch },
                    point.type,
                    point.meta,
                    point.uid
                )
                points.add(dockyardPoint)
            }

            val props = mutableListOf<DockyardProp>()
            sm.props.forEach {
                val prop = it.value
                val dockyardProp = DockyardProp(
                    prop.uid,
                    prop.location.toLocation(origin).apply { yaw = prop.yaw; pitch = prop.pitch },
                    prop.yaw,
                    prop.pitch,
                    prop.meta,
                    prop.transformation.translation.toVector3d(),
                    prop.transformation.leftRotation.toQuaternion(),
                    prop.transformation.scale.toVector3d(),
                    prop.transformation.rightRotation.toQuaternion(),
                    prop.brightness,
                    prop.itemStack.toItemStack(),
                    prop.youkaiModelId
                )
                props.add(dockyardProp)
            }

            val dockyardMap = DockyardMap(
                sm.id,
                sm.name,
                boundingBoxes,
                points,
                props,
                sm.size.toVector3d(),
                sm.meta,
                origin,
                schematic = map.schematic
            )
            return dockyardMap
        }
    }
}

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
    var youkaiModelId: String? = null
)

data class DockyardBoundingBox(
    var id: String,
    var origin: Location,
    var size: Vector3d,
    var meta: MutableMap<String, String> = mutableMapOf(),
)

data class DockyardPoint(
    var id: String,
    var location: Location,
    var type: PointType,
    var meta: MutableMap<String, String> = mutableMapOf(),
    var uid: String,
)