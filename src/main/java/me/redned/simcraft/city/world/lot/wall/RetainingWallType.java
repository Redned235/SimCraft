package me.redned.simcraft.city.world.lot.wall;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.redned.levelparser.BlockState;

@Getter
@RequiredArgsConstructor
public enum RetainingWallType {
    DIRT(BlockState.of("minecraft:dirt")),
    STONE(BlockState.of("minecraft:dripstone_block")),
    BRICKS(BlockState.of("minecraft:bricks")),
    DARK_BRICKS(BlockState.of("minecraft:cobbled_deepslate")),
    CONCRETE(BlockState.of("minecraft:light_gray_concrete"));

    private final BlockState state;
}
