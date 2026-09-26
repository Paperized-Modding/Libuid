package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidAction;
import dev.tako.libuid.api.FluidHandler;
import dev.tako.libuid.api.FluidStack;

import java.util.Objects;

                                                                            
public final class FluidUtil {
    private FluidUtil() {}

       
                                                                                    
                                                                                                          
       
    public static FluidStack tryFluidTransfer(FluidHandler destination, FluidHandler source, int maxAmount, FluidAction action) {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(action, "action");
        if (maxAmount <= 0) return FluidStack.EMPTY;

        FluidStack available = source.drain(maxAmount, FluidAction.SIMULATE);
        if (available.isEmpty()) return FluidStack.EMPTY;
        int accepted = destination.fill(available, FluidAction.SIMULATE);
        if (accepted <= 0) return FluidStack.EMPTY;
        FluidStack exact = available.copyWithAmount(Math.min(available.amount(), accepted));
        if (action.simulate()) return exact;

        FluidStack drained = source.drain(exact, FluidAction.EXECUTE);
        if (!FluidStack.matches(exact, drained)) {
            throw new IllegalStateException("Fluid source violated simulate/execute contract: expected " + exact + ", got " + drained);
        }
        int filled = destination.fill(drained, FluidAction.EXECUTE);
        if (filled != drained.amount()) {
            throw new IllegalStateException("Fluid destination violated simulate/execute contract: expected " + drained.amount() + ", got " + filled);
        }
        return drained;
    }

                                                                                       
    public static FluidActionResult tryEmptyContainer(org.bukkit.inventory.ItemStack container, FluidHandler destination,
                                                       int maxAmount, FluidAction action) {
        var handler = FluidContainerRegistry.handlerFor(container);
        org.bukkit.inventory.ItemStack one = container.clone();
        one.setAmount(1);
        if (handler.isEmpty()) return FluidActionResult.failure(one);
        FluidStack moved = tryFluidTransfer(destination, handler.get(), maxAmount, action);
        if (moved.isEmpty()) return FluidActionResult.failure(one);
        return new FluidActionResult(true, action.execute() ? handler.get().container() : one, moved);
    }

                                                                                      
    public static FluidActionResult tryFillContainer(org.bukkit.inventory.ItemStack container, FluidHandler source,
                                                     int maxAmount, FluidAction action) {
        var handler = FluidContainerRegistry.handlerFor(container);
        org.bukkit.inventory.ItemStack one = container.clone();
        one.setAmount(1);
        if (handler.isEmpty()) return FluidActionResult.failure(one);
        FluidStack moved = tryFluidTransfer(handler.get(), source, maxAmount, action);
        if (moved.isEmpty()) return FluidActionResult.failure(one);
        return new FluidActionResult(true, action.execute() ? handler.get().container() : one, moved);
    }
}
