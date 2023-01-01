package me.redned.simcraft.city.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.redned.simreader.sc4.type.NetworkTile1;
import me.redned.simreader.sc4.type.NetworkType;
import org.cloudburstmc.math.vector.Vector3f;

@ToString
@Getter
@RequiredArgsConstructor
public class NetworkTile1Data implements NetworkData {
    private static final int ADJUSTED_OFFSET = 1;

    private final NetworkTile1 networkTile;

    @Override
    public NetworkType getNetworkType() {
        return this.networkTile.getNetworkType();
    }

    @Override
    public Vector3f getMinPosition() {
        return Vector3f.from(
                this.networkTile.getMinCoordinateX(),
                this.networkTile.getMinCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getMinCoordinateZ()
        );
    }

    @Override
    public Vector3f getMaxPosition() {
        return Vector3f.from(
                this.networkTile.getMaxCoordinateX(),
                this.networkTile.getMaxCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getMaxCoordinateZ()
        );
    }

    public Vector3f getPosition() {
        return Vector3f.from(
                this.networkTile.getCoordinateX(),
                this.networkTile.getCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getCoordinateZ()
        );
    }

    @Override
    public int getRotation() {
        return switch (this.networkTile.getOrientation()) {
            case 1 -> 90;
            case 2 -> 0;
            case 3 -> 270;
            case 4 -> 180;
            default -> 0;
        };
    }

    @Override
    public byte getOrientation() {
        return this.networkTile.getOrientation();
    }

    @Override
    public boolean isVisible() {
        return (this.networkTile.getAppearanceFlag() & 0x01) != 0;
    }

    @Override
    public boolean shouldDisplay() {
        return this.isVisible();
    }

    @Override
    public boolean hasNorthConnectivity() {
        return this.networkTile.getConnectionNorth() == 0x02;
    }

    @Override
    public boolean hasEastConnectivity() {
        return this.networkTile.getConnectionEast() == 0x02;
    }

    @Override
    public boolean hasSouthConnectivity() {
        return this.networkTile.getConnectionSouth() == 0x02;
    }

    @Override
    public boolean hasWestConnectivity() {
        return this.networkTile.getConnectionWest() == 0x02;
    }

    @Override
    public int getTextureId() {
        return this.networkTile.getTextureId();
    }

    @Override
    public byte getCrossingFlag() {
        return this.networkTile.getCrossingFlag();
    }
}
