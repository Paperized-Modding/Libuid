package dev.tako.libuid.api;

import java.util.Objects;
import java.util.regex.Pattern;

                                                        
public record ResourceKey(String namespace, String value) {
    private static final Pattern PART = Pattern.compile("[a-z0-9._-]+");

    public ResourceKey {
        if (!PART.matcher(namespace).matches() || !PART.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid namespaced key: " + namespace + ':' + value);
        }
    }

    public static ResourceKey of(String key) {
        Objects.requireNonNull(key, "key");
        int split = key.indexOf(':');
        if (split <= 0 || split != key.lastIndexOf(':') || split == key.length() - 1) {
            throw new IllegalArgumentException("Key must use namespace:path: " + key);
        }
        return new ResourceKey(key.substring(0, split), key.substring(split + 1));
    }

    @Override
    public String toString() {
        return namespace + ':' + value;
    }
}
