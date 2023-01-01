package me.redned.simcraft.city.placeable;

import org.cloudburstmc.math.vector.Vector3f;

public interface PlaceableData {

    String getIdentifier();

    Vector3f getMinPosition();

    Vector3f getMaxPosition();

    int getRotation();

    byte getOrientation();

    boolean isVisible();

    boolean shouldDisplay();
}
