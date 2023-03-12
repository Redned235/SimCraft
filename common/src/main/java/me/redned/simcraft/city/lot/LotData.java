package me.redned.simcraft.city.lot;

import lombok.Getter;
import lombok.ToString;
import me.redned.simcraft.city.placeable.BuildingData;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.exemplar.property.ExemplarPropertyTypes;
import me.redned.simreader.sc4.storage.exemplar.property.type.OccupantGroupTypesProperty;
import me.redned.simreader.sc4.storage.exemplar.type.ExemplarSubfile;
import me.redned.simreader.sc4.type.Lot;
import me.redned.simreader.sc4.type.lot.LotZoneType;
import me.redned.simreader.sc4.type.lot.LotZoneWealth;
import me.redned.simreader.sc4.type.occupant.OccupantGroupType;
import org.cloudburstmc.math.vector.Vector2i;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class LotData {
    private final Lot lot;

    private final Vector2i minTilePosition;
    private final Vector2i maxTilePosition;

    private final Vector2i dimensions;

    private final BuildingData building;

    private final List<OccupantGroupType> occupants;

    public LotData(Lot lot, BuildingData building, ExemplarFile exemplar) {
        this.lot = lot;
        this.building = building;

        this.minTilePosition = Vector2i.from(this.lot.getMinTileX(), this.lot.getMinTileZ());
        this.maxTilePosition = Vector2i.from(this.lot.getMaxTileX(), this.lot.getMaxTileZ());
        this.dimensions = Vector2i.from(this.lot.getSizeX(), this.lot.getSizeZ());

        if (building != null) {
            ExemplarSubfile subfile = exemplar.getExemplarFiles().get(building.getBuilding().getResourceKey());
            if (subfile == null) {
                throw new IllegalArgumentException("Building with key " + building.getBuilding().getResourceKey() + " not found in SimCity exemplar!");
            }

            OccupantGroupTypesProperty occupants = subfile.getProperty(ExemplarPropertyTypes.OCCUPANT_GROUPS);
            if (occupants != null) {
                this.occupants = new ArrayList<>(occupants.getOccupants());
            } else {
                this.occupants = List.of();
            }
        } else {
            this.occupants = List.of();
        }
    }

    public Vector2i getMinTilePosition() {
        return this.minTilePosition;
    }

    public Vector2i getMaxTilePosition() {
        return this.maxTilePosition;
    }

    public Vector2i getDimensions() {
        return this.dimensions;
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
