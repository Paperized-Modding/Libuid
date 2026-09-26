package dev.tako.libuid;

import dev.tako.libuid.bstats.Metrics;
import dev.tako.libuid.builtin.BuiltinFluidTags;
import dev.tako.libuid.builtin.BuiltinFluidTypes;
import dev.tako.libuid.config.LibuidConfigRegistrations;
import dev.tako.libuid.config.LibuidFluidContainerSetting;
import org.bukkit.plugin.java.JavaPlugin;

                                                                                 
public final class LibuidPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        BuiltinFluidTypes.register();
        BuiltinFluidTags.register();
        LibuidFluidContainerSetting.register();
        if (!LibuidConfigRegistrations.register(getLogger())) {
            throw new IllegalStateException("Libuid could not claim the libuid_fluid(s) CraftEngine section");
        }
    }

    @Override
    public void onEnable() {
        new Metrics(this, 34149);
    }
}
