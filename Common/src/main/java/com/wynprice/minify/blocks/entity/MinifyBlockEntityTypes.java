package com.wynprice.minify.blocks.entity;

import com.mojang.datafixers.types.Type;
import com.wynprice.minify.blocks.MinificationBlock;
import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.platform.Services;
import com.wynprice.minify.util.Registered;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MinifyBlockEntityTypes {

    private static final List<Registered<BlockEntityType<?>>> TYPES = new ArrayList<>();

    public static final BlockEntityType<MinifyBlockEntity> MINIFICATION_BLOCK_ENTITY = create("minification_block_entity", Services.PLATFORM.createBlockEntity(MinifyBlockEntity::new, MinifyBlocks.MINIFICATION_BLOCK));

    private static <T extends BlockEntity> BlockEntityType<T> create(String name, Function<Type<?>, BlockEntityType<T>> creator) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, name);
        BlockEntityType<T> object = creator.apply(type);
        TYPES.add(new Registered<>(object, name));
        return object;
    }

    public static List<Registered<BlockEntityType<?>>> getTypes() {
        return TYPES;
    }
}
