package org.valkyrienskies.vs_template.fabric.client

import net.fabricmc.api.ClientModInitializer
import org.valkyrienskies.vs_template.client.VSTemplateModClient

/**
 * The fabric-side client initializer for the mod. Used for fabric-platform-specific code that runs on the client exclusively.
 */
class VSTemplateModFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Put anything initialized on fabric-side client here.
        VSTemplateModClient.initClient()
    }
}
