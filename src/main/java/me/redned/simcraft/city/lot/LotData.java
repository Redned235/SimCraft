package me.redned.simcraft.city.lot;

import lombok.Getter;
import lombok.ToString;
import me.redned.simreader.sc4.type.Lot;
import me.redned.simreader.sc4.type.LotZoneType;
import me.redned.simreader.sc4.type.LotZoneWealth;
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

    public LotZoneType getZoneType() {
        return this.lot.getZoneType();
    }

    public LotZoneWealth getZoneWealth() {
        return this.lot.getZoneWealth();
    }

    public boolean isIndustrial() {
        LotZoneType zoneType = this.lot.getZoneType();
        return switch (zoneType) {
            case INDUSTRIAL_LOW, INDUSTRIAL_MEDIUM, INDUSTRIAL_HIGH -> true;
            default -> false;
        };
    }

    public boolean isResidential() {
        LotZoneType zoneType = this.lot.getZoneType();
        return switch (zoneType) {
            case RESIDENTIAL_LOW, RESIDENTIAL_MEDIUM, RESIDENTIAL_HIGH -> true;
            default -> false;
        };
    }

    public boolean isCommercial() {
        LotZoneType zoneType = this.lot.getZoneType();
        return switch (zoneType) {
            case COMMERCIAL_LOW, COMMERCIAL_MEDIUM, COMMERCIAL_HIGH -> true;
            default -> false;
        };
    }
}
