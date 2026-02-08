package org.valkyrienskies.rumbleport.fabric;

import net.fabricmc.api.ModInitializer;
import org.valkyrienskies.rumbleport.RumblePortMod;

/**
 * The fabric-side initializer for the mod. Used for fabric-platform-specific code.
 */
public final class RumblePortFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RumblePortMod.init();
    }
}
