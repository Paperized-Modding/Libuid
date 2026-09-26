package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidStack;
import dev.tako.libuid.api.FluidStackCodec;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

                                                                             
public final class ItemFluidData {
    public static final NamespacedKey KEY_FLUID_STACK = new NamespacedKey("libuid", "fluid_stack");

    private ItemFluidData() {}

    public static ItemFluidDataReadResult read(ItemStack item) {
        Objects.requireNonNull(item, "item");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ItemFluidDataReadResult(FluidStack.EMPTY, ItemFluidDataReadResult.Status.EMPTY);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(KEY_FLUID_STACK, PersistentDataType.BYTE_ARRAY)) {
            return new ItemFluidDataReadResult(FluidStack.EMPTY, ItemFluidDataReadResult.Status.EMPTY);
        }
        try {
            FluidStack stack = FluidStackCodec.fromBinaryOptional(pdc.get(KEY_FLUID_STACK, PersistentDataType.BYTE_ARRAY));
            return new ItemFluidDataReadResult(stack, stack.isEmpty()
                    ? ItemFluidDataReadResult.Status.EMPTY
                    : ItemFluidDataReadResult.Status.PRESENT);
        } catch (IllegalArgumentException ignored) {
            return new ItemFluidDataReadResult(FluidStack.EMPTY, ItemFluidDataReadResult.Status.INVALID);
        }
    }

                                                                       
    public static void write(ItemStack item, FluidStack fluid) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(fluid, "fluid");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalArgumentException("Item does not support metadata: " + item.getType());
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (fluid.isEmpty()) pdc.remove(KEY_FLUID_STACK);
        else pdc.set(KEY_FLUID_STACK, PersistentDataType.BYTE_ARRAY, FluidStackCodec.toBinary(fluid));
        item.setItemMeta(meta);
    }
}
