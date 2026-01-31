package org.valkyrienskies.vs_template.forge.client

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.valkyrienskies.vs_template.client.VSTemplateModClient

class VSTemplateModForgeClient {
    companion object {
        @JvmStatic
        fun clientInit(event: FMLClientSetupEvent) {
            // Put anything initialized on forge-side client here.
            VSTemplateModClient.initClient()
        }
    }
}
