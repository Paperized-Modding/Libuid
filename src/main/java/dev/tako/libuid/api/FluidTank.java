package dev.tako.libuid.api;

import java.util.Objects;
import java.util.function.Predicate;

   
                                                                           
  
                                                                                                    
                                                                                            
   
public class FluidTank implements FluidHandler {
    private final Predicate<FluidStack> validator;
    private final int capacity;
    private FluidStack fluid = FluidStack.EMPTY;

    public FluidTank(int capacity) { this(capacity, stack -> true); }

    public FluidTank(int capacity, Predicate<FluidStack> validator) {
        if (capacity <= 0) throw new IllegalArgumentException("Tank capacity must be positive");
        this.capacity = capacity;
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    @Override public int tanks() { return 1; }

    @Override public FluidStack fluidInTank(int tank) {
        if (tank != 0) throw new IndexOutOfBoundsException("Tank " + tank + " does not exist");
        return fluid;
    }

    @Override public int tankCapacity(int tank) {
        if (tank != 0) throw new IndexOutOfBoundsException("Tank " + tank + " does not exist");
        return capacity;
    }

    @Override public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0) throw new IndexOutOfBoundsException("Tank " + tank + " does not exist");
        return stack.isEmpty() || validator.test(stack);
    }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !validator.test(resource)) return 0;
        if (fluid.isEmpty()) {
            int amount = Math.min(capacity, resource.amount());
            if (action.execute()) {
                fluid = resource.copyWithAmount(amount);
                onContentsChanged();
            }
            return amount;
        }
        if (!FluidStack.isSameFluidSameComponents(fluid, resource)) return 0;
        int amount = Math.min(space(), resource.amount());
        if (action.execute() && amount > 0) {
            fluid = fluid.grow(amount);
            onContentsChanged();
        }
        return amount;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource)) return FluidStack.EMPTY;
        return drain(resource.amount(), action);
    }

    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        if (fluid.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        int drained = Math.min(fluid.amount(), maxDrain);
        FluidStack result = fluid.copyWithAmount(drained);
        if (action.execute()) {
            fluid = fluid.shrink(drained);
            onContentsChanged();
        }
        return result;
    }

                                                                                                 
    public void setFluid(FluidStack stack) {
        fluid = Objects.requireNonNull(stack, "stack");
        onContentsChanged();
    }

    public FluidStack fluid() { return fluid; }
    public int amount() { return fluid.amount(); }
    public int capacity() { return capacity; }
    public int space() { return capacity - fluid.amount(); }
    public boolean isEmpty() { return fluid.isEmpty(); }

                                                                                                           
    protected void onContentsChanged() {}
}
