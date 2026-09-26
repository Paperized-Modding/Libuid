package dev.tako.libuid.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

                                                                    
public final class FluidComponentRegistry {
    private static volatile Map<ResourceKey, FluidComponentType<?>> values = Map.of();
    private FluidComponentRegistry() {}

    public static Optional<FluidComponentType<?>> get(ResourceKey key) { return Optional.ofNullable(values.get(key)); }
    public static synchronized void register(FluidComponentType<?> type) {
        Objects.requireNonNull(type, "type");
        if (values.containsKey(type.key())) throw new IllegalArgumentException("Duplicate fluid component: " + type.key());
        Map<ResourceKey, FluidComponentType<?>> copy = new LinkedHashMap<>(values);
        copy.put(type.key(), type);
        values = Map.copyOf(copy);
    }
    static synchronized void clearForTests() { values = Map.of(); }
}
