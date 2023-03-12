package me.redned.simcraft.city.network;

import me.redned.simreader.sc4.type.network.NetworkBaseTexture;
import me.redned.simreader.sc4.type.network.NetworkType;
import me.redned.simreader.sc4.type.network.NetworkWealthTexture;
import org.cloudburstmc.math.vector.Vector3f;

public interface NetworkData {

    NetworkType getNetworkType();

    Vector3f getMinPosition();

    Vector3f getPosition();

    Vector3f getMaxPosition();

    int getRotation();

    byte getOrientation();

    boolean isVisible();

    boolean shouldDisplay();

    boolean hasNorthConnectivity();

    boolean hasEastConnectivity();

    boolean hasSouthConnectivity();

    boolean hasWestConnectivity();

    int getTextureId();

    byte getCrossingFlag();

    NetworkWealthTexture getWealthTexture();

    NetworkBaseTexture getBaseTexture();
}
