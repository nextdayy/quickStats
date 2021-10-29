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

	@EventHandler()
	public void register() {
		MinecraftForge.EVENT_BUS.register(this);
		try {
			if (GUIConfig.autoGame) {
				mc.thePlayer.sendChatMessage("/locraw");
			} else {
				gameType = "";
			}
		} catch (Exception e) {
			QuickStats.LOGGER
					.error("couldn't sent locraw message. this usually occurs when being kicked from the server.");
		}
	}

	@EventHandler()
	private void destroy() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	

	@SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled = true)
	public String getGameType(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().contains("{")) {
			// System.out.println("locraw util found msg");
			if (!GUIConfig.locrawComp) {
				event.setCanceled(true);
			}
			try {
				JsonObject jsonObject = new JsonParser().parse(event.message.getUnformattedText()).getAsJsonObject();
				destroy();
				try {
					gameType = jsonObject.get("mode").toString();
				} catch (Exception e) {
					try {
						gameType = jsonObject.get("gametype").toString();
					} catch (Exception e1) { // catch if in limbo
						gameType = "limbo";
						return "limbo";
					}
				}
				QuickStats.LOGGER.info(gameType);
				return gameType;
			} catch (Exception e) {
				if (GUIConfig.debugMode) {
					e.printStackTrace();
				}
				// gameType = "solo_insane";
				return "solo_insane";
			}
		} else {
			return "e";
		}
	}
}
