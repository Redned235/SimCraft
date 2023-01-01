package me.redned.simcraft.city.placeable;

import lombok.Getter;
import lombok.ToString;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.exemplar.property.ExemplarPropertyTypes;
import me.redned.simreader.sc4.storage.exemplar.property.type.StringProperty;
import me.redned.simreader.sc4.storage.exemplar.type.ExemplarSubfile;
import me.redned.simreader.sc4.type.Flora;
import org.cloudburstmc.math.vector.Vector3f;

@ToString
@Getter
public class FloraData implements PlaceableData {
    private final Flora flora;
    private final String identifier;

    public FloraData(Flora flora, ExemplarFile exemplar) {
        this.flora = flora;

        ExemplarSubfile subfile = exemplar.getExemplarFiles().get(flora.getResourceKey());
        if (subfile == null) {
            throw new IllegalArgumentException("Flora with key " + flora.getResourceKey() + " not found in SimCity exemplar!");
        }

        StringProperty.Single property = subfile.getProperty(ExemplarPropertyTypes.EXEMPLAR_NAME);
        if (property == null) {
            throw new IllegalArgumentException("No name property existed for flora " + flora + " in exemplar properties!");
        }

        this.identifier = property.getValue();
    }

    public Vector3f getPosition() {
        return Vector3f.from(
                this.flora.getCoordinateX(),
                this.flora.getCoordinateY(),
                this.flora.getCoordinateZ()
        );
    }

    @Override
    public Vector3f getMinPosition() {
        return this.getPosition();
    }

    @Override
    public Vector3f getMaxPosition() {
        return this.getPosition();
    }

    @Override
    public int getRotation() {
        return switch (this.flora.getOrientation()) {
            case 1 -> 90;
            case 2 -> 0;
            case 3 -> 270;
            case 4 -> 180;
            default -> 0;
        };
    }

    @Override
    public byte getOrientation() {
        return this.flora.getOrientation();
    }

    @Override
    public boolean isVisible() {
        return (this.flora.getAppearanceFlag() & 0x01) != 0;
    }

    @Override
    public boolean shouldDisplay() {
        return this.isVisible();
    }

    public boolean isBurnt() {
        return (this.flora.getAppearanceFlag() & 0x40) != 0;
    }
}
