package org.valkyrienskies.rumbleport.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.valkyrienskies.rumbleport.RumblePortMod;
import org.valkyrienskies.rumbleport.forge.client.RumblePortForgeClient;

@Mod(RumblePortMod.MOD_ID)
public final class RumblePortForge {
    public RumblePortForge() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(RumblePortMod.MOD_ID, modEventBus);

        modEventBus.addListener(this::init);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(RumblePortForgeClient::clientInit);
        }

        RumblePortMod.init();
    }

    private void init(FMLCommonSetupEvent event) {
        // Put anything initialized on forge-side here.
    }
}
