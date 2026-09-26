package dev.tako.libuid.config;

import dev.tako.libuid.api.FluidSoundAction;
import dev.tako.libuid.api.FluidType;
import dev.tako.libuid.api.ResourceKey;
import dev.tako.libuid.builtin.BuiltinFluidTypes;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LibuidFluidParserTest {
    @Test
    void nameOnlyConfigurationInheritsBuiltinMilkProperties() {
        FluidType milk = BuiltinFluidTypes.all().stream()
                .filter(type -> type.key().equals(ResourceKey.of("minecraft:milk")))
                .findFirst().orElseThrow();

        FluidType overridden = LibuidFluidParser.decode(milk.key(), ConfigSection.of("minecraft:milk", Map.of(
                "name", "<white>Custom milk")), milk);

        assertEquals("<white>Custom milk", overridden.displayName().orElseThrow());
        assertEquals(0xFFFFFF, overridden.color().orElseThrow());
        assertEquals("milk", overridden.texture().orElseThrow());
        assertEquals(1024, overridden.density());
        assertEquals(1024, overridden.viscosity());
    }

    @Test
    void soundAndBucketParseAndInheritBaseline() {
        FluidType milk = BuiltinFluidTypes.all().stream()
                .filter(type -> type.key().equals(ResourceKey.of("minecraft:milk")))
                .findFirst().orElseThrow();

        FluidType parsed = LibuidFluidParser.decode(milk.key(), ConfigSection.of("minecraft:milk", Map.of(
                "bucket", "minecraft:milk_bucket",
                "sounds", Map.of("bucket_fill", "minecraft:item.bucket.fill"))), milk);

        assertEquals("minecraft:milk_bucket", parsed.bucketItem().orElseThrow());
        assertEquals("minecraft:item.bucket.fill", parsed.sound(FluidSoundAction.BUCKET_FILL).orElseThrow());
        assertEquals("milk", parsed.texture().orElseThrow(), "unrelated baseline fields still inherit");
    }

    @Test
    void unknownSoundActionIsRejected() {
        FluidType milk = BuiltinFluidTypes.all().stream()
                .filter(type -> type.key().equals(ResourceKey.of("minecraft:milk")))
                .findFirst().orElseThrow();

        assertThrows(IllegalArgumentException.class, () -> LibuidFluidParser.decode(milk.key(), ConfigSection.of("minecraft:milk", Map.of(
                "sounds", Map.of("slurp", "minecraft:item.bucket.fill"))), milk));
    }
}
