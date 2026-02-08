package org.valkyrienskies.rumbleport.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import org.valkyrienskies.rumbleport.client.RumblePortModClient;

/**
 * The fabric-side client initializer for the mod. Used for fabric-platform-specific code that runs on the client exclusively.
 */
public final class RumblePortFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RumblePortModClient.initClient();
    }
}
