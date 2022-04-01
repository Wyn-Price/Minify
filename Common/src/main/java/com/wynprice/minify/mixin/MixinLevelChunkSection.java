package com.wynprice.minify.mixin;

import com.wynprice.minify.util.LevelChunkSectionAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelChunkSection.class)
public class MixinLevelChunkSection implements LevelChunkSectionAccessor {
    @Shadow
    private short nonEmptyBlockCount;
    @Shadow
    private short tickingBlockCount;
    @Shadow
    private short tickingFluidCount;

    @Mutable
    @Shadow
    private PalettedContainer<BlockState> states;


    @Override
    public void accessor_setStates(PalettedContainer<BlockState> states) {
        this.states = states;
    }

    @Override
    public void accessor_setNonEmptyBlockCount(short nonEmptyBlockCount) {
        this.nonEmptyBlockCount = nonEmptyBlockCount;
    }

    @Override
    public void accessor_setTickingBlockCount(short tickingBlockCount) {
        this.tickingBlockCount = tickingBlockCount;
    }

    @Override
    public void accessor_setTickingFluidCount(short tickingFluidCount) {
        this.tickingFluidCount = tickingFluidCount;
    }


}
