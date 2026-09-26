package dev.tako.libuid.builtin;

import dev.tako.libuid.api.FluidType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinFluidTypesTest {
    @Test
    void exposesNeoForgeWaterLavaAndMilkDefinitions() {
        Map<String, FluidType> types = BuiltinFluidTypes.all().stream()
                .collect(java.util.stream.Collectors.toMap(type -> type.key().toString(), Function.identity()));

        assertEquals(3, types.size());
        assertTrue(types.containsKey("minecraft:water"));
        assertTrue(types.containsKey("minecraft:lava"));
        assertTrue(types.containsKey("minecraft:milk"));
        assertEquals(3000, types.get("minecraft:lava").density());
        assertEquals(6000, types.get("minecraft:lava").viscosity());
        assertEquals(1300, types.get("minecraft:lava").temperature());
        assertEquals(15, types.get("minecraft:lava").lightLevel());
        assertEquals(0xFFFFFF, types.get("minecraft:lava").color().orElseThrow(),
                "lava texture already carries its colour, so the tint must stay neutral");
        assertEquals(1024, types.get("minecraft:milk").density());
        assertEquals(1024, types.get("minecraft:milk").viscosity());
    }
}
