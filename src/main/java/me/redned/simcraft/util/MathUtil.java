package me.redned.simcraft.util;

import org.cloudburstmc.math.vector.Vector3i;

public class MathUtil {

    public static Vector3i rotateAroundYAxis(int x, int z, double axisX, double axisY, double axisZ, int angle, boolean rotateCenter) {
        if (angle == 0) {
            return Vector3i.from(x, axisY, z + (axisX > axisZ ? 1 : 0));
        }

        if (angle == 90) {
            double zPlane = rotateCenter ? axisX : Math.min(axisZ, axisX);
            double xPlane = rotateCenter ? axisZ : Math.max(axisX, axisZ);

            return Vector3i.from(z + (zPlane - axisZ) + ((axisX > axisZ) ? 1 : 0), axisY, -x + (xPlane + axisX) - 1);
        }

        if (angle == 180) {
            return Vector3i.from(-x + (axisX * 2) - 1, axisY, (-z + (axisZ * 2)) - 1 + (axisX > axisZ ? -1 : 0));
        }

        if (angle == 270) {
            double zPlane = rotateCenter ? axisX : Math.min(axisZ, axisX);
            double xPlane = rotateCenter ? axisZ : Math.max(axisX, axisZ);

            return Vector3i.from(-z + (zPlane + axisZ) - 1, axisY, x + (xPlane - axisX) + ((axisZ > axisX) ? 1 : 0));
        }

        throw new IllegalArgumentException("Unsupported rotation angle " + angle);
    }
}
