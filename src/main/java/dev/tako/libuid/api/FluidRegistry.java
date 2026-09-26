package dev.tako.libuid.api;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

                                                                                                     
public final class FluidRegistry {
    private static volatile Map<ResourceKey, FluidType> codeValues = Map.of();
    private static volatile Map<ResourceKey, FluidType> configValues = Map.of();
    private static volatile Map<ResourceKey, FluidType> values = Map.of();
    private FluidRegistry() {}

    public static Optional<FluidType> get(ResourceKey key) { return Optional.ofNullable(values.get(key)); }
    public static Optional<FluidType> get(String key) { return get(ResourceKey.of(key)); }

                                                                                       
    public static Optional<FluidType> codeDefinition(ResourceKey key) { return Optional.ofNullable(codeValues.get(key)); }
    public static boolean contains(ResourceKey key) { return values.containsKey(key); }
    public static boolean contains(String key) { return get(key).isPresent(); }
    public static Collection<FluidType> all() { return values.values(); }
    public static int size() { return values.size(); }

                                                                                                            
    public static synchronized void register(FluidType type) {
        Objects.requireNonNull(type, "type");
        if (codeValues.containsKey(type.key())) throw new IllegalArgumentException("Duplicate fluid type: " + type.key());
        Map<ResourceKey, FluidType> copy = new LinkedHashMap<>(codeValues);
        copy.put(type.key(), type);
        codeValues = Map.copyOf(copy);
        rebuild();
    }

                                                                           
    public static synchronized void replaceConfigured(Collection<FluidType> types) {
        Objects.requireNonNull(types, "types");
        Map<ResourceKey, FluidType> replacement = new LinkedHashMap<>();
        for (FluidType type : types) {
            if (replacement.putIfAbsent(type.key(), type) != null) throw new IllegalArgumentException("Duplicate configured fluid: " + type.key());
        }
        configValues = Map.copyOf(replacement);
        rebuild();
    }

    private static void rebuild() {
        Map<ResourceKey, FluidType> combined = new LinkedHashMap<>(codeValues);
        combined.putAll(configValues);
        values = Map.copyOf(combined);
    }

    static synchronized void clearForTests() { codeValues = Map.of(); configValues = Map.of(); values = Map.of(); }
}
