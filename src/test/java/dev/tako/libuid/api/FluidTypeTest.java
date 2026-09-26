package dev.tako.libuid.api;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FluidTypeTest {
    private static final ResourceKey WATER = ResourceKey.of("minecraft:water");

    @Test
    void soundAndBucketAreOptional() {
        FluidType plain = FluidType.builder(WATER).build();

        assertEquals(Optional.empty(), plain.sound(FluidSoundAction.BUCKET_FILL));
        assertEquals(Optional.empty(), plain.bucketItem());
    }

    @Test
    void soundAndBucketRoundTrip() {
        FluidType full = FluidType.builder(WATER)
                .sound(FluidSoundAction.BUCKET_FILL, "minecraft:item.bucket.fill")
                .bucketItem("minecraft:water_bucket")
                .build();

        assertEquals(Optional.of("minecraft:item.bucket.fill"), full.sound(FluidSoundAction.BUCKET_FILL));
        assertEquals(Optional.empty(), full.sound(FluidSoundAction.VAPORIZE));
        assertEquals(Optional.of("minecraft:water_bucket"), full.bucketItem());
    }
}
