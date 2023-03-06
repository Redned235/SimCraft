package me.redned.simcraft.city;

import lombok.Getter;
import me.redned.simcraft.city.placeable.BuildingData;
import me.redned.simcraft.city.placeable.FloraData;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.placeable.PropData;
import me.redned.simcraft.city.network.NetworkTile1Data;
import me.redned.simcraft.city.network.NetworkTile2Data;
import me.redned.simreader.sc4.storage.SC4File;
import me.redned.simreader.sc4.storage.exemplar.ExemplarFile;
import me.redned.simreader.sc4.storage.type.RegionViewSubfile;
import me.redned.simreader.sc4.type.Building;
import me.redned.simreader.sc4.type.Flora;
import me.redned.simreader.sc4.type.Lot;
import me.redned.simreader.sc4.type.NetworkTile1;
import me.redned.simreader.sc4.type.NetworkTile2;
import me.redned.simreader.sc4.type.Prop;
import org.cloudburstmc.math.vector.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<NetworkTile1Data> networkTile1s = new ArrayList<>();
    private final List<NetworkTile2Data> networkTile2s = new ArrayList<>();

    private final Vector2i dimensions;
    private final Vector2i tilePosition;

    public City(SC4File saveFile, ExemplarFile exemplarFile) {
        this.saveFile = saveFile;
        this.exemplarFile = exemplarFile;

        this.regionView = saveFile.getRegionViewFile();

        Map<Integer, BuildingData> buildingsByIId = new HashMap<>();
        if (saveFile.getBuildingFile() != null) {
            for (Building building : saveFile.getBuildingFile().getBuildings()) {
                BuildingData buildingData = new BuildingData(building, exemplarFile);
                this.buildings.add(buildingData);

                buildingsByIId.put(building.getInstanceId(), buildingData);
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
                this.lots.add(new LotData(lot, buildingsByIId.get(lot.getBuildingInstanceId()), exemplarFile));
            }
        }

        if (saveFile.getNetworkTile1File() != null) {
            for (NetworkTile1 tile : saveFile.getNetworkTile1File().getNetworkTiles()) {
                if (tile.getMinCoordinateX() < 0 || tile.getMinCoordinateY() < 0 || tile.getMinCoordinateZ() < 0) {
                    System.err.println("Network tile at " + tile.getMinCoordinateX() + " " + tile.getMinCoordinateY() + " " + tile.getMinCoordinateZ() + " was out of the world!");
                    continue;
                }

                this.networkTile1s.add(new NetworkTile1Data(tile));
            }
        }

        if (saveFile.getNetworkTile2File() != null) {
            for (NetworkTile2 tile : saveFile.getNetworkTile2File().getNetworkTiles()) {
                if (tile.getMinCoordinateX() < 0 || tile.getMinCoordinateY() < 0 || tile.getMinCoordinateZ() < 0) {
                    System.err.println("Network tile (2) at " + tile.getMinCoordinateX() + " " + tile.getMinCoordinateY() + " " + tile.getMinCoordinateZ() + " was out of the world!");
                    continue;
                }

                this.networkTile2s.add(new NetworkTile2Data(tile));
            }
        }

        this.dimensions = Vector2i.from(this.regionView.getCitySizeX(), this.regionView.getCitySizeY());
        this.tilePosition = Vector2i.from(this.regionView.getTileXLocation(), this.regionView.getTileYLocation());
    }

    public String getName() {
        return this.regionView.getCityName();
    }

    public float[][] getHeightMap() {
        return this.saveFile.getTerrainFile().getHeightMap();
    }
}
