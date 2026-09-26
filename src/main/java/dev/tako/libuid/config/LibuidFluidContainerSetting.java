package dev.tako.libuid.config;

import dev.tako.libuid.api.FluidIngredient;
import dev.tako.libuid.api.item.FluidContainerDefinition;
import net.momirealms.craftengine.core.item.setting.CustomItemSettingType;
import net.momirealms.craftengine.core.item.setting.ItemSettingsModifier;
import net.momirealms.craftengine.core.item.setting.ItemSettingsModifierType;
import net.momirealms.craftengine.core.item.setting.ItemSettingsModifiers;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

                                                                             
public final class LibuidFluidContainerSetting {
    public static final CustomItemSettingType<FluidContainerDefinition> FLUID_CONTAINER = CustomItemSettingType.simple();
    private static boolean registered;

    private LibuidFluidContainerSetting() {}

    public static synchronized void register() {
        if (registered) return;
        ItemSettingsModifiers.register(Key.of("libuid:fluid_container"), value -> {
            ConfigSection section = value.getAsSection();
            int capacity = section.getInt("capacity", 0);
            Object allowed = section.containsKey("allowed_fluids") ? section.get("allowed_fluids") : null;
            FluidIngredient ingredient = allowed == null ? null : FluidIngredient.parse(allowed);
            FluidContainerDefinition definition = new FluidContainerDefinition(capacity, ingredient);
            return settings -> settings.addCustomData(FLUID_CONTAINER, definition);
        });
        registered = true;
    }
}
