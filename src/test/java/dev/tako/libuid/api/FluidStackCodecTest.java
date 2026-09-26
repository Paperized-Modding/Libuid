package dev.tako.libuid.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FluidStackCodecTest {
    private static final FluidType OIL = FluidType.builder(ResourceKey.of("mytech:oil")).build();
    private static final FluidComponentType<Integer> TEMPERATURE = FluidComponentType.of(
            ResourceKey.of("mytech:temperature"), Integer.class);

    @AfterEach
    void clearRegistry() {
        FluidRegistry.clearForTests();
        FluidComponentRegistry.clearForTests();
    }

    @Test
    void mapAndBinaryCodecsRoundTripTypedComponents() {
        FluidRegistry.register(OIL);
        FluidComponentRegistry.register(TEMPERATURE);
        FluidStack original = FluidStack.of(OIL, 1_000).with(TEMPERATURE, 500);

        Map<String, Object> encoded = FluidStackCodec.toMap(original);
        FluidStack fromMap = FluidStackCodec.fromMap(encoded);
        FluidStack fromBinary = FluidStackCodec.fromBinary(FluidStackCodec.toBinary(original));

        assertEquals("mytech:oil", encoded.get("id"));
        assertEquals(1_000, fromMap.amount());
        assertEquals(500, fromMap.get(TEMPERATURE));
        assertEquals(500, fromBinary.get(TEMPERATURE));
    }

    @Test
    void optionalBinaryEncodesEmptyStackAsZeroLengthArray() {
        FluidRegistry.register(OIL);

        assertEquals(0, FluidStackCodec.toBinaryOptional(FluidStack.EMPTY).length);
        assertSame(FluidStack.EMPTY, FluidStackCodec.fromBinaryOptional(new byte[0]));
        assertSame(FluidStack.EMPTY, FluidStackCodec.fromBinaryOptional(null));

        FluidStack filled = FluidStack.of(OIL, 250);
        FluidStack decoded = FluidStackCodec.fromBinaryOptional(FluidStackCodec.toBinaryOptional(filled));
        assertTrue(FluidStack.matches(filled, decoded));
    }
}
