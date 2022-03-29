package com.wynprice.minify.blocks;

import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WallRedstoneBlock extends BarrierBlock {

    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    protected WallRedstoneBlock(Properties properties) {
        super(properties);
    }

    //TODO: problem
    //Say we have a line of redstone on one side (line A),
    //then a line of redstone on the other side (line B)
    //
    //If we power line A, then line B will be powered. Perfect.
    //However, line B will then go back and power the wall blocks, meaning that
    //line A is now powered, as the wall blocks pass the signal back.
    //If we then unpower line A, the viewer will be still powering the line A,
    //which will power line B, ect. It will never be unpowered.
    //
    //The solution can't be to just only power non-powered walls, as
    //If you put redstone around a corner to another wall it'd cause the problem.
    //
    //This method is also costly, a lot of updates are caused.
    //A better way would be to just refresh all faces when a state changes.
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean idk) {
        ChunkPos chunkPos = new ChunkPos(fromPos);
        int chunkY = fromPos.getY() >> 4;

        int innerX = fromPos.getX() & 15;
        int innerY = fromPos.getY() & 15;
        int innerZ = fromPos.getZ() & 15;

        boolean isInRange = innerX >= 1 && innerY >= 1 && innerZ >= 1 && innerX <= 8 && innerY <= 8 && innerZ <= 8;

        if(isInRange && level instanceof ServerLevel serverLevel && level.dimension() == DimensionRegistry.WORLD_KEY) {
            BlockState fromState = level.getBlockState(fromPos);
            if(fromState.getBlock() != this) {
                for (Direction direction : Direction.values()) {
                    if(pos.relative(direction).equals(fromPos)) {
                        MinifyLocationKey key = new MinifyLocationKey(chunkPos, chunkY);
//                        MinifyChunkManager.getManager(serverLevel).updateViewerSignal(key, direction.getOpposite());
                        break;
                    }
                }
            }

        }
        super.neighborChanged(state, level, pos, fromBlock, fromPos, idk);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(LEVEL);
    }
}
