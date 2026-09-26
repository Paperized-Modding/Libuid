package dev.tako.libuid.api.item;

import dev.tako.libuid.config.LibuidFluidContainerSetting;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

                                                      
public final class FluidContainerRegistry {
    private FluidContainerRegistry() {}

    public static Optional<FluidHandlerItem> handlerFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        BukkitItemDefinition definition = CraftEngineItems.byItemStack(stack);
        if (definition != null) {
            FluidContainerDefinition container = definition.settings()
                    .getCustomData(LibuidFluidContainerSetting.FLUID_CONTAINER);
            if (container != null) return Optional.of(new PdcFluidHandlerItem(stack, container));
        }
        return VanillaBucketHandler.supports(stack)
                ? Optional.of(new VanillaBucketHandler(stack))
                : Optional.empty();
    }
}
