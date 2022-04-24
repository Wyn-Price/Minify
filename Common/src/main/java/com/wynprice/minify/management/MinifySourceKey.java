package com.wynprice.minify.management;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public record MinifySourceKey(ResourceLocation dimension, BlockPos pos) {

    public static CompoundTag toNBT(MinifySourceKey key, CompoundTag tag) {
        tag.putString("dimension", key.dimension().toString());
        tag.put("position", NbtUtils.writeBlockPos(key.pos));
        return tag;
    }

    public static MinifySourceKey fromNBT(CompoundTag tag) {
        return new MinifySourceKey(
            new ResourceLocation(tag.getString("dimension")),
            NbtUtils.readBlockPos(tag.getCompound("position"))
        );
    }

    public static boolean hasKey(CompoundTag tag) {
        return tag.contains("dimension", 8) && tag.contains("position", 10);
    }
}
