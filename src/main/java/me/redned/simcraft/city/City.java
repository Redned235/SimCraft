package me.redned.simcraft.city;

import lombok.Getter;
import me.redned.simcraft.city.building.BuildingData;
import me.redned.simcraft.city.flora.FloraData;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.prop.PropData;
import me.redned.simreader.sc4.storage.SC4File;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.type.RegionViewSubfile;
import me.redned.simreader.sc4.type.Building;
import me.redned.simreader.sc4.type.Flora;
import me.redned.simreader.sc4.type.Lot;
import me.redned.simreader.sc4.type.Prop;
import org.cloudburstmc.math.vector.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a city within SimCity.
 * <p>
 * This class primarily holds city data from
 * the SimCity save file and handles mapping
 * much of it to a Minecraft save.
 */
@Getter
public class City {
    private final SC4File saveFile;
    private final ExemplarFile exemplarFile;
    private final RegionViewSubfile regionView;

    private final List<BuildingData> buildings = new ArrayList<>();
    private final List<PropData> props = new ArrayList<>();
    private final List<FloraData> flora = new ArrayList<>();
    private final List<LotData> lots = new ArrayList<>();

    public City(SC4File saveFile, ExemplarFile exemplarFile) {
        this.saveFile = saveFile;
        this.exemplarFile = exemplarFile;

        this.regionView = saveFile.getRegionViewFile();

        if (saveFile.getBuildingFile() != null) {
            for (Building building : saveFile.getBuildingFile().getBuildings()) {
                this.buildings.add(new BuildingData(building, exemplarFile));
            }
        }

        if (saveFile.getPropFile() != null) {
            for (Prop prop : saveFile.getPropFile().getProps()) {
                this.props.add(new PropData(prop, exemplarFile));
            }
        }

        if (saveFile.getFloraFile() != null) {
            for (Flora flora : saveFile.getFloraFile().getFlora()) {
                this.flora.add(new FloraData(flora, exemplarFile));
            }
        }

        if (saveFile.getLotFile() != null) {
            for (Lot lot : saveFile.getLotFile().getLots()) {
                this.lots.add(new LotData(lot));
            }
        }
    }

    public String getName() {
        return this.regionView.getCityName();
    }

    public Vector2i getDimensions() {
        return Vector2i.from(this.regionView.getCitySizeX(), this.regionView.getCitySizeY());
    }

    public Vector2i getTilePosition() {
        return Vector2i.from(this.regionView.getTileXLocation(), this.regionView.getTileYLocation());
    }

    public float[][] getHeightMap() {
        return this.saveFile.getTerrainFile().getHeightMap();
    }
}
