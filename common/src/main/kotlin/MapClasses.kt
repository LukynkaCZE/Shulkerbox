import java.util.UUID

data class ShulkerboxMap(
    val name: String,
    val uuid: UUID,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val size: ShulkerVector3,
    val origin: ShulkerBlockPos
)

data class ShulkerVector3(
    val x: Int,
    val y: Int,
    val z: Int,
)

data class ShulkerLocation(
    val x: Int,
    val y: Int,
    val z: Int,
    val yaw: Float,
    val roll: Float
)

data class ShulkerBlockPos(
    val x: Int,
    val y: Int,
    val z: Int,
)