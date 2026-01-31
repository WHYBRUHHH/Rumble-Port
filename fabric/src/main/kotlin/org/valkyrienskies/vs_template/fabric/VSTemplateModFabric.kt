package org.valkyrienskies.vs_template.fabric

import net.fabricmc.api.ModInitializer
import org.valkyrienskies.vs_template.RumblePortMod

/**
 * The fabric-side initializer for the mod. Used for fabric-platform-specific code.
 */
class VSTemplateModFabric : ModInitializer {
    override fun onInitialize() {
        // Put anything initialized on fabric-side here, such as platform-specific registries.
        RumblePortMod.init()
    }
}
