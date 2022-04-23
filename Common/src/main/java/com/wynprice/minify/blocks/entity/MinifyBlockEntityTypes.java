package com.wynprice.minify.blocks.entity;

import com.mojang.datafixers.types.Type;
import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.platform.Services;
import com.wynprice.minify.util.Registered;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MinifyBlockEntityTypes {

    private static final List<Registered<BlockEntityType<?>>> TYPES = new ArrayList<>();

    public static final BlockEntityType<MinifyViewerBlockEntity> MINIFY_VIEWER_BLOCK_ENTITY = create("viewer", Services.PLATFORM.createBlockEntity(MinifyViewerBlockEntity::new, MinifyBlocks.MINIFY_VIEWER));
    public static final BlockEntityType<MinifySourceBlockEntity> MINIFY_SOURCE_BLOCK_ENTITY = create("source", Services.PLATFORM.createBlockEntity(MinifySourceBlockEntity::new, MinifyBlocks.MINIFY_SOURCE));

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
