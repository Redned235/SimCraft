package me.redned.simcraft.city.lot;

import lombok.Getter;
import lombok.ToString;
import me.redned.simreader.sc4.type.Lot;
import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector2i;

@ToString
@Getter
public class LotData {
    private final Lot lot;

    public LotData(Lot lot) {
        this.lot = lot;
    }

    public Vector2i getMinTilePosition() {
        return Vector2i.from(
                this.lot.getMinTileX(),
                this.lot.getMinTileZ()
        );
    }

    public Vector2i getMaxTilePosition() {
        return Vector2i.from(
                this.lot.getMaxTileX(),
                this.lot.getMaxTileZ()
        );
    }

    public Vector2i getSize() {
        return Vector2i.from(
                this.lot.getSizeX(),
                this.lot.getSizeZ()
        );
    }

    public float getYPosition() {
        return this.lot.getPositionY();
    }
}
