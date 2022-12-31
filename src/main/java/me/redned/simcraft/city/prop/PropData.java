package me.redned.simcraft.city.prop;

import lombok.Getter;
import lombok.ToString;
import me.redned.simcraft.city.PlaceableData;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.exemplar.property.ExemplarPropertyTypes;
import me.redned.simreader.sc4.storage.exemplar.property.type.StringProperty;
import me.redned.simreader.sc4.storage.exemplar.type.ExemplarSubfile;
import me.redned.simreader.sc4.type.Building;
import me.redned.simreader.sc4.type.Prop;
import org.cloudburstmc.math.vector.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

@ToString
@Getter
public class PropData implements PlaceableData {
    private final Prop prop;
    private final String identifier;

    public PropData(Prop prop, ExemplarFile exemplar) {
        this.prop = prop;

        ExemplarSubfile subfile = exemplar.getExemplarFiles().get(prop.getResourceKey());
        if (subfile == null) {
            throw new IllegalArgumentException("Prop with key " + prop.getResourceKey() + " not found in SimCity exemplar!");
        }

        StringProperty.Single property = subfile.getProperty(ExemplarPropertyTypes.EXEMPLAR_NAME);
        if (property == null) {
            throw new IllegalArgumentException("No name property existed for prop " + prop + " in exemplar properties!");
        }

        this.identifier = property.getValue();
    }

    @Override
    public Vector3f getMinPosition() {
        return Vector3f.from(
                this.prop.getMinCoordinateX(),
                this.prop.getMinCoordinateY(),
                this.prop.getMinCoordinateZ()
        );
    }

    @Override
    public Vector3f getMaxPosition() {
        return Vector3f.from(
                this.prop.getMaxCoordinateX(),
                this.prop.getMaxCoordinateY(),
                this.prop.getMaxCoordinateZ()
        );
    }

    @Override
    public int getRotation() {
        return switch (this.prop.getOrientation()) {
            case 1 -> 90;
            case 2 -> 0;
            case 3 -> 270;
            case 4 -> 180;
            default -> 0;
        };
    }

    @Override
    public byte getOrientation() {
        return this.prop.getOrientation();
    }

    @Override
    public boolean isVisible() {
        return (this.prop.getAppearanceFlag() & 0x01) != 0;
    }

    @Override
    public boolean shouldDisplay() {
        int chance = this.prop.getAppearanceChance();
        int num = ThreadLocalRandom.current().nextInt(0, 100);
        return this.isVisible() && num <= chance;
    }

    public boolean isBurnt() {
        return (this.prop.getAppearanceFlag() & 0x40) != 0;
    }
}
