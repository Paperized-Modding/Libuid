package dev.tako.libuid.builtin;

import dev.tako.libuid.api.FluidRegistry;
import dev.tako.libuid.api.FluidType;
import dev.tako.libuid.api.ResourceKey;

import java.util.List;

                                                                                            
public final class BuiltinFluidTypes {
    private static final List<FluidType> TYPES = List.of(
            FluidType.builder(ResourceKey.of("minecraft:water"))
                    .displayName("<lang:block.minecraft.water>")
                    .color(0x3F76E4)
                    .build(),
            FluidType.builder(ResourceKey.of("minecraft:lava"))
                    .displayName("<lang:block.minecraft.lava>")
                    .color(0xFFFFFF)
                    .texture("lava")
                    .density(3_000)
                    .viscosity(6_000)
                    .temperature(1_300)
                    .lightLevel(15)
                    .build(),
            FluidType.builder(ResourceKey.of("minecraft:milk"))
                    .displayName("<lang:item.minecraft.milk_bucket>")
                    .color(0xFFFFFF)
                    .texture("milk")
                    .density(1_024)
                    .viscosity(1_024)
                    .build());

    private BuiltinFluidTypes() {}

    public static List<FluidType> all() { return TYPES; }

    public static void register() {
        for (FluidType type : TYPES) FluidRegistry.register(type);
    }
}
