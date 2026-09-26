package dev.tako.libuid.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FluidIngredientTest {
    private static final FluidType WATER = FluidType.builder(ResourceKey.of("minecraft:water")).build();
    private static final FluidType OIL = FluidType.builder(ResourceKey.of("mytech:oil")).build();

    @AfterEach
    void clear() {
        FluidRegistry.clearForTests();
        FluidTagRegistry.clearForTests();
    }

    @Test
    void singleIngredientMatchesTypeOnly() {
        FluidRegistry.register(WATER);
        FluidIngredient water = FluidIngredient.single(ResourceKey.of("minecraft:water"));

        assertTrue(water.test(FluidStack.of(WATER, 1)));
        assertTrue(water.test(FluidStack.of(WATER, 1_000)));
        assertFalse(water.test(FluidStack.of(OIL, 1_000)));
        assertFalse(water.test(FluidStack.EMPTY));
        assertEquals(List.of(WATER.key()), water.candidates().stream().map(FluidType::key).toList());
    }

    @Test
    void tagIngredientDelegatesToTagRegistry() {
        FluidTagRegistry.register(ResourceKey.of("c:water"), List.of(ResourceKey.of("minecraft:water")));
        FluidIngredient waterTag = FluidIngredient.tag(ResourceKey.of("c:water"));

        assertTrue(waterTag.test(FluidStack.of(WATER, 1)));
        assertFalse(waterTag.test(FluidStack.of(OIL, 1)));
    }

    @Test
    void anyOfAndEmptyIngredientCompose() {
        FluidIngredient waterOrOil = FluidIngredient.anyOf(
                FluidIngredient.single(ResourceKey.of("minecraft:water")),
                FluidIngredient.single(ResourceKey.of("mytech:oil")));

        assertTrue(waterOrOil.test(FluidStack.of(WATER, 1)));
        assertTrue(waterOrOil.test(FluidStack.of(OIL, 1)));
        assertFalse(waterOrOil.test(FluidStack.EMPTY));
        assertFalse(FluidIngredient.empty().test(FluidStack.of(WATER, 1)));
    }

    @Test
    void sizedIngredientRequiresMinimumAmount() {
        SizedFluidIngredient sized = new SizedFluidIngredient(
                FluidIngredient.single(ResourceKey.of("minecraft:water")), 500);

        assertTrue(sized.test(FluidStack.of(WATER, 500)));
        assertTrue(sized.test(FluidStack.of(WATER, 1_000)), ">= not ==");
        assertFalse(sized.test(FluidStack.of(WATER, 499)));
        assertFalse(sized.test(FluidStack.of(OIL, 1_000)));
        assertEquals(500, sized.amount());
        assertThrows(IllegalArgumentException.class, () -> new SizedFluidIngredient(FluidIngredient.empty(), 0));
    }

    @Test
    void parseAcceptsIdTagMapAndList() {
        FluidRegistry.register(WATER);
        FluidTagRegistry.register(ResourceKey.of("c:water"), List.of(ResourceKey.of("minecraft:water")));

        FluidIngredient id = FluidIngredient.parse("minecraft:water");
        assertTrue(id.test(FluidStack.of(WATER, 1)));

        FluidIngredient tag = FluidIngredient.parse("#c:water");
        assertTrue(tag.test(FluidStack.of(WATER, 1)));
        assertEquals(List.of("minecraft:water"),
                tag.candidates().stream().map(type -> type.key().toString()).toList());

        FluidIngredient fromMap = FluidIngredient.parse(Map.of("fluid", "minecraft:water"));
        assertTrue(fromMap.test(FluidStack.of(WATER, 1)));

        FluidIngredient fromTagMap = FluidIngredient.parse(Map.of("tag", "c:water"));
        assertTrue(fromTagMap.test(FluidStack.of(WATER, 1)));

        FluidIngredient list = FluidIngredient.parse(List.of("mytech:oil", "#c:water"));
        assertTrue(list.test(FluidStack.of(OIL, 1)));
        assertTrue(list.test(FluidStack.of(WATER, 1)));
    }

    @Test
    void parseSizedAppliesDefaultAndExplicitAmount() {
        SizedFluidIngredient defaulted = FluidIngredient.parseSized("minecraft:water", 1_000);
        assertEquals(1_000, defaulted.amount());

        SizedFluidIngredient explicit = FluidIngredient.parseSized(Map.of("fluid", "minecraft:water", "amount", 250), 1_000);
        assertEquals(250, explicit.amount());
    }

    @Test
    void parseRejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> FluidIngredient.parse(42));
        assertThrows(IllegalArgumentException.class, () -> FluidIngredient.parse(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> FluidIngredient.parse(Map.of("fluid", 5)));
        assertThrows(IllegalArgumentException.class, () -> FluidIngredient.parse(List.of()));
        assertThrows(IllegalArgumentException.class, () -> FluidIngredient.parseSized("minecraft:water", 0));
    }
}
