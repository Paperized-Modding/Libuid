package dev.tako.libuid.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

                                                                
public final class FluidComponentMap {
    private static final FluidComponentMap EMPTY = new FluidComponentMap(Map.of());
    private final Map<FluidComponentType<?>, Object> values;

    private FluidComponentMap(Map<FluidComponentType<?>, Object> values) { this.values = Map.copyOf(values); }
    public static FluidComponentMap empty() { return EMPTY; }
    public <T> T get(FluidComponentType<T> type) { return type.valueType().cast(values.get(type)); }
    public <T> T getOrDefault(FluidComponentType<T> type, T fallback) { T value = get(type); return value == null ? fallback : value; }
    public boolean contains(FluidComponentType<?> type) { return values.containsKey(type); }
    Map<FluidComponentType<?>, Object> entries() { return values; }
    public <T> FluidComponentMap with(FluidComponentType<T> type, T value) {
        Objects.requireNonNull(type, "type");
        if (!type.valueType().isInstance(value)) throw new IllegalArgumentException("Invalid value for " + type.key());
        Map<FluidComponentType<?>, Object> copy = new LinkedHashMap<>(values);
        copy.put(type, value);
        return new FluidComponentMap(copy);
    }
    public FluidComponentMap without(FluidComponentType<?> type) {
        if (!values.containsKey(type)) return this;
        Map<FluidComponentType<?>, Object> copy = new LinkedHashMap<>(values);
        copy.remove(type);
        return copy.isEmpty() ? EMPTY : new FluidComponentMap(copy);
    }

    public boolean isEmpty() { return values.isEmpty(); }
    public int size() { return values.size(); }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof FluidComponentMap map && values.equals(map.values));
    }

    @Override
    public int hashCode() { return values.hashCode(); }
}
