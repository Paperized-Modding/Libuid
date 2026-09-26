package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidStack;
import java.util.Objects;

                                                                
public record ItemFluidDataReadResult(FluidStack fluid, Status status) {
    public enum Status { EMPTY, PRESENT, INVALID }

    public ItemFluidDataReadResult {
        fluid = Objects.requireNonNull(fluid, "fluid");
        status = Objects.requireNonNull(status, "status");
    }
}
