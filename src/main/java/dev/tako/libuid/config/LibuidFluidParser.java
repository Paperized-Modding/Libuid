package dev.tako.libuid.config;

import dev.tako.libuid.api.FluidRarity;
import dev.tako.libuid.api.FluidRegistry;
import dev.tako.libuid.api.FluidSoundAction;
import dev.tako.libuid.api.FluidType;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

                                                                                      
public final class LibuidFluidParser extends IdSectionConfigParser {
    public static final Key TYPE = Key.of("libuid:fluid");
    private final Logger logger;
    private Map<ResourceKey, FluidType> pending = Map.of();

    public LibuidFluidParser(Logger logger) { this.logger = logger; }

    @Override public @NotNull Key type() { return TYPE; }
    @Override public String @NotNull [] sectionId() { return ConfigKeys.of("libuid_fluid(s)"); }
    @Override public @NotNull LoadingStage loadingStage() { return LibuidLoadingStages.FLUID; }
    @Override public @NotNull List<LoadingStage> dependencies() { return List.of(); }
    @Override public boolean async() { return false; }

    @Override public void preProcess() { pending = new LinkedHashMap<>(); }

    @Override protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
        ResourceKey key;
        try { key = ResourceKey.of(id.toString()); }
        catch (IllegalArgumentException exception) { logger.warning("Ignoring invalid Libuid fluid id at " + path + ": " + id); return; }
        if (pending.containsKey(key)) { logger.warning("Ignoring duplicate Libuid fluid " + key + " at " + path); return; }
        try { pending.put(key, decode(key, section, FluidRegistry.codeDefinition(key).orElse(null))); }
        catch (IllegalArgumentException exception) { logger.warning("Ignoring Libuid fluid " + key + " at " + path + ": " + exception.getMessage()); }
    }

    @Override public void postProcess() {
        FluidRegistry.replaceConfigured(pending.values());
        logger.info("Parsed " + pending.size() + " Libuid fluid definition(s)");
        pending = Map.of();
    }

    static FluidType decode(ResourceKey key, ConfigSection section, FluidType baseline) {
        FluidType.Properties properties = FluidType.builder(key)
                .density(baseline == null ? 1_000 : baseline.density())
                .temperature(baseline == null ? 300 : baseline.temperature())
                .viscosity(baseline == null ? 1_000 : baseline.viscosity())
                .lightLevel(baseline == null ? 0 : baseline.lightLevel())
                .rarity(baseline == null ? FluidRarity.COMMON : baseline.rarity());
        if (baseline != null) {
            baseline.displayName().ifPresent(properties::displayName);
            baseline.texture().ifPresent(properties::texture);
            baseline.color().ifPresent(properties::color);
            baseline.bucketItem().ifPresent(properties::bucketItem);
            for (FluidSoundAction action : FluidSoundAction.values()) {
                baseline.sound(action).ifPresent(sound -> properties.sound(action, sound));
            }
        }
        if (section.containsKey("density")) properties.density(section.getInt("density", 1_000));
        if (section.containsKey("temperature")) properties.temperature(section.getInt("temperature", 300));
        if (section.containsKey("viscosity")) properties.viscosity(section.getInt("viscosity", 1_000));
        if (section.containsKey("light-level")) properties.lightLevel(section.getInt("light-level", 0));
        else if (section.containsKey("light_level")) properties.lightLevel(section.getInt("light_level", 0));
        String rarity = section.getString("rarity");
        if (rarity != null && !rarity.isBlank()) properties.rarity(FluidRarity.valueOf(rarity.trim().toUpperCase(Locale.ROOT)));
        String name = section.getString("name");
        if (name != null && !name.isBlank()) properties.displayName(name);
        String texture = section.getString("texture");
        if (texture != null && !texture.isBlank()) properties.texture(texture);
        String color = section.getString("color");
        if (color != null && !color.isBlank()) {
            String hex = color.startsWith("#") ? color.substring(1) : color;
            if (!hex.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("color must be #RRGGBB");
            properties.color(Integer.parseInt(hex, 16));
        }
        String bucket = section.getString("bucket");
        if (bucket != null && !bucket.isBlank()) properties.bucketItem(bucket);
        ConfigSection sounds = section.getSection("sounds");
        if (sounds != null) {
            for (String soundKey : sounds.values().keySet()) {
                String sound = sounds.getString(soundKey);
                if (sound == null || sound.isBlank()) continue;
                try {
                    properties.sound(FluidSoundAction.valueOf(soundKey.toUpperCase(Locale.ROOT).replace('-', '_')), sound);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("unknown sound action: " + soundKey);
                }
            }
        }
        return properties.build();
    }
}
