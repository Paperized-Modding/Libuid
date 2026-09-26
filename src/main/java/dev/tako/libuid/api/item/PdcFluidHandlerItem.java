package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidAction;
import dev.tako.libuid.api.FluidStack;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

                                                                
public final class PdcFluidHandlerItem implements FluidHandlerItem {
    private final ItemStack container;
    private final FluidContainerDefinition definition;
    private FluidStack fluid;
    private final boolean valid;

    public PdcFluidHandlerItem(ItemStack item, FluidContainerDefinition definition) {
        this.container = one(Objects.requireNonNull(item, "item"));
        this.definition = Objects.requireNonNull(definition, "definition");
        ItemFluidDataReadResult stored = ItemFluidData.read(container);
        this.fluid = stored.fluid();
        this.valid = stored.status() != ItemFluidDataReadResult.Status.INVALID
                && (fluid.isEmpty() || definition.accepts(fluid));
    }

    @Override public ItemStack container() { return container.clone(); }
    @Override public int tanks() { return 1; }
    @Override public FluidStack fluidInTank(int tank) { checkTank(tank); return fluid; }
    @Override public int tankCapacity(int tank) { checkTank(tank); return definition.capacity(); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { checkTank(tank); return stack.isEmpty() || definition.accepts(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(action, "action");
        if (!valid || resource.isEmpty() || !definition.accepts(resource)) return 0;
        if (!fluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, resource)) return 0;
        int accepted = Math.min(Math.max(0, definition.capacity() - fluid.amount()), resource.amount());
        if (accepted > 0 && action.execute()) {
            fluid = fluid.isEmpty() ? resource.copyWithAmount(accepted) : fluid.grow(accepted);
            ItemFluidData.write(container, fluid);
        }
        return accepted;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(resource, "resource");
        if (!valid || resource.isEmpty() || !FluidStack.isSameFluidSameComponents(fluid, resource)) return FluidStack.EMPTY;
        return drain(resource.amount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        Objects.requireNonNull(action, "action");
        if (!valid || fluid.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        FluidStack drained = fluid.copyWithAmount(Math.min(maxDrain, fluid.amount()));
        if (action.execute()) {
            fluid = fluid.shrink(drained.amount());
            ItemFluidData.write(container, fluid);
        }
        return drained;
    }

    private static ItemStack one(ItemStack item) {
        ItemStack copy = item.clone();
        copy.setAmount(1);
        return copy;
    }

    private static void checkTank(int tank) {
        if (tank != 0) throw new IndexOutOfBoundsException("Tank " + tank + " does not exist");
    }
}
