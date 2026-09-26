package dev.tako.libuid.builtin;

import dev.tako.libuid.api.FluidTagRegistry;
import dev.tako.libuid.api.ResourceKey;

import java.util.List;

                                                                              
public final class BuiltinFluidTags {
    private BuiltinFluidTags() {}

    public static void register() {
        FluidTagRegistry.register(ResourceKey.of("c:water"), List.of(ResourceKey.of("minecraft:water")));
        FluidTagRegistry.register(ResourceKey.of("c:milk"), List.of(ResourceKey.of("minecraft:milk")));
        FluidTagRegistry.register(ResourceKey.of("c:honey"), List.of(ResourceKey.of("minecraft:honey")));
        FluidTagRegistry.register(ResourceKey.of("c:lava"), List.of(ResourceKey.of("minecraft:lava")));
    }
}
