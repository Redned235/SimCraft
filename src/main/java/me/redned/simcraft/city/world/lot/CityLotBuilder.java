package me.redned.simcraft.city.world.lot;

import lombok.RequiredArgsConstructor;
import me.redned.simcraft.city.lot.LotData;
import me.redned.simcraft.city.world.CityRegion;
import me.redned.simcraft.city.world.lot.wall.RetainingWallType;
import me.redned.simreader.sc4.type.lot.LotZoneWealth;
import me.redned.simreader.sc4.type.occupant.OccupantGroupType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@RequiredArgsConstructor
public class CityLotBuilder {
    private static final int DEPTH = 16;
    private static final int WIDTH = 16;

    private static final EnumSet<OccupantGroupType> CONCRETE_WALLS = EnumSet.of(OccupantGroupType.R$, OccupantGroupType.ID, OccupantGroupType.CS$);
    private static final EnumSet<OccupantGroupType> DARK_BRICK_WALLS = EnumSet.of(OccupantGroupType.R$$, OccupantGroupType.IM, OccupantGroupType.CO$$, OccupantGroupType.CS$$);
    private static final EnumSet<OccupantGroupType> BRICK_WALLS = EnumSet.of(OccupantGroupType.R$$$, OccupantGroupType.IHT, OccupantGroupType.CO$$$, OccupantGroupType.CS$$$);

    private final CityRegion region;

    public void buildLots() {
        this.region.getLots().values().forEach(this::buildLot);
    }

    public void buildLot(LotData lot) {
        RetainingWallType retainingWall = getRetainingWallType(lot);
        for (int chunkX = lot.getMinTilePosition().getX(); chunkX < lot.getMinTilePosition().getX() + lot.getDimensions().getX(); chunkX++) {
            for (int chunkZ = lot.getMinTilePosition().getY(); chunkZ < lot.getMinTilePosition().getY() + lot.getDimensions().getY(); chunkZ++) {
                if (chunkX < 0 || chunkZ < 0) {
                    continue;
                }

                if (this.region.getNetworkBuilder().hasNetwork(chunkX, chunkZ)) {
                    continue;
                }

                for (int x = 0; x < WIDTH; x++) {
                    for (int z = 0; z < WIDTH; z++) {
                        int xPos = (chunkX << 4) + x;
                        int zPos = (chunkZ << 4) + z;
                        int y = this.region.getTerrainGenerator().getHeight(xPos, zPos);
                        for (int depth = 1; depth < DEPTH; depth++) {
                            this.region.setBlockState(xPos, y - depth, zPos, retainingWall.getState());
                        }
                    }
                }
            }
        }
    }

    private static RetainingWallType getRetainingWallType(LotData lot) {
        List<OccupantGroupType> occupants = lot.getOccupants();
        if (!occupants.isEmpty()) {
            // Check specific occupants first
            if (occupants.contains(OccupantGroupType.IR)) {
                return RetainingWallType.DIRT;
            }

            if (!Collections.disjoint(occupants, CONCRETE_WALLS)) {
                return RetainingWallType.CONCRETE;
            }

            if (!Collections.disjoint(occupants, DARK_BRICK_WALLS)) {
                return RetainingWallType.DARK_BRICKS;
            }

            if (!Collections.disjoint(occupants, BRICK_WALLS)) {
                return RetainingWallType.BRICKS;
            }

            // No common occupants - fallback to wealth
            if (lot.getZoneWealth() == LotZoneWealth.$) {
                return RetainingWallType.CONCRETE;
            }

            if (lot.getZoneWealth() == LotZoneWealth.$$) {
                return RetainingWallType.DARK_BRICKS;
            }

            if (lot.getZoneWealth() == LotZoneWealth.$$$) {
                return RetainingWallType.BRICKS;
            }
        }

        return RetainingWallType.STONE;
    }
}
