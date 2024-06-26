package selection

import org.bukkit.Location
import org.bukkit.util.Vector

class Selection(var basePoint: Location) {

    private var secondPoint: Location? = null

    fun setSecondPoint(location: Location) {
        this.secondPoint = location
        boundingBoxEntity.setSize(getBoundingBoxSize())
    }

    fun setFirstPoint(location: Location) {
        this.basePoint = location
        boundingBoxEntity.setLocation(basePoint)
        boundingBoxEntity.setSize(getBoundingBoxSize())
    }

    fun getSecondPoint(): Location? {
        return secondPoint
    }

    var boundingBoxEntity = BoundingBoxEntity(basePoint, Vector(1, 1, 1))



    fun getBoundingBoxSize(): Vector {
        if(secondPoint == null) return Vector(1, 1, 1)

        var x = secondPoint!!.x - basePoint.x
        var y = secondPoint!!.y - basePoint.y
        var z = secondPoint!!.z - basePoint.z

        if(secondPoint!!.y > basePoint.y) y += 1.0
        if(secondPoint!!.y < basePoint.y) y -= 1.0

        if(secondPoint!!.z > basePoint.z) z += 1.0
        if(secondPoint!!.x > basePoint.x) x += 1.0

        return Vector(x, y, z)
    }
}