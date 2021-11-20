package com.nxtdelivery.quickStats.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LocrawUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static String gameType;
    public static boolean lobby = false;

    public LocrawUtil() {
        register();
    }

    @EventHandler()
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler()
    private void destroy() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @EventHandler()
    public void send() {
        MinecraftForge.EVENT_BUS.register(this);
        try {
            if (GUIConfig.autoGame && mc.getCurrentServerData().serverIP.contains("hypixel")) {
                mc.thePlayer.sendChatMessage("/locraw");
            } else {
                gameType = "DEFAULT";
            }
        } catch (Exception e) {
            QuickStats.LOGGER
                    .error("couldn't sent locraw message. this usually occurs when being kicked from the server.");
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void getGameType(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().contains("{")) {
            if (!GUIConfig.locrawComp) {
                event.setCanceled(true);
            }
            try {
                JsonObject jsonObject = new JsonParser().parse(event.message.getUnformattedText()).getAsJsonObject();
                try {
                    gameType = jsonObject.get("mode").toString();
                    lobby = false;
                } catch (Exception e) {     // this means we are in a lobby
                    try {
                        gameType = jsonObject.get("gametype").toString();
                        lobby = true;
                        if (GUIConfig.debugMode) {
                            QuickStats.LOGGER.info("detected this as a lobby");
                        }
                    } catch (Exception e1) { // catch if errors/in limbo
                        if (GUIConfig.debugMode) {
                            e.printStackTrace();
                        }
                        lobby = false;
                        gameType = "LIMBO";
                    }
                }

                QuickStats.LOGGER.info(gameType);
            } catch (Exception e) {
                if (GUIConfig.debugMode) {
                    e.printStackTrace();
                }
                gameType = "DEFAULT";
            }
        }
    }
}
