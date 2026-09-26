package dev.tako.libuid.api.item;

import dev.tako.libuid.api.FluidAction;
import dev.tako.libuid.api.FluidRegistry;
import dev.tako.libuid.api.FluidStack;
import dev.tako.libuid.api.FluidType;
import dev.tako.libuid.api.ResourceKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

                                                                               
public final class VanillaBucketHandler implements FluidHandlerItem {
    private static final Map<Material, ResourceKey> FLUID_BUCKETS = Map.of(
            Material.WATER_BUCKET, ResourceKey.of("minecraft:water"),
            Material.LAVA_BUCKET, ResourceKey.of("minecraft:lava"),
            Material.MILK_BUCKET, ResourceKey.of("minecraft:milk"));
    private final ItemStack container;
    private final FluidStack fluid;

    public VanillaBucketHandler(ItemStack item) {
        container = item.clone();
        container.setAmount(1);
        ResourceKey key = FLUID_BUCKETS.get(container.getType());
        fluid = key == null ? FluidStack.EMPTY : FluidRegistry.get(key)
                .map(type -> FluidStack.of(type, FluidStack.BUCKET_VOLUME)).orElse(FluidStack.EMPTY);
    }

    public static boolean supports(ItemStack item) {
        return item.getType() == Material.BUCKET || FLUID_BUCKETS.containsKey(item.getType());
    }

    @Override public ItemStack container() { return container.clone(); }
    @Override public int tanks() { return 1; }
    @Override public FluidStack fluidInTank(int tank) { checkTank(tank); return fluid; }
    @Override public int tankCapacity(int tank) { checkTank(tank); return FluidStack.BUCKET_VOLUME; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { checkTank(tank); return bucketMaterial(stack) != null; }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (container.getType() != Material.BUCKET || resource.amount() < FluidStack.BUCKET_VOLUME) return 0;
        Material filled = bucketMaterial(resource);
        if (filled == null) return 0;
        if (action.execute()) container.setType(filled);
        return FluidStack.BUCKET_VOLUME;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.amount() < FluidStack.BUCKET_VOLUME || !FluidStack.isSameFluidSameComponents(fluid, resource)) return FluidStack.EMPTY;
        return drain(FluidStack.BUCKET_VOLUME, action);
    }

    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        if (fluid.isEmpty() || maxDrain < FluidStack.BUCKET_VOLUME) return FluidStack.EMPTY;
        if (action.execute()) container.setType(Material.BUCKET);
        return fluid;
    }

    private static Material bucketMaterial(FluidStack stack) {
        if (stack.isEmpty() || !stack.components().isEmpty()) return null;
        return FLUID_BUCKETS.entrySet().stream()
                .filter(entry -> entry.getValue().equals(stack.fluidKey()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private static void checkTank(int tank) { if (tank != 0) throw new IndexOutOfBoundsException("Tank " + tank + " does not exist"); }
}
