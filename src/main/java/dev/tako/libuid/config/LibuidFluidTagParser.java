package dev.tako.libuid.config;

import dev.tako.libuid.api.FluidTagRegistry;
import dev.tako.libuid.api.LibuidLoadingStages;
import dev.tako.libuid.api.ResourceKey;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

   
                                                                                       
  
                                                                      
              
                     
             
              
                          
                           
          
   
public final class LibuidFluidTagParser extends IdSectionConfigParser {
    public static final Key TYPE = Key.of("libuid:fluid_tag");
    private final Logger logger;
    private Map<ResourceKey, Set<ResourceKey>> pending = Map.of();

    public LibuidFluidTagParser(Logger logger) { this.logger = logger; }

    @Override public @NotNull Key type() { return TYPE; }
    @Override public String @NotNull [] sectionId() { return ConfigKeys.of("libuid_fluid_tag(s)"); }
    @Override public @NotNull LoadingStage loadingStage() { return LibuidLoadingStages.FLUID_TAG; }
    @Override public @NotNull List<LoadingStage> dependencies() { return List.of(LibuidLoadingStages.FLUID); }
    @Override public boolean async() { return false; }

    @Override public void preProcess() { pending = new LinkedHashMap<>(); }

    @Override protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
        ResourceKey tag;
        try { tag = ResourceKey.of(id.toString()); }
        catch (IllegalArgumentException exception) {
            logger.warning("Ignoring invalid Libuid fluid tag id at " + path + ": " + id);
            return;
        }
        List<String> fluids = section.getStringList("fluids");
        if (fluids == null || fluids.isEmpty()) {
            logger.warning("Ignoring Libuid fluid tag " + tag + " without a fluids list at " + path);
            return;
        }
        Set<ResourceKey> members = new LinkedHashSet<>();
        for (String fluid : fluids) {
            try { members.add(ResourceKey.of(fluid.trim())); }
            catch (IllegalArgumentException exception) {
                logger.warning("Ignoring invalid fluid in tag " + tag + " at " + path + ": " + fluid);
            }
        }
        if (members.isEmpty()) return;
        pending.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).addAll(members);
    }

    @Override public void postProcess() {
        FluidTagRegistry.replaceConfigured(pending);
        logger.info("Parsed " + pending.size() + " Libuid fluid tag(s)");
        pending = Map.of();
    }
}
