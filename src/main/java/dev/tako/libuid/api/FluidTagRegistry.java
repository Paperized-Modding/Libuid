package dev.tako.libuid.api;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

                                                                                   
public final class FluidTagRegistry {
    private static volatile Map<ResourceKey, Set<ResourceKey>> codeValues = Map.of();
    private static volatile Map<ResourceKey, Set<ResourceKey>> configuredValues = Map.of();
    private static volatile Map<ResourceKey, Set<ResourceKey>> values = Map.of();
    private static volatile Map<ResourceKey, Set<ResourceKey>> reverseValues = Map.of();

    private FluidTagRegistry() {}

                                                                                                
    public static synchronized void register(ResourceKey tag, Collection<ResourceKey> fluids) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fluids, "fluids");
        Map<ResourceKey, Set<ResourceKey>> copy = mutableCopy(codeValues);
        copy.computeIfAbsent(tag, ignored -> new LinkedHashSet<>()).addAll(fluids);
        codeValues = immutableCopy(copy);
        rebuild();
    }

                                                                                                            
    public static synchronized void replaceConfigured(Map<ResourceKey, ? extends Collection<ResourceKey>> tags) {
        Objects.requireNonNull(tags, "tags");
        Map<ResourceKey, Set<ResourceKey>> copy = new LinkedHashMap<>();
        for (var entry : tags.entrySet()) {
            copy.put(Objects.requireNonNull(entry.getKey(), "tag"), Set.copyOf(entry.getValue()));
        }
        configuredValues = immutableCopy(copy);
        rebuild();
    }

    public static Set<ResourceKey> members(ResourceKey tag) { return values.getOrDefault(tag, Set.of()); }
    public static Set<ResourceKey> tagsOf(ResourceKey fluid) { return reverseValues.getOrDefault(fluid, Set.of()); }
    public static boolean contains(ResourceKey tag, ResourceKey fluid) { return members(tag).contains(fluid); }

    private static void rebuild() {
        Map<ResourceKey, Set<ResourceKey>> merged = mutableCopy(codeValues);
        configuredValues.forEach((tag, fluids) -> merged.put(tag, new LinkedHashSet<>(fluids)));
        values = immutableCopy(merged);

        Map<ResourceKey, Set<ResourceKey>> reverse = new LinkedHashMap<>();
        values.forEach((tag, fluids) -> fluids.forEach(fluid -> reverse.computeIfAbsent(fluid, ignored -> new LinkedHashSet<>()).add(tag)));
        reverseValues = immutableCopy(reverse);
    }

    private static Map<ResourceKey, Set<ResourceKey>> mutableCopy(Map<ResourceKey, Set<ResourceKey>> source) {
        Map<ResourceKey, Set<ResourceKey>> copy = new LinkedHashMap<>();
        source.forEach((key, members) -> copy.put(key, new LinkedHashSet<>(members)));
        return copy;
    }

    private static Map<ResourceKey, Set<ResourceKey>> immutableCopy(Map<ResourceKey, Set<ResourceKey>> source) {
        Map<ResourceKey, Set<ResourceKey>> copy = new LinkedHashMap<>();
        source.forEach((key, members) -> copy.put(key, Set.copyOf(members)));
        return Map.copyOf(copy);
    }

    static synchronized void clearForTests() {
        codeValues = Map.of();
        configuredValues = Map.of();
        values = Map.of();
        reverseValues = Map.of();
    }
}
