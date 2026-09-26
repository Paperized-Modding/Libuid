package dev.tako.libuid.api;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

                                                                        
public final class FluidType {
    private final ResourceKey key;
    private final Properties properties;
    private final Map<FluidSoundAction, String> sounds;

    private FluidType(ResourceKey key, Properties properties) {
        this.key = key;
        this.properties = properties;
        this.sounds = Map.copyOf(properties.sounds);
    }

    public static Properties builder(ResourceKey key) {
        return new Properties(key);
    }

    public ResourceKey key() { return key; }
    public int density() { return properties.density; }
    public int temperature() { return properties.temperature; }
    public int viscosity() { return properties.viscosity; }
    public int lightLevel() { return properties.lightLevel; }
    public FluidRarity rarity() { return properties.rarity; }
    public Optional<String> displayName() { return Optional.ofNullable(properties.displayName); }
    public Optional<Integer> color() { return Optional.ofNullable(properties.color); }
    public Optional<String> texture() { return Optional.ofNullable(properties.texture); }
    public Optional<String> sound(FluidSoundAction action) { return Optional.ofNullable(sounds.get(action)); }
    public Optional<String> bucketItem() { return Optional.ofNullable(properties.bucketItem); }

    public static final class Properties {
        private final ResourceKey key;
        private int density = 1_000;
        private int temperature = 300;
        private int viscosity = 1_000;
        private int lightLevel;
        private FluidRarity rarity = FluidRarity.COMMON;
        private String displayName;
        private Integer color;
        private String texture;
        private final EnumMap<FluidSoundAction, String> sounds = new EnumMap<>(FluidSoundAction.class);
        private String bucketItem;

        private Properties(ResourceKey key) { this.key = Objects.requireNonNull(key, "key"); }
        public Properties density(int value) { density = value; return this; }
        public Properties temperature(int value) { temperature = value; return this; }
        public Properties viscosity(int value) { viscosity = value; return this; }
        public Properties lightLevel(int value) { lightLevel = value; return this; }
        public Properties rarity(FluidRarity value) { rarity = Objects.requireNonNull(value, "rarity"); return this; }
        public Properties displayName(String value) { displayName = value; return this; }
        public Properties color(int value) { color = value; return this; }
        public Properties texture(String value) { texture = value; return this; }
        public Properties sound(FluidSoundAction action, String soundKey) {
            sounds.put(Objects.requireNonNull(action, "action"), Objects.requireNonNull(soundKey, "soundKey"));
            return this;
        }
        public Properties bucketItem(String itemId) { bucketItem = Objects.requireNonNull(itemId, "itemId"); return this; }

        public FluidType build() {
            if (viscosity < 0) throw new IllegalArgumentException("Viscosity must not be negative");
            if (lightLevel < 0 || lightLevel > 15) throw new IllegalArgumentException("Light level must be in [0, 15]");
            if (color != null && (color < 0 || color > 0xFFFFFF)) throw new IllegalArgumentException("Color must be RGB");
            return new FluidType(key, this);
        }
    }
}
