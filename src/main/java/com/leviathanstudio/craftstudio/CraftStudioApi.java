package com.leviathanstudio.craftstudio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.leviathanstudio.craftstudio.client.registry.CSModelRegistry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

 


















@Mod(CraftStudioApi.MOD_ID)
public class CraftStudioApi
{
    public static final String MOD_ID = "craftstudioapi";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CraftStudioApi(IEventBus modEventBus) {
         
         
         
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(CSModelRegistry.INSTANCE);
        }
    }
}
