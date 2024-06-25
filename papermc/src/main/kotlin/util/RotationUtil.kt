package util

import org.bukkit.util.EulerAngle
import org.joml.AxisAngle4d
import org.joml.AxisAngle4f
import org.joml.Vector4f
import kotlin.math.*

fun quaternionToEuler(q: Vector4f): EulerAngle {
    val ysqr = q.y * q.y
    val t0 = 2.0 * (q.w * q.x + q.y * q.z)
    val t1 = 1.0 - 2.0 * (q.x * q.x + ysqr)
    val roll = atan2(t0, t1)
    val t2 = 2.0 * (q.w * q.y - q.z * q.x)
    val pitch = if (abs(t2) >= 1.0) {
        (Math.PI / 2.0).withSign(t2)
    } else {
        asin(t2)
    }
    val t3 = 2.0 * (q.w * q.z + q.x * q.y)
    val t4 = 1.0 - 2.0 * (ysqr + q.z * q.z)
    val yaw = atan2(t3, t4)

    return EulerAngle(pitch, yaw, roll)
}

fun eulerToQuaternion(euler: EulerAngle): AxisAngle4f {
    val cx = cos(euler.x * 0.5)
    val sx = sin(euler.x * 0.5)
    val cy = cos(euler.y * 0.5)
    val sy = sin(euler.y * 0.5)
    val cz = cos(euler.z * 0.5)
    val sz = sin(euler.z * 0.5)

    val w = cx * cy * cz + sx * sy * sz
    val x = sx * cy * cz - cx * sy * sz
    val y = cx * sy * cz + sx * cy * sz
    val z = cx * cy * sz - sx * sy * cz

    return AxisAngle4f(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
}