package dev.tako.libuid.config;

import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.logging.Logger;

                                                                    
public final class LibuidConfigRegistrations {
    private static LibuidFluidParser fluidParser;
    private static LibuidFluidTagParser fluidTagParser;
    private static boolean registered;

    private LibuidConfigRegistrations() {}

    public static synchronized boolean register(Logger logger) {
        if (registered) return true;
        if (fluidParser == null) fluidParser = new LibuidFluidParser(logger);
        if (fluidTagParser == null) fluidTagParser = new LibuidFluidTagParser(logger);
        try {
            if (!CraftEngine.instance().packManager().registerConfigSectionParser(fluidParser)) return false;
            if (!CraftEngine.instance().packManager().registerConfigSectionParser(fluidTagParser)) return false;
            registered = true;
            return true;
        } catch (Throwable failure) {
            logger.severe("Could not register Libuid config parsers: " + failure.getMessage());
            return false;
        }
    }
}
