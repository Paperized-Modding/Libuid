package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidStack;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

                                                                                     
public record FluidActionResult(boolean success, ItemStack result, FluidStack moved) {
    public FluidActionResult {
        result = Objects.requireNonNull(result, "result");
        moved = Objects.requireNonNull(moved, "moved");
    }

    public static FluidActionResult failure(ItemStack input) {
        return new FluidActionResult(false, input, FluidStack.EMPTY);
    }
}
