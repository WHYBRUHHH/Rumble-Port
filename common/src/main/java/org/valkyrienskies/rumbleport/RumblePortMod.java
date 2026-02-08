package org.valkyrienskies.rumbleport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valkyrienskies.rumbleport.item.ModItems;
import org.valkyrienskies.rumbleport.server.RumblePortCommands;

/**
 * The common static object that represents the mod. Referenced by both fabric and forge for initialization.
 */
public final class RumblePortMod {
    public static final String MOD_ID = "rumble-port";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private RumblePortMod() {
    }

    public static void init() {
        ModItems.register();
        RumblePortCommands.register();
        LOGGER.info("Rumble Port Initialised");
    }
}
