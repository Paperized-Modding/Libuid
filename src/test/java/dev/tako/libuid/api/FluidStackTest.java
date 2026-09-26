package dev.tako.libuid.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidStackTest {

    private static final FluidType OIL = FluidType.builder(ResourceKey.of("mytech:oil"))
            .density(900)
            .temperature(293)
            .viscosity(1_000)
            .build();
    private static final FluidComponentType<Integer> TEMPERATURE = FluidComponentType.of(
            ResourceKey.of("mytech:temperature"), Integer.class);

    @Test
    void stackIsImmutableAndUsesMillibuckets() {
        FluidStack stack = FluidStack.of(OIL, FluidStack.BUCKET_VOLUME).with(TEMPERATURE, 500);

        assertEquals(1_000, stack.amount());
        assertEquals(500, stack.get(TEMPERATURE));
        assertNull(FluidStack.of(OIL, 1).get(TEMPERATURE));
        assertEquals(293, FluidStack.of(OIL, 1).getOrDefault(TEMPERATURE, 293));
        assertEquals(500, stack.copyWithAmount(250).get(TEMPERATURE));
        assertEquals(250, stack.copyWithAmount(250).amount());
        assertEquals("mytech:oil", stack.type().key().toString());
    }

    @Test
    void rejectsInvalidKeysAndAmounts() {
        assertThrows(IllegalArgumentException.class, () -> ResourceKey.of("oil"));
        assertThrows(IllegalArgumentException.class, () -> ResourceKey.of("mytech:Bad"));
        assertThrows(IllegalArgumentException.class, () -> FluidStack.of(OIL, 0));
    }

    @Test
    void emptyStackCarriesNoTypeOrAmount() {
        assertTrue(FluidStack.EMPTY.isEmpty());
        assertNull(FluidStack.EMPTY.type());
        assertEquals(0, FluidStack.EMPTY.amount());
        assertSame(FluidStack.EMPTY, FluidStack.ofAllowEmpty(OIL, 0));
        assertSame(FluidStack.EMPTY, FluidStack.ofAllowEmpty(null, 500));
        assertFalse(FluidStack.of(OIL, 1).isEmpty());
    }

    @Test
    void amountArithmeticCollapsesToEmpty() {
        FluidStack bucket = FluidStack.of(OIL, 1_000).with(TEMPERATURE, 500);

        assertEquals(1_250, bucket.grow(250).amount());
        assertEquals(500, bucket.grow(250).get(TEMPERATURE), "components survive amount changes");
        assertEquals(750, bucket.shrink(250).amount());
        assertSame(FluidStack.EMPTY, bucket.shrink(1_000));
        assertSame(FluidStack.EMPTY, bucket.shrink(5_000));
        assertEquals(250, bucket.limitSize(250).amount());
        assertEquals(1_000, bucket.limitSize(5_000).amount(), "limitSize never grows");
        assertSame(FluidStack.EMPTY, bucket.limitSize(0));
    }

    @Test
    void splitReturnsTakenAndRemainder() {
        FluidStack.Split split = FluidStack.of(OIL, 1_000).split(300);

        assertEquals(300, split.taken().amount());
        assertEquals(700, split.remainder().amount());

        FluidStack.Split all = FluidStack.of(OIL, 1_000).split(4_000);
        assertEquals(1_000, all.taken().amount());
        assertTrue(all.remainder().isEmpty());
    }

    @Test
    void comparesByKeySoReloadedTypesStillMatch() {
        FluidType reloaded = FluidType.builder(ResourceKey.of("mytech:oil")).density(123).build();
        assertNotSame(OIL, reloaded);

        FluidStack before = FluidStack.of(OIL, 500);
        FluidStack after = FluidStack.of(reloaded, 500);

        assertTrue(FluidStack.isSameFluid(before, after));
        assertTrue(FluidStack.isSameFluidSameComponents(before, after));
        assertTrue(FluidStack.matches(before, after));
        assertEquals(before.hashFluidAndComponents(), after.hashFluidAndComponents());
    }

    @Test
    void equalitySeparatesAmountFromFluidAndComponents() {
        FluidStack small = FluidStack.of(OIL, 250);
        FluidStack large = FluidStack.of(OIL, 1_000);
        FluidStack hot = large.with(TEMPERATURE, 500);

        assertTrue(FluidStack.isSameFluidSameComponents(small, large), "amount is ignored");
        assertFalse(FluidStack.matches(small, large), "amount matters for matches");
        assertFalse(FluidStack.isSameFluidSameComponents(large, hot));
        assertTrue(FluidStack.isSameFluid(large, hot));
        assertEquals(small.hashFluidAndComponents(), large.hashFluidAndComponents());
        assertNotEquals(large.hashFluidAndComponents(), hot.hashFluidAndComponents());
    }

    @Test
    void emptyStacksCompareEqualToEachOther() {
        assertTrue(FluidStack.matches(FluidStack.EMPTY, FluidStack.EMPTY));
        assertTrue(FluidStack.isSameFluid(FluidStack.EMPTY, FluidStack.EMPTY));
        assertTrue(FluidStack.isSameFluidSameComponents(FluidStack.EMPTY, FluidStack.EMPTY));
        assertFalse(FluidStack.isSameFluid(FluidStack.EMPTY, FluidStack.of(OIL, 1)));
        assertFalse(FluidStack.matches(FluidStack.EMPTY, FluidStack.of(OIL, 1)));
    }
}
