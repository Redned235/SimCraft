package me.redned.simcraft.util;

import me.redned.levelparser.BlockState;

import java.util.HashMap;
import java.util.Map;

public class BlockRotationUtil {
    private static final Map<String, Integer> DIRECTION_TO_DEGREE = Map.of(
            "north", 0,
            "west", 90,
            "south", 180,
            "east", 270
    );

    public static BlockState rotate(BlockState state, int angle) {
        if (angle == 0 | state.getProperties().isEmpty()) {
            return state;
        }

        if (angle % 90 != 0) {
            throw new IllegalArgumentException("Angle used for rotation was not divisible by 90!");
        }

        Map<String, Object> properties = new HashMap<>(state.getProperties());
        String facingProperty = (String) state.getProperties().get("facing");
        if (facingProperty != null) {
            properties.put("facing", getRotation(facingProperty, angle));
        }

        Map<String, Object> facingProperties = new HashMap<>();
        for (String string : DIRECTION_TO_DEGREE.keySet()) {
            Object hasDirection = state.getProperties().get(string);
            if (hasDirection != null) {
                properties.remove(string);

                String rotation = getRotation(string, angle);
                facingProperties.put(rotation, hasDirection);
            }
        }

        properties.putAll(facingProperties);
        return BlockState.of(state.getIdentifier(), properties);
    }

    private static String getRotation(String direction, int degrees) {
        Integer directionDegree = DIRECTION_TO_DEGREE.get(direction);
        if (directionDegree == null) {
            return direction;
        }

        directionDegree += degrees;
        directionDegree %= 360;
        return switch (directionDegree) {
            case 0 -> "north";
            case 90 -> "west";
            case 180 -> "south";
            case 270 ->  "east";
            default -> throw new IllegalArgumentException("Unsupported degree: " + directionDegree);
        };
    }
}
