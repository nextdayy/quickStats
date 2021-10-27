/* Changelog v1.4
 *  - complete rewrite of configuration utility
 *  - support for window customisation
 *  - bug fixes
 *  - code cleanup
 */

package com.nxtdelivery.quickStats;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import com.nxtdelivery.quickStats.command.StatsCommand;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.gui.GUIStats;
import com.nxtdelivery.quickStats.util.*;

import gg.essential.vigilance.Vigilance;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class QuickStats {

	@Mod.Instance("qSts") // variables and things
	public static QuickStats instance;
	private static Minecraft mc = Minecraft.getMinecraft();
	private KeyBinding statsKey;
	public static final Logger LOGGER = LogManager.getLogger("QuickStats");
	public static boolean updateCheck;
	public static boolean betaFlag = false;
	public static boolean locraw = false;
	public static boolean corrupt = false;

	public boolean registerBus(EventBus bus, LoadController controller) { // register mod to the bus
		bus.register(this);
		return true;
	}

	@EventHandler()
	public void init(FMLInitializationEvent event) {
		try {
			Vigilance.initialize();
	        GUIConfig.INSTANCE.preload();
		} catch (Exception e) {e.printStackTrace(); corrupt = true;}
		LOGGER.info("attempting to check update status...");
		updateCheck = UpdateChecker.updateNeeded(Reference.VERSION);
		LOGGER.info("registering settings...");
		statsKey = new KeyBinding("Get Stats", GUIConfig.key, "QuickStats");
		FMLCommonHandler.instance().bus().register(this);
		ClientRegistry.registerKeyBinding(statsKey);
		MinecraftForge.EVENT_BUS.register(this);
		ClientCommandHandler.instance.registerCommand(new StatsCommand());
		LOGGER.info("Complete! QuickStats loaded successfully.");
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (Keyboard.getEventKey() == statsKey.getKeyCode() && GUIConfig.modEnabled == true) {
			if(GUIConfig.key != statsKey.getKeyCode() ) {				// will write new key code if the player changed it in settings
	    		LOGGER.warn("Key code from config (" + GUIConfig.key + ") differs to key code just used! (" + statsKey.getKeyCode() + ") writing new to config file...");
	    		Integer key = (Integer) Keyboard.getEventKey();
	    		GUIConfig.key = key;
	    		GUIConfig.INSTANCE.markDirty();
	    		GUIConfig.INSTANCE.writeData();
	    	}
			if (Keyboard.getEventKeyState()) {
				try {
					Entity entity = GetEntity.get(0);
					if (entity instanceof EntityPlayer) {
						GUIStats gui = new GUIStats(entity.getName());
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@SubscribeEvent
	public void onChatRecieve(ClientChatReceivedEvent event) {
		// System.out.println(event.message.getUnformattedText());
		if(GUIConfig.autoGetAPI) {
			try {
				if (event.message.getUnformattedText().contains("Your new API key is")) {
					String apiMessage = event.message.getUnformattedText();
					String apiKey = apiMessage.substring(20, apiMessage.length());
					//System.out.println(apiKey);
					LOGGER.info("got API key from message: " + apiKey + ". writing and reloading config...");
					GUIConfig.apiKey = apiKey;
					GUIConfig.INSTANCE.markDirty();
		    		GUIConfig.INSTANCE.writeData();
					new TickDelay(() -> mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(
							EnumChatFormatting.DARK_GRAY + "[QuickStats] Grabbed and set your API key. The mod is now ready to use!")),
							5);
					mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		try {
			if (mc.getCurrentServerData().serverIP.contains("hypixel")) {
				locraw = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		if (updateCheck == true && GUIConfig.sendUp) {
			new TickDelay(() -> sendUpdateMessage(), 20);
			updateCheck = false;
		}
		if (Reference.VERSION.contains("beta") && betaFlag == true) {
			try {
				new TickDelay(() -> sendMessages("",
						"[QuickStats] Beta build has been detected (ver. " + Reference.VERSION + ")",
						"[QuickStats] Note that some features might be unstable! Use at your own risk!"), 20);
				betaFlag = false;
				return;
			} catch (NullPointerException e) {
				LOGGER.fatal(e);
				LOGGER.error("skipping beta message, bad world return!");
			}
		}
	}

	private void sendMessages(String... messages) {
        try {
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
            for (String message : messages) {
                mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(EnumChatFormatting.DARK_GRAY + message));
            }
        } catch (NullPointerException e) {
            LOGGER.fatal(e);
            LOGGER.error("skipping new message, bad world return!");
        }
    }

	private Runnable sendUpdateMessage() {
		try {
			IChatComponent comp = new ChatComponentText("Click here to update it!");
			ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, Reference.URL));
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					(IChatComponent) new ChatComponentText(EnumChatFormatting.DARK_GRAY + Reference.URL)));
			style.setColor(EnumChatFormatting.DARK_GRAY);
			style.setUnderlined(true);
			comp.setChatStyle(style);
			mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
			mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(
					EnumChatFormatting.DARK_GRAY + "--------------------------------------"));
			mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(EnumChatFormatting.DARK_GRAY
					+ ("A newer version of QuickStats is available! (" + UpdateChecker.latestVersion + ")")));
			mc.thePlayer.addChatMessage(comp);
			mc.thePlayer.addChatMessage((IChatComponent) new ChatComponentText(
					EnumChatFormatting.DARK_GRAY + "--------------------------------------"));
		} catch (NullPointerException e) {
			LOGGER.fatal(e);
			LOGGER.error("skipping update message, bad world return!");
		}
		return null;
	}

}
