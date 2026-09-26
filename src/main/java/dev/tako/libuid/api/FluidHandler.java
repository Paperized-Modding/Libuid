package dev.tako.libuid.api;

                                                                             
public interface FluidHandler {
    int tanks();

                                                                                             
    FluidStack fluidInTank(int tank);

    int tankCapacity(int tank);

                                                                             
    boolean isFluidValid(int tank, FluidStack stack);

                                                                                           
    int fill(FluidStack resource, FluidAction action);

                                                                                     
    FluidStack drain(FluidStack resource, FluidAction action);

                                                                                              
    FluidStack drain(int maxDrain, FluidAction action);
}
