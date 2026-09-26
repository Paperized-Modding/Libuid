package dev.tako.libuid.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FluidTagRegistryTest {
    private static final ResourceKey WATER_TAG = ResourceKey.of("c:water");
    private static final ResourceKey WATER = ResourceKey.of("minecraft:water");
    private static final ResourceKey RAIN = ResourceKey.of("mytech:rainwater");

    @AfterEach
    void clear() { FluidTagRegistry.clearForTests(); }

    @Test
    void registersMembersAndReverseIndex() {
        FluidTagRegistry.register(WATER_TAG, List.of(WATER, RAIN));

        assertEquals(Set.of(WATER, RAIN), FluidTagRegistry.members(WATER_TAG));
        assertTrue(FluidTagRegistry.contains(WATER_TAG, WATER));
        assertFalse(FluidTagRegistry.contains(WATER_TAG, ResourceKey.of("minecraft:lava")));
        assertEquals(Set.of(WATER_TAG), FluidTagRegistry.tagsOf(RAIN));
    }

    @Test
    void registerMergesIntoExistingTag() {
        FluidTagRegistry.register(WATER_TAG, List.of(WATER));
        FluidTagRegistry.register(WATER_TAG, List.of(RAIN));

        assertEquals(Set.of(WATER, RAIN), FluidTagRegistry.members(WATER_TAG));
    }

    @Test
    void replaceConfiguredSwapsBatchAtomically() {
        FluidTagRegistry.register(WATER_TAG, List.of(WATER));
        FluidTagRegistry.replaceConfigured(Map.of(ResourceKey.of("c:milk"), Set.of(ResourceKey.of("minecraft:milk"))));

        assertTrue(FluidTagRegistry.contains(ResourceKey.of("c:milk"), ResourceKey.of("minecraft:milk")));
        assertTrue(FluidTagRegistry.contains(WATER_TAG, WATER), "code tags survive a config reload");

        FluidTagRegistry.replaceConfigured(Map.of());
        assertTrue(FluidTagRegistry.members(ResourceKey.of("c:milk")).isEmpty(), "config batch fully replaced");
        assertTrue(FluidTagRegistry.contains(WATER_TAG, WATER));
    }

    @Test
    void unknownTagIsEmptyNotNull() {
        assertTrue(FluidTagRegistry.members(ResourceKey.of("c:unknown")).isEmpty());
        assertTrue(FluidTagRegistry.tagsOf(ResourceKey.of("mytech:unknown")).isEmpty());
        assertFalse(FluidTagRegistry.contains(ResourceKey.of("c:unknown"), WATER));
    }

    @Test
    void stackTagLookupUsesRegistry() {
        FluidTagRegistry.register(WATER_TAG, List.of(WATER));
        FluidType water = FluidType.builder(WATER).build();

        assertTrue(FluidStack.of(water, 1_000).is(WATER_TAG));
        assertFalse(FluidStack.of(water, 1_000).is(ResourceKey.of("c:lava")));
        assertFalse(FluidStack.EMPTY.is(WATER_TAG));
    }
}
