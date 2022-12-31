package me.redned.simcraft.schematic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.redned.levelparser.BlockState;
import me.redned.levelparser.Chunk;
import me.redned.levelparser.Level;
import me.redned.simcraft.util.BlockRotationUtil;
import me.redned.simcraft.util.MathUtil;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.util.VarInts;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

@Getter
@AllArgsConstructor
public class Schematic {
    private final int verison;
    private final int dataVersion;
    private final int width;
    private final int height;
    private final int length;
    private final Map<Vector3i, BlockState> blocks;
    private final Map<Vector3i, NbtMap> blockEntities;
    private final NbtMap metadata;

    public static Schematic parse(NbtMap nbt) {
        Map<Integer, BlockState> palette = new HashMap<>();
        for (Map.Entry<String, Object> entry : nbt.getCompound("Palette").entrySet()) {
            int value = (int) entry.getValue();
            String identifier = entry.getKey();

            // Parse states
            String[] split = identifier.split("\\[");
            if (split.length == 1) {
                palette.put(value, BlockState.of(identifier));
                continue;
            }

            String cleanIdentifier = split[0];
            String states = split[1].replace("]", "");

            Map<String, Object> properties = new HashMap<>();
            for (String state : states.split(",")) {
                String[] property = state.split("=");
                properties.put(property[0], property[1]);
            }

            palette.put(value, BlockState.of(cleanIdentifier, properties));
        }

        int width = nbt.getShort("Width") & 0xFFFF;
        int height = nbt.getShort("Height") & 0xFFFF;
        int length = nbt.getShort("Length") & 0xFFFF;

        byte[] blockData = nbt.getByteArray("BlockData");

        Map<Vector3i, BlockState> blocks = new HashMap<>();

        int index = 0;
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(blockData))) {
            while (input.available() > 0) {
                int nextId = VarInts.readUnsignedInt(input);
                BlockState state = palette.get(nextId);

                int y = index / (width * length);
                int remainder = index - (y * width * length);
                int z = remainder / width;
                int x = remainder - z * width;

                blocks.put(Vector3i.from(x, y, z), state);
                index++;
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Map<Vector3i, NbtMap> blockEntities = new HashMap<>();
        for (NbtMap blockEntity : nbt.getList("BlockEntities", NbtType.COMPOUND)) {
            int[] pos = blockEntity.getIntArray("Pos");
            Vector3i position = Vector3i.from(pos[0], pos[1], pos[2]);

            NbtMapBuilder builder = NbtMap.builder();
            for (Map.Entry<String, Object> entry : blockEntity.entrySet()) {
                if (entry.getKey().equals("Pos")) {
                    continue;
                }

                if (entry.getKey().equals("Id")) {
                    builder.put("id", entry.getValue());
                    continue;
                }

                builder.put(entry.getKey(), entry.getValue());
            }

            blockEntities.put(position, builder.build());
        }

        return new Schematic(
                nbt.getInt("Version"),
                nbt.getInt("DataVersion"),
                width,
                height,
                length,
                blocks,
                blockEntities,
                nbt.getCompound("Metadata")
        );
    }

    public Vector3i getMinPosition() {
        return Vector3i.ZERO;
    }

    public Vector3i getMaxPosition() {
        return Vector3i.from(this.width, this.height, this.length).sub(Vector3i.ONE);
    }

    public Vector3d getCenterPosition() {
        return Vector3d.from(this.width / 2.0D, this.height / 2.0D, this.length / 2.0D);
    }

    public void paste(Level level, Vector3i position, boolean pasteAir) {
        this.paste(level, position, 0, pasteAir);
    }

    public void paste(Level level, Vector3i position, int rotation, boolean pasteAir) {
        this.paste(level, position, rotation, null, pasteAir);
    }

    public void paste(Level level, Vector3i position, int rotation, BinaryOperator<Vector3i> positionOperator, boolean pasteAir) {
        if (rotation % 90 != 0) {
            throw new IllegalArgumentException("Angle used for rotation was not divisible by 90!");
        }

        pasteAir |= this.getMetadata().getBoolean("SCPasteAir", false);

        Vector3d centerPos = this.getCenterPosition();
        for (Map.Entry<Vector3i, BlockState> entry : this.blocks.entrySet()) {
            Vector3i schemPos = position.add(MathUtil.rotateAroundYAxis(entry.getKey(), Vector3d.from(centerPos.getX(), entry.getKey().getY(), centerPos.getZ()), rotation));
            if (positionOperator != null) {
                schemPos = positionOperator.apply(entry.getKey(), schemPos);
            }

            if (entry.getValue() == null || (!pasteAir && BlockState.AIR.equals(entry.getValue()))) {
                continue;
            }

            level.setBlockState(schemPos.getX(), schemPos.getY(), schemPos.getZ(), BlockRotationUtil.rotate(entry.getValue(), rotation));
        }

        for (Map.Entry<Vector3i, NbtMap> entry : this.blockEntities.entrySet()) {
            Vector3i schemPos = position.add(MathUtil.rotateAroundYAxis(entry.getKey(), Vector3d.from(centerPos.getX(), entry.getKey().getY(), centerPos.getZ()), rotation));
            if (positionOperator != null) {
                schemPos = positionOperator.apply(entry.getKey(), schemPos);
            }

            NbtMap blockEntityTag = entry.getValue().toBuilder()
                    .putInt("x", schemPos.getX())
                    .putInt("y", schemPos.getY())
                    .putInt("z", schemPos.getZ())
                    .build();

            Chunk chunk = level.getChunk(schemPos.getX() >> 4, schemPos.getZ() >> 4);
            chunk.getBlockEntities().add(blockEntityTag);
        }
    }
}
