package dev.tako.libuid.api;

import java.util.Objects;

   
                                                                                       
  
                                                                                                  
                                                     
  
                                                                                                   
                                                                                                  
                                                                      
   
public final class FluidStack {
    public static final int BUCKET_VOLUME = 1_000;
    public static final int BOTTLE_VOLUME = 250;

                                                                        
    public static final FluidStack EMPTY = new FluidStack();

    private final FluidType type;
    private final int amount;
    private final FluidComponentMap components;

    private FluidStack() {
        this.type = null;
        this.amount = 0;
        this.components = FluidComponentMap.empty();
    }

    private FluidStack(FluidType type, int amount, FluidComponentMap components) {
        this.type = Objects.requireNonNull(type, "type");
        if (amount <= 0) throw new IllegalArgumentException("Fluid stack amount must be positive");
        this.amount = amount;
        this.components = Objects.requireNonNull(components, "components");
    }

                                                                    
    public static FluidStack of(FluidType type, int amount) {
        return new FluidStack(type, amount, FluidComponentMap.empty());
    }

                                                                                            
    public static FluidStack ofAllowEmpty(FluidType type, int amount) {
        return type == null || amount <= 0 ? EMPTY : of(type, amount);
    }

                                                                                      
    public FluidType type() { return type; }

    public int amount() { return amount; }
    public FluidComponentMap components() { return components; }
    public boolean isEmpty() { return type == null || amount <= 0; }

                                                                                                               
    public ResourceKey fluidKey() { return type == null ? null : type.key(); }

    public FluidStack copyWithAmount(int amount) {
        return isEmpty() ? EMPTY : ofAllowEmptyWithComponents(type, amount, components);
    }

                                                                                                          
    public FluidStack grow(int delta) { return copyWithAmount(amount + delta); }

                                                                                                          
    public FluidStack shrink(int delta) { return copyWithAmount(amount - delta); }

                                                                       
    public FluidStack limitSize(int maxAmount) { return copyWithAmount(Math.min(amount, maxAmount)); }

                                                                  
    public Split split(int toTake) {
        if (isEmpty() || toTake <= 0) return new Split(EMPTY, this);
        int taken = Math.min(amount, toTake);
        return new Split(copyWithAmount(taken), copyWithAmount(amount - taken));
    }

                                                          
    public record Split(FluidStack taken, FluidStack remainder) {}

    public <T> T get(FluidComponentType<T> component) { return components.get(component); }
    public <T> T getOrDefault(FluidComponentType<T> component, T fallback) { return components.getOrDefault(component, fallback); }

    public <T> FluidStack with(FluidComponentType<T> component, T value) {
        if (isEmpty()) throw new IllegalStateException("Cannot attach components to an empty fluid stack");
        return new FluidStack(type, amount, components.with(component, value));
    }

    public FluidStack without(FluidComponentType<?> component) {
        return isEmpty() ? EMPTY : new FluidStack(type, amount, components.without(component));
    }

                                                                             
    public static boolean matches(FluidStack first, FluidStack second) {
        return isSameFluidSameComponents(first, second) && first.amount() == second.amount();
    }

                                                                                      
    public static boolean isSameFluid(FluidStack first, FluidStack second) {
        if (first.isEmpty() || second.isEmpty()) return first.isEmpty() && second.isEmpty();
        return first.fluidKey().equals(second.fluidKey());
    }

                                                                                      
    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        return isSameFluid(first, second) && first.components().equals(second.components());
    }

                                                                                                 
    public int hashFluidAndComponents() {
        return isEmpty() ? 0 : Objects.hash(fluidKey(), components);
    }

                                                               
    public boolean is(ResourceKey tag) {
        return !isEmpty() && FluidTagRegistry.contains(tag, fluidKey());
    }

    @Override
    public String toString() {
        return isEmpty() ? "FluidStack.EMPTY" : amount + "mB " + fluidKey();
    }

    private static FluidStack ofAllowEmptyWithComponents(FluidType type, int amount, FluidComponentMap components) {
        return amount <= 0 ? EMPTY : new FluidStack(type, amount, components);
    }
}
