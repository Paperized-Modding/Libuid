package dev.tako.libuid.api;

import java.util.Objects;

                                                                            
public record SizedFluidIngredient(FluidIngredient ingredient, int amount) {
    public SizedFluidIngredient {
        Objects.requireNonNull(ingredient, "ingredient");
        if (amount <= 0) throw new IllegalArgumentException("Sized fluid ingredient amount must be positive");
    }

                                                                                       
    public boolean test(FluidStack stack) {
        return ingredient.test(stack) && stack.amount() >= amount;
    }
}
