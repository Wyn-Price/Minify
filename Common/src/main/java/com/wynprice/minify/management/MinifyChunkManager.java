package com.wynprice.minify.management;

import com.wynprice.minify.blocks.entity.MinifyBlockEntity;
import com.wynprice.minify.generation.DimensionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class MinifyChunkManager {
    private final Map<MinifyLocationKey, MinifyBlockEntity> locationMap = new HashMap<>();

    public void setLocation(MinifyLocationKey key, MinifyBlockEntity value) {
        this.locationMap.put(key, value);
    }

    public void onUnload(MinifyLocationKey key) {
        this.locationMap.remove(key);
    }

    public void onBlockChangedAt(Level level, BlockPos pos, BlockState state) {
        if(!level.isClientSide) {
            ServerLevel dimension = level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
            this.locationMap.forEach((key, blockEntity) -> {
                if(blockEntity.getArea().contains(pos.getX(), pos.getY(), pos.getZ())) {
                    BlockPos offset = pos.subtract(blockEntity.getOffset());
                    BlockPos blockAt = key.chunk().getBlockAt(offset.getX(), key.yChunk() * 16 + offset.getY(), offset.getZ());
                    dimension.setBlock(blockAt.offset(1, 1, 1), state, 19);
                }
            });
        }
    }

    //https://stackoverflow.com/a/3706260
    //Spiral search to find next chunk
    public MinifyLocationKey findNextLocation() {
        // (dx, dz) is a vector - direction in which we move right now
        int dx = 0;
        int dz = 1;
        // length of current segment
        int segment_length = 1;

        // current position (x, z) and how much of current segment we passed
        int x = 0;
        int z = 0;
        int segment_passed = 0;

        do {
            for(int i = 0; i < 16; i++) {
                MinifyLocationKey testKey = new MinifyLocationKey(new ChunkPos(x, z), i);
                if(!this.locationMap.containsKey(testKey)) {
                    return testKey;
                }
            }

            // make a step, add 'direction' vector (dx, dz) to current position (x, z)
            x += dx;
            z += dz;

            if (++segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = dz;
                dz = -dx;
                dx = buffer;

                // increase segment length if necessary
                if (dx == 0) {
                    ++segment_length;
                }
            }
        } while (true);
    }
}
