package me.redned.simcraft.util;

import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;

public class MathUtil {

    public static Vector3i rotateAroundYAxis(int x, int z, double axisX, double axisY, double axisZ, int angle) {
        double dx = x - axisX;
        double dz = z - axisZ;

        Vector3d vec3d = rotateAroundY(dx, 0, dz, angle).add(axisX, axisY, axisZ);
        return vec3d.ceil().toInt();
    }

    public static Vector3d rotateAroundY(double x, double y, double z, int angle) {
        double cos = fastCos(angle);
        double sin = fastSin(angle);

        double xPos = cos * x + sin * z;
        double zPos = -sin * x + cos * z;

        return Vector3d.from(xPos, y, zPos);
    }

    private static double fastCos(int degrees) {
        if (degrees % 90 != 0) {
            return Math.cos(Math.toRadians(degrees));
        }

        degrees %= 360;
        if (degrees < 0) {
            degrees += 360;
        }

        return switch (degrees) {
            case 0 -> 1.0;
            case 90, 270 -> 0.0;
            case 180 -> -1.0;
            default -> 0.0;
        };
    }

    private static double fastSin(int degrees) {
        if (degrees % 90 != 0) {
            return Math.sin(Math.toRadians(degrees));
        }

        degrees %= 360;
        if (degrees < 0) {
            degrees += 360;
        }

        return switch (degrees) {
            case 0, 180 -> 0.0;
            case 90 -> 1.0;
            case 270 -> -1.0;
            default -> 0.0;
        };
    }
}
