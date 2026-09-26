package dev.tako.libuid.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

   
                                                                                            
  
                                                                           
   
public interface FluidIngredient extends Predicate<FluidStack> {

                                         
    static FluidIngredient single(ResourceKey fluid) {
        Objects.requireNonNull(fluid, "fluid");
        return new FluidIngredient() {
            @Override public boolean test(FluidStack stack) {
                return !stack.isEmpty() && fluid.equals(stack.fluidKey());
            }

            @Override public List<FluidType> candidates() {
                return FluidRegistry.get(fluid).map(List::of).orElseGet(List::of);
            }

            @Override public String toString() { return fluid.toString(); }
        };
    }

                                                  
    static FluidIngredient tag(ResourceKey tag) {
        Objects.requireNonNull(tag, "tag");
        return new FluidIngredient() {
            @Override public boolean test(FluidStack stack) {
                return stack.is(tag);
            }

            @Override public List<FluidType> candidates() {
                List<FluidType> result = new ArrayList<>();
                for (ResourceKey key : FluidTagRegistry.members(tag)) {
                    FluidRegistry.get(key).ifPresent(result::add);
                }
                return List.copyOf(result);
            }

            @Override public String toString() { return '#' + tag.toString(); }
        };
    }

                                                           
    static FluidIngredient anyOf(FluidIngredient... parts) {
        Objects.requireNonNull(parts, "parts");
        if (parts.length == 0) return empty();
        return new FluidIngredient() {
            @Override public boolean test(FluidStack stack) {
                for (FluidIngredient part : parts) if (part.test(stack)) return true;
                return false;
            }

            @Override public List<FluidType> candidates() {
                List<FluidType> result = new ArrayList<>();
                for (FluidIngredient part : parts) result.addAll(part.candidates());
                return List.copyOf(result);
            }

            @Override public String toString() {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) builder.append(" | ");
                    builder.append(parts[i]);
                }
                return builder.toString();
            }
        };
    }

                           
    static FluidIngredient empty() {
        return new FluidIngredient() {
            @Override public boolean test(FluidStack stack) { return false; }
            @Override public List<FluidType> candidates() { return List.of(); }
            @Override public String toString() { return "empty"; }
        };
    }

                                                                                                                
    static FluidIngredient parse(Object raw) {
        if (raw instanceof String string) {
            if (string.isBlank()) throw new IllegalArgumentException("Fluid ingredient must not be blank");
            return string.startsWith("#") ? tag(ResourceKey.of(string.substring(1))) : single(ResourceKey.of(string));
        }
        if (raw instanceof Map<?, ?> map) {
            Object fluid = map.get("fluid");
            Object tag = map.get("tag");
            if (fluid == null && tag == null) throw new IllegalArgumentException("Fluid ingredient map requires 'fluid' or 'tag'");
            if (fluid != null && tag != null) throw new IllegalArgumentException("Fluid ingredient map must not define both 'fluid' and 'tag'");
            if (fluid != null) {
                if (!(fluid instanceof String id)) throw new IllegalArgumentException("Fluid ingredient 'fluid' must be a string id");
                return single(ResourceKey.of(id));
            }
            if (!(tag instanceof String tagId)) throw new IllegalArgumentException("Fluid ingredient 'tag' must be a string id");
            return tag(ResourceKey.of(tagId));
        }
        if (raw instanceof Collection<?> list) {
            if (list.isEmpty()) throw new IllegalArgumentException("Fluid ingredient list must not be empty");
            return anyOf(list.stream().map(FluidIngredient::parse).toArray(FluidIngredient[]::new));
        }
        throw new IllegalArgumentException("Unsupported fluid ingredient value: " + raw.getClass().getSimpleName());
    }

                                                                                                     
    static SizedFluidIngredient parseSized(Object raw, int defaultAmount) {
        if (defaultAmount <= 0) throw new IllegalArgumentException("Default fluid amount must be positive");
        if (raw instanceof Map<?, ?> map && map.get("amount") != null) {
            Object amount = map.get("amount");
            if (!(amount instanceof Number number)) throw new IllegalArgumentException("Fluid ingredient 'amount' must be numeric");
            if (number.intValue() <= 0) throw new IllegalArgumentException("Fluid ingredient amount must be positive");
            return new SizedFluidIngredient(parse(map), number.intValue());
        }
        return new SizedFluidIngredient(parse(raw), defaultAmount);
    }

                                                                                      
    List<FluidType> candidates();
}
