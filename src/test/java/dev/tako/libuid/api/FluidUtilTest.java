package dev.tako.libuid.api;

import dev.tako.libuid.api.item.FluidUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidUtilTest {
    private static final FluidType WATER = FluidType.builder(ResourceKey.of("minecraft:water")).build();

    @Test
    void transferMovesOnlyDestinationSpace() {
        FluidTank source = new FluidTank(1_000);
        source.setFluid(FluidStack.of(WATER, 1_000));
        FluidTank destination = new FluidTank(1_000);
        destination.setFluid(FluidStack.of(WATER, 800));

        FluidStack moved = FluidUtil.tryFluidTransfer(destination, source, 1_000, FluidAction.EXECUTE);

        assertEquals(200, moved.amount());
        assertEquals(800, source.amount());
        assertEquals(1_000, destination.amount());
    }

    @Test
    void simulatedTransferDoesNotMutateEitherHandler() {
        FluidTank source = new FluidTank(1_000);
        source.setFluid(FluidStack.of(WATER, 500));
        FluidTank destination = new FluidTank(1_000);

        FluidStack moved = FluidUtil.tryFluidTransfer(destination, source, 1_000, FluidAction.SIMULATE);

        assertEquals(500, moved.amount());
        assertEquals(500, source.amount());
        assertTrue(destination.isEmpty());
    }
}
