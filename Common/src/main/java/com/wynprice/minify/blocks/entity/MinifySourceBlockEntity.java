package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.client.BaseMinifyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MinifySourceBlockEntity extends BaseMinifyBlockEntity {

    private static final int MOVEMENT_PADDING = 2;
    //The area that the `offset` can move too. Offset is the minimum xyz of the area.
    //The movement padding is the amount of blocks away from the source block the area can be
    public static final AABB AREA = new AABB(
        -8-MOVEMENT_PADDING,-8-MOVEMENT_PADDING,-8-MOVEMENT_PADDING,
        MOVEMENT_PADDING, MOVEMENT_PADDING, MOVEMENT_PADDING
    );


    private BlockPos offset = new BlockPos(0, 1, 0);

    public MinifySourceBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFY_SOURCE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("offset_x", this.offset.getX());
        tag.putInt("offset_y", this.offset.getY());
        tag.putInt("offset_z", this.offset.getZ());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        this.offset = new BlockPos(tag.getInt("offset_x"), tag.getInt("offset_y"), tag.getInt("offset_z"));
        super.load(tag);
    }

    public BlockPos getOffset() {
        return offset;
    }


}
