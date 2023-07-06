package me.redned.simcraft.city.network;

import lombok.Getter;
import lombok.ToString;
import me.redned.simreader.sc4.type.NetworkTile1;
import me.redned.simreader.sc4.type.network.NetworkBaseTexture;
import me.redned.simreader.sc4.type.network.NetworkType;
import me.redned.simreader.sc4.type.network.NetworkWealthTexture;
import org.cloudburstmc.math.vector.Vector3f;

@ToString
@Getter
public class NetworkTile1Data implements NetworkData {
    private static final int ADJUSTED_OFFSET = 1;

    private final NetworkTile1 networkTile;

    private final Vector3f minPosition;
    private final Vector3f maxPosition;
    private final Vector3f position;

    public NetworkTile1Data(NetworkTile1 networkTile) {
        this.networkTile = networkTile;

        this.minPosition = Vector3f.from(
                this.networkTile.getMinCoordinateX(),
                this.networkTile.getMinCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getMinCoordinateZ()
        );

        this.maxPosition = Vector3f.from(
                this.networkTile.getMaxCoordinateX(),
                this.networkTile.getMaxCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getMaxCoordinateZ()
        );

        this.position = Vector3f.from(
                this.networkTile.getCoordinateX(),
                this.networkTile.getCoordinateY() + ADJUSTED_OFFSET,
                this.networkTile.getCoordinateZ()
        );
    }

    @Override
    public NetworkType getNetworkType() {
        return this.networkTile.getNetworkType();
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
        byte connection = this.networkTile.getConnectionNorth();
        return connection == 0x01 || connection == 0x02 || connection == 0x03;
    }

    @Override
    public boolean hasEastConnectivity() {
        byte connection = this.networkTile.getConnectionEast();
        return connection == 0x01 || connection == 0x02 || connection == 0x03;
    }

    @Override
    public boolean hasSouthConnectivity() {
        byte connection = this.networkTile.getConnectionSouth();
        return connection == 0x01 || connection == 0x02 || connection == 0x03;
    }

    @Override
    public boolean hasWestConnectivity() {
        byte connection = this.networkTile.getConnectionWest();
        return connection == 0x01 || connection == 0x02 || connection == 0x03;
    }

    @Override
    public int getTextureId() {
        return this.networkTile.getTextureId();
    }

    @Override
    public byte getCrossingFlag() {
        return this.networkTile.getCrossingFlag();
    }

    @Override
    public NetworkWealthTexture getWealthTexture() {
        return this.networkTile.getWealthTexture();
    }

    @Override
    public NetworkBaseTexture getBaseTexture() {
        return this.networkTile.getBaseTexture();
    }
}
