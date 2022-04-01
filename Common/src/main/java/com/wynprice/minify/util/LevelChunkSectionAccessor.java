package com.wynprice.minify.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface LevelChunkSectionAccessor {
    void accessor_setStates(PalettedContainer<BlockState> states);
    void accessor_setNonEmptyBlockCount(short nonEmptyBlockCount);
    void accessor_setTickingBlockCount(short tickingBlockCount);
    void accessor_setTickingFluidCount(short tickingFluidCount);
}
