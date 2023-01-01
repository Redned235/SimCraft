package me.redned.simcraft.city.placeable;

import lombok.Getter;
import lombok.ToString;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.exemplar.property.ExemplarPropertyTypes;
import me.redned.simreader.sc4.storage.exemplar.property.type.StringProperty;
import me.redned.simreader.sc4.storage.exemplar.type.ExemplarSubfile;
import me.redned.simreader.sc4.type.Building;
import org.cloudburstmc.math.vector.Vector3f;

@ToString
@Getter
public class BuildingData implements PlaceableData {
    private final Building building;
    private final String identifier;

    private final Vector3f minPosition;
    private final Vector3f maxPosition;

    public BuildingData(Building building, ExemplarFile exemplar) {
        this.building = building;

        ExemplarSubfile subfile = exemplar.getExemplarFiles().get(building.getResourceKey());
        if (subfile == null) {
            throw new IllegalArgumentException("Building with key " + building.getResourceKey() + " not found in SimCity exemplar!");
        }

        StringProperty.Single property = subfile.getProperty(ExemplarPropertyTypes.EXEMPLAR_NAME);
        if (property == null) {
            throw new IllegalArgumentException("No name property existed for building " + building + " in exemplar properties!");
        }

        this.identifier = property.getValue();

        this.minPosition = Vector3f.from(
                this.building.getMinCoordinateX(),
                this.building.getMinCoordinateY(),
                this.building.getMinCoordinateZ()
        );

        this.maxPosition = Vector3f.from(
                this.building.getMaxCoordinateX(),
                this.building.getMaxCoordinateY(),
                this.building.getMaxCoordinateZ()
        );
    }

    @Override
    public int getRotation() {
        return switch (this.building.getOrientation()) {
            case 1 -> 90;
            case 2 -> 0;
            case 3 -> 270;
            case 4 -> 180;
            default -> 0;
        };
    }

    @Override
    public byte getOrientation() {
        return this.building.getOrientation();
    }

    @Override
    public boolean isVisible() {
        return (this.building.getAppearanceFlag() & 0x01) != 0;
    }

    @Override
    public boolean shouldDisplay() {
        return this.isVisible();
    }
}
