package com.nxtdelivery.quickStats.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nxtdelivery.quickStats.QuickStats;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LocrawUtil {
	private static Minecraft mc = Minecraft.getMinecraft();
	public static String gameType;

	@EventHandler()
	public void regist() {
		MinecraftForge.EVENT_BUS.register(this);
		try {
			mc.thePlayer.sendChatMessage("/locraw");
		} catch (Exception e) {
			QuickStats.LOGGER
					.error("couldn't sent locraw message. this usually occours when being kicked from the server.");
		}
	}

	@EventHandler()
	private void destroy() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	/**
	 * Get the current game type using /locraw.
	 * 
	 * @param event
	 * @return current game
	 */
	public String getGameType(ClientChatReceivedEvent event) {
		if (event.message.getUnformattedText().contains("{")) {
			//System.out.println("locraw util found msg");
			event.setCanceled(true);
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
				jsonObject = null;
				return gameType;
			} catch (Exception e) {
				e.printStackTrace();
				// gameType = "solo_insane";
				return "solo_insane";
			}
		} else {
			return "e";
		}
	}
}
