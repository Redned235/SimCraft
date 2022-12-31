package me.redned.simcraft.util;

import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;

public class MathUtil {

    public static Vector3i rotateAroundYAxis(Vector3i pos, Vector3d axis, int angle) {
        Vector3d vec3d = rotateAroundY(pos.toDouble().sub(axis).mul(1, 0, 1), angle).add(axis);
        return vec3d.ceil().toInt();
    }

    public static Vector3d rotateAroundY(Vector3d pos, int angle) {
        double cos = fastCos(angle);
        double sin = fastSin(angle);

        double x = cos * pos.getX() + sin * pos.getZ();
        double z = -sin * pos.getX() + cos * pos.getZ();

        return Vector3d.from(x, pos.getY(), z);
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
