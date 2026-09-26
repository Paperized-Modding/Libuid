package dev.tako.libuid.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FluidTankTest {
    private static final FluidType WATER = FluidType.builder(ResourceKey.of("minecraft:water")).build();
    private static final FluidType OIL = FluidType.builder(ResourceKey.of("mytech:oil")).build();
    private static final FluidComponentType<Integer> TEMPERATURE = FluidComponentType.of(
            ResourceKey.of("mytech:temperature"), Integer.class);

    @Test
    void fillsEmptyTankUpToCapacity() {
        FluidTank tank = new FluidTank(1_000);

        assertEquals(1_000, tank.fill(FluidStack.of(WATER, 2_000), FluidAction.EXECUTE));
        assertEquals(1_000, tank.amount());
        assertTrue(FluidStack.isSameFluid(FluidStack.of(WATER, 1), tank.fluid()));
        assertEquals(0, tank.space());
    }

    @Test
    void fillAllowsPartialAndSimulateDoesNotMutate() {
        FluidTank tank = new FluidTank(1_000);

        assertEquals(400, tank.fill(FluidStack.of(WATER, 400), FluidAction.SIMULATE));
        assertTrue(tank.isEmpty(), "SIMULATE must not mutate");

        assertEquals(400, tank.fill(FluidStack.of(WATER, 400), FluidAction.EXECUTE));
        assertEquals(400, tank.amount());
    }

    @Test
    void fillRejectsDifferentFluidAndComponents() {
        FluidTank tank = new FluidTank(1_000);
        tank.setFluid(FluidStack.of(WATER, 100));

        assertEquals(0, tank.fill(FluidStack.of(OIL, 500), FluidAction.EXECUTE));
        assertEquals(0, tank.fill(FluidStack.of(WATER, 100).with(TEMPERATURE, 400), FluidAction.EXECUTE));
        assertEquals(100, tank.amount(), "incompatible fills change nothing");
    }

    @Test
    void fillRejectsValidatorBlockedFluids() {
        FluidTank tank = new FluidTank(1_000, stack -> FluidStack.isSameFluid(stack, FluidStack.of(WATER, 1)));

        assertEquals(0, tank.fill(FluidStack.of(OIL, 500), FluidAction.EXECUTE));
        assertEquals(500, tank.fill(FluidStack.of(WATER, 500), FluidAction.EXECUTE));
    }

    @Test
    void drainIsFluidSensitiveAndComponentSensitive() {
        FluidTank tank = new FluidTank(1_000);
        tank.setFluid(FluidStack.of(WATER, 800).with(TEMPERATURE, 300));

        assertTrue(tank.drain(FluidStack.of(OIL, 100), FluidAction.EXECUTE).isEmpty());
        assertTrue(tank.drain(FluidStack.of(WATER, 100), FluidAction.EXECUTE).isEmpty(),
                "drain(FluidStack) must reject when components differ");

        FluidStack taken = tank.drain(FluidStack.of(WATER, 100).with(TEMPERATURE, 300), FluidAction.EXECUTE);
        assertEquals(100, taken.amount());
        assertEquals(700, tank.amount());
    }

    @Test
    void drainByAmountIgnoresFluidAndSupportsSimulate() {
        FluidTank tank = new FluidTank(1_000);
        tank.setFluid(FluidStack.of(WATER, 800));

        assertEquals(500, tank.drain(500, FluidAction.SIMULATE).amount());
        assertEquals(800, tank.amount(), "SIMULATE must not mutate");

        assertEquals(500, tank.drain(500, FluidAction.EXECUTE).amount());
        assertEquals(300, tank.amount());
        assertTrue(FluidStack.isSameFluid(FluidStack.of(WATER, 1), tank.fluid()), "components survive a partial drain");
    }

    @Test
    void contentsChangedFiresOnSetFillAndDrain() {
        AtomicInteger changes = new AtomicInteger();
        FluidTank tank = new FluidTank(1_000) {
            @Override
            protected void onContentsChanged() { changes.incrementAndGet(); }
        };

        tank.setFluid(FluidStack.of(WATER, 100));
        assertEquals(1, changes.get());
        tank.fill(FluidStack.of(WATER, 100), FluidAction.EXECUTE);
        assertEquals(2, changes.get());
        tank.fill(FluidStack.of(WATER, 100), FluidAction.SIMULATE);
        assertEquals(2, changes.get(), "simulation never fires a change");
        tank.drain(50, FluidAction.EXECUTE);
        assertEquals(3, changes.get());
    }
}
