package dev.tako.libuid.api;

import java.util.Objects;

                                                                        
public final class FluidComponentType<T> {
    private final ResourceKey key;
    private final Class<T> valueType;

    private FluidComponentType(ResourceKey key, Class<T> valueType) {
        this.key = Objects.requireNonNull(key, "key");
        this.valueType = Objects.requireNonNull(valueType, "valueType");
    }

    public static <T> FluidComponentType<T> of(ResourceKey key, Class<T> valueType) {
        return new FluidComponentType<>(key, valueType);
    }

    public ResourceKey key() { return key; }
    public Class<T> valueType() { return valueType; }
}
