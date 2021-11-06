/* Changelog v1.5
 *  cleanup getEntity
 *  changes to API utility
 *  added party utility system
 *  added tab completion options to command
 *  bug fixes
 *  code cleanup
 */

package com.nxtdelivery.quickStats;

import com.nxtdelivery.quickStats.command.StatsCommand;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.gui.GUIStats;
import com.nxtdelivery.quickStats.util.GetEntity;
import com.nxtdelivery.quickStats.util.TickDelay;
import com.nxtdelivery.quickStats.util.UpdateChecker;
import gg.essential.universal.wrappers.message.UTextComponent;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class QuickStats {

    @Mod.Instance("qSts") // variables and things
    public static QuickStats instance;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private KeyBinding statsKey;
    public static final Logger LOGGER = LogManager.getLogger("QuickStats");
    public static boolean updateCheck;
    public static boolean betaFlag = false;
    public static boolean locraw = false;
    public static boolean corrupt = false;
    boolean set = false;
    String partySet;


    @EventHandler()
    public void init(FMLInitializationEvent event) {
        LOGGER.info("reading config...");
        try {
            GUIConfig.INSTANCE.preload();
            LOGGER.info("config read was successful");
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            corrupt = true;
            LOGGER.error("Config failed to read. File has been reset. If you just reset your config, ignore this message.");
        }
        LOGGER.info("attempting to check update status...");
        updateCheck = UpdateChecker.updateNeeded(Reference.VERSION);
        LOGGER.info("registering settings...");
        statsKey = new KeyBinding("Get Stats", GUIConfig.key, "QuickStats");
        ClientRegistry.registerKeyBinding(statsKey);
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new StatsCommand());
        LOGGER.debug(instance.toString());        // please stop moaning at me intellij
        LOGGER.info("Complete! QuickStats loaded successfully.");
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() == statsKey.getKeyCode() && GUIConfig.modEnabled) {
            if (GUIConfig.key != statsKey.getKeyCode()) {                // will write new key code if the player changed it in settings
                LOGGER.warn("Key code from config (" + GUIConfig.key + ") differs to key code just used! (" + statsKey.getKeyCode() + ") writing new to config file...");
                GUIConfig.key = Keyboard.getEventKey();
                GUIConfig.INSTANCE.markDirty();
                GUIConfig.INSTANCE.writeData();
            }
            if (Keyboard.getEventKeyState()) {
                try {
                    Entity entity = GetEntity.get(0);
                    if (entity instanceof EntityPlayer) {
                        new GUIStats(entity.getName());
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent event) {
        // System.out.println(event.message.getUnformattedText());
        if (GUIConfig.autoGetAPI) {
            try {
                if (event.message.getUnformattedText().contains("Your new API key is")) {
                    String apiMessage = event.message.getUnformattedText();
                    String apiKey = apiMessage.substring(20);
                    //System.out.println(apiKey);
                    LOGGER.info("got API key from message: " + apiKey + ". writing and reloading config...");
                    GUIConfig.apiKey = apiKey;
                    GUIConfig.INSTANCE.markDirty();
                    GUIConfig.INSTANCE.writeData();
                    new TickDelay(() -> mc.thePlayer.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.DARK_GRAY + "[QuickStats] Grabbed and set your API key. The mod is now ready to use!")),
                            5);
                    mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
                }
            } catch (Exception e) {
                if (GUIConfig.debugMode) {
                    e.printStackTrace();
                }
            }
        }
        if (GUIConfig.doPartyDetection) {
            try {
                if (GUIConfig.doPartyDetectionPLUS) {
                    if (event.message.getUnformattedText().contains("say") && getUsernameFromChat(event.message.getUnformattedText()).equals(mc.thePlayer.getName())) {
                        try {
                            String unformatted = UTextComponent.Companion.stripFormatting(event.message.getUnformattedText());
                            partySet = StringUtils.substringAfter(unformatted, "say ");
                            set = true;
                            if (partySet.contains("my name")) {
                                partySet = null;
                                set = false;
                            }
                            if (GUIConfig.debugMode) {
                                LOGGER.info(partySet);
                            }
                        } catch (Exception e) {
                            if (GUIConfig.debugMode) {
                                e.printStackTrace();
                                set = false;
                            }
                        }
                    }
                }
                if (event.message.getUnformattedText().contains(mc.thePlayer.getName())) {
                    //System.out.println(event.message.getUnformattedText());
                    if (!event.message.getUnformattedText().contains("lobby!")) {
                        String username = getUsernameFromChat(event.message.getUnformattedText());
                        if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                            new GUIStats(username);
                        }
                    }
                }
                if (set && event.message.getUnformattedText().contains(partySet)) {
                    String username = getUsernameFromChat(event.message.getUnformattedText());
                    if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                        new GUIStats(username);
                    }
                }
            } catch (Exception e) {
                if (GUIConfig.debugMode) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public String getUsernameFromChat(String message) {
        try {
            String unformatted = UTextComponent.Companion.stripFormatting(message);
            return unformatted.substring(unformatted.lastIndexOf("]") + 2, unformatted.lastIndexOf(":"));
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            return message;
        }
    }


    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        try {
            if (mc.getCurrentServerData().serverIP.contains("hypixel")) {
                locraw = true;
            }
        } catch (Exception e) {
            // if(GUIConfig.debugMode) {e.printStackTrace();}
        }
        if (updateCheck && GUIConfig.sendUp) {
            new TickDelay(this::sendUpdateMessage, 20);
            updateCheck = false;
        }
        if (Reference.VERSION.contains("beta") && betaFlag) {
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
        if (corrupt) {
            new TickDelay(() -> sendMessages("",
                    "[QuickStats] An error occurred while trying to read your config file. You will have to reset it.",
                    "[QuickStats] If you just reset your configuration file, ignore this message."), 20);
            corrupt = false;
        }
    }

    private void sendMessages(String... messages) {
        try {
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
            for (String message : messages) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY + message));
            }
        } catch (NullPointerException e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            LOGGER.error("skipping new message, bad world return!");
        }
    }

    private void sendUpdateMessage() {
        try {
            IChatComponent comp = new ChatComponentText("Click here to update it!");
            ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, Reference.URL));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText(EnumChatFormatting.DARK_GRAY + Reference.URL)));
            style.setColor(EnumChatFormatting.DARK_GRAY);
            style.setUnderlined(true);
            comp.setChatStyle(style);
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_GRAY + "--------------------------------------"));
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                    + ("A newer version of QuickStats is available! (" + UpdateChecker.latestVersion + ")")));
            mc.thePlayer.addChatMessage(comp);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_GRAY + "--------------------------------------"));
        } catch (NullPointerException e) {
            LOGGER.fatal(e);
            LOGGER.error("skipping update message, bad world return!");
        }
    }

}
