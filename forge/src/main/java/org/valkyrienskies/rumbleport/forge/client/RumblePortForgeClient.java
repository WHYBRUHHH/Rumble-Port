package org.valkyrienskies.rumbleport.forge.client;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.valkyrienskies.rumbleport.client.RumblePortModClient;

public final class RumblePortForgeClient {
    private RumblePortForgeClient() {
    }

    public static void clientInit(FMLClientSetupEvent event) {
        RumblePortModClient.initClient();
    }
}
