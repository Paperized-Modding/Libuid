package dev.tako.libuid.api;

import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;

                                                                                
public final class LibuidLoadingStages {
    public static final LoadingStage FLUID = new LoadingStage("libuid:fluid");
    public static final LoadingStage FLUID_TAG = new LoadingStage("libuid:fluid_tag");

    private LibuidLoadingStages() {
    }
}
