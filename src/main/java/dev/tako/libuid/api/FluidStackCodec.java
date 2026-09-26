package dev.tako.libuid.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

                                                                       
public final class FluidStackCodec {
    private static final int MAGIC = 0x4C554944;        
    private static final byte VERSION = 1;
    private FluidStackCodec() {}

    public static Map<String, Object> toMap(FluidStack stack) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", stack.type().key().toString());
        result.put("amount", stack.amount());
        Map<String, Object> components = new LinkedHashMap<>();
        for (var entry : stack.components().entries().entrySet()) components.put(entry.getKey().key().toString(), entry.getValue());
        if (!components.isEmpty()) result.put("components", Map.copyOf(components));
        return Map.copyOf(result);
    }

    public static FluidStack fromMap(Map<String, ?> map) {
        Object id = map.get("id");
        Object amount = map.get("amount");
        if (!(id instanceof String key) || !(amount instanceof Number number)) throw new IllegalArgumentException("Fluid stack requires string id and numeric amount");
        FluidType type = FluidRegistry.get(ResourceKey.of(key)).orElseThrow(() -> new IllegalArgumentException("Unknown fluid type: " + key));
        FluidStack stack = FluidStack.of(type, number.intValue());
        Object rawComponents = map.get("components");
        if (rawComponents == null) return stack;
        if (!(rawComponents instanceof Map<?, ?> components)) throw new IllegalArgumentException("components must be a map");
        for (var entry : components.entrySet()) {
            if (!(entry.getKey() instanceof String componentKey)) throw new IllegalArgumentException("Component key must be a string");
            FluidComponentType<?> component = FluidComponentRegistry.get(ResourceKey.of(componentKey))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown fluid component: " + componentKey));
            stack = putUnchecked(stack, component, entry.getValue());
        }
        return stack;
    }

    public static byte[] toBinary(FluidStack stack) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(MAGIC); out.writeByte(VERSION); out.writeUTF(stack.type().key().toString()); out.writeInt(stack.amount());
            var entries = stack.components().entries(); out.writeInt(entries.size());
            for (var entry : entries.entrySet()) { out.writeUTF(entry.getKey().key().toString()); writeValue(out, entry.getValue()); }
            return bytes.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("Unexpected binary encoding failure", exception); }
    }

                                                                                                  
    public static byte[] toBinaryOptional(FluidStack stack) {
        return stack.isEmpty() ? new byte[0] : toBinary(stack);
    }

                                                                                                                             
    public static FluidStack fromBinaryOptional(byte[] bytes) {
        return bytes == null || bytes.length == 0 ? FluidStack.EMPTY : fromBinary(bytes);
    }

    public static FluidStack fromBinary(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (in.readInt() != MAGIC || in.readByte() != VERSION) throw new IllegalArgumentException("Unsupported Libuid fluid stack format");
            FluidType type = FluidRegistry.get(ResourceKey.of(in.readUTF())).orElseThrow(() -> new IllegalArgumentException("Unknown fluid type"));
            FluidStack stack = FluidStack.of(type, in.readInt());
            int count = in.readInt();
            if (count < 0 || count > 128) throw new IllegalArgumentException("Invalid component count");
            for (int i = 0; i < count; i++) {
                ResourceKey key = ResourceKey.of(in.readUTF());
                FluidComponentType<?> component = FluidComponentRegistry.get(key).orElseThrow(() -> new IllegalArgumentException("Unknown fluid component: " + key));
                stack = putUnchecked(stack, component, readValue(in, component.valueType()));
            }
            return stack;
        } catch (IOException exception) { throw new IllegalArgumentException("Invalid Libuid fluid stack binary", exception); }
    }

    private static void writeValue(DataOutputStream out, Object value) throws IOException {
        if (value instanceof Integer integer) { out.writeByte(1); out.writeInt(integer); }
        else if (value instanceof String string) { out.writeByte(2); out.writeUTF(string); }
        else if (value instanceof Boolean bool) { out.writeByte(3); out.writeBoolean(bool); }
        else if (value instanceof Double decimal) { out.writeByte(4); out.writeDouble(decimal); }
        else throw new IllegalArgumentException("Unsupported component value type: " + value.getClass().getName());
    }
    private static Object readValue(DataInputStream in, Class<?> expected) throws IOException {
        Object value = switch (in.readByte()) { case 1 -> in.readInt(); case 2 -> in.readUTF(); case 3 -> in.readBoolean(); case 4 -> in.readDouble(); default -> throw new IllegalArgumentException("Unsupported component value tag"); };
        if (!expected.isInstance(value)) throw new IllegalArgumentException("Component binary type does not match " + expected.getName());
        return value;
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static FluidStack putUnchecked(FluidStack stack, FluidComponentType component, Object value) { return stack.with(component, value); }
}
