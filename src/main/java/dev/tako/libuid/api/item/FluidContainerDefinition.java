package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidIngredient;

import java.util.Objects;

                                                                 
public record FluidContainerDefinition(int capacity, FluidIngredient allowedFluids) {
    public FluidContainerDefinition {
        if (capacity <= 0) throw new IllegalArgumentException("Fluid container capacity must be positive");
    }

    public boolean accepts(dev.tako.libuid.api.FluidStack stack) {
        return !stack.isEmpty() && (allowedFluids == null || allowedFluids.test(stack));
    }
}
