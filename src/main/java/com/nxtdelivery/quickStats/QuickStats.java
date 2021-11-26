/* Changelog v@VER@
 *  fix the mod working (or rather not working) on other servers
 *  optimize the locraw utility
 *  optimize the party detection in hope of making it more efficient
 *  fix the auto game being off actually turning off
 *  stopped showing stats if player is NPC
 *  changed color of text to be easier to read
 *  added window speed modification
 *  fixed version checker sending messages
 *  rewrote stats to be null-protected
 *  added animations to the window
 *  added hash checker (beta)
 *  bug fixes
 *  code cleanup
 */

package com.nxtdelivery.quickStats;

import com.nxtdelivery.quickStats.command.StatsCommand;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.gui.GUIStats;
import com.nxtdelivery.quickStats.util.GetEntity;
import com.nxtdelivery.quickStats.util.LocrawUtil;
import com.nxtdelivery.quickStats.util.TickDelay;
import com.nxtdelivery.quickStats.util.UpdateChecker;
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
    public static final Logger LOGGER = LogManager.getLogger(Reference.NAME);
    public static boolean updateCheck;
    public static boolean betaFlag = true;
    public static boolean locraw = false;
    public static boolean corrupt = false;
    public static LocrawUtil LocInst;
    public static GUIStats GuiInst;
    public static boolean onHypixel = false;
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
        LOGGER.info("attempting to check update status and authenticity of mod...");
        updateCheck = UpdateChecker.checkUpdate(Reference.VERSION);
        //AuthChecker.checkAuth(JarName);  // TODO
        LOGGER.info("registering settings...");
        statsKey = new KeyBinding("Get Stats", GUIConfig.key, "QuickStats");
        ClientRegistry.registerKeyBinding(statsKey);
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new StatsCommand());
        LocInst = new LocrawUtil();
        GuiInst = new GUIStats();
        locraw = true;
        LOGGER.debug(instance.toString());        // please stop moaning at me intellij
        LOGGER.info("Complete! QuickStats loaded successfully.");
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (onHypixel || GUIConfig.otherServer) {
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
                            if (entity.getName() == null || entity.getName().equals("")) {
                                return;
                            }
                            if(onHypixel) {
                                if (entity.getDisplayName().getUnformattedText().startsWith("\u00A78[NPC]") || !entity.getDisplayName().getUnformattedText().startsWith("\u00A7")) {      // npc test
                                    return;
                                }
                            }
                            GuiInst.showGUI(entity.getName());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent event) {
        // System.out.println(event.message.getUnformattedText());
        if (onHypixel) {
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
                                Reference.COLOR + "[QuickStats] Grabbed and set your API key. The mod is now ready to use!")),
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
                if (QuickStats.locraw) {
                    QuickStats.locraw = false;
                    LocInst.send();
                }
                if (LocrawUtil.lobby) {
                    try {
                        if (event.message.getUnformattedText().contains("Party ") || event.message.getUnformattedText().contains("lobby!")) {
                            return;
                        }
                        if (event.message.getUnformattedText().contains(mc.thePlayer.getName())) {
                            String username = getUsernameFromChat(event.message.getUnformattedText());
                            if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                                event.setCanceled(true);
                                StringBuilder sb = new StringBuilder(event.message.getUnformattedText());
                                sb.insert(event.message.getUnformattedText().indexOf(mc.thePlayer.getName()), "\u00A7l");
                                mc.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                                GuiInst.showGUI(username);
                            }
                        }

                        if (GUIConfig.doPartyDetectionPLUS) {
                            if (event.message.getUnformattedText().contains("say")) {
                                if (getUsernameFromChat(event.message.getUnformattedText()).equals(mc.thePlayer.getName())) {
                                    try {
                                        String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
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
                                if (set) {
                                    if (event.message.getUnformattedText().contains(partySet)) {
                                        String username = getUsernameFromChat(event.message.getUnformattedText());
                                        if (!username.equalsIgnoreCase(mc.thePlayer.getName())) {
                                            event.setCanceled(true);
                                            StringBuilder sb = new StringBuilder(event.message.getUnformattedText());
                                            sb.insert(event.message.getUnformattedText().indexOf(partySet), "\u00A7l");
                                            mc.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
                                            GuiInst.showGUI(username);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (GUIConfig.debugMode) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public String getUsernameFromChat(String message) {
        try {
            String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(message);
            return unformatted.substring(unformatted.lastIndexOf("]") + 2, unformatted.lastIndexOf(":"));
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @SubscribeEvent
    @SuppressWarnings({"ConstantConditions", "MismatchedStringCase"})
    public void onWorldLoad(WorldEvent.Load event) {
        try {
            if (mc.getCurrentServerData().serverIP.contains("hypixel")) {
                if (GUIConfig.debugMode) {
                    LOGGER.info("on Hypixel!");
                }
                locraw = true;
                onHypixel = true;
                LocrawUtil.lobby = false;
            } else {
                onHypixel = false;
                LocrawUtil.lobby = false;
                locraw = false;
            }
        } catch (Exception e) {
            // if(GUIConfig.debugMode) {e.printStackTrace();}
        }
        if (updateCheck && GUIConfig.sendUp && event.world.isRemote) {
            new TickDelay(this::sendUpdateMessage, 20);
            updateCheck = false;
        }
        if (Reference.VERSION.contains("beta") && betaFlag && event.world.isRemote) {
            try {
                new TickDelay(() -> sendMessages("",
                        "Beta build has been detected (ver. " + Reference.VERSION + ")",
                        "Note that some features might be unstable! Use at your own risk!"), 20);
                betaFlag = false;
            } catch (Exception e) {
                betaFlag = true;
                if (GUIConfig.debugMode) {
                    e.printStackTrace();
                }
                LOGGER.error("skipping beta message, bad world return!");
            }
        }
        if (corrupt) {
            try {
                new TickDelay(() -> sendMessages("",
                        "An error occurred while trying to read your config file. You will have to reset it.",
                        "If you just reset your configuration file, ignore this message."), 20);
                corrupt = false;
            } catch (Exception e) {
                if (GUIConfig.debugMode) {
                    e.printStackTrace();
                }
                LOGGER.error("skipping corrupt message, bad world return!");
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "MismatchedStringCase"})
    public static void sendMessages(String... messages) {
        try {
            for (String message : messages) {
                mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR + "[" + Reference.NAME + "] " + message));
            }
        } catch (Exception e) {
            LOGGER.error("Didn't send message: " + e.getMessage());
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            if (Reference.VERSION.contains("beta")) {
                betaFlag = true;
            }
        }
    }

    private void sendUpdateMessage() {
        try {
            IChatComponent comp = new ChatComponentText("Click here to update it!");
            ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_URL, Reference.URL));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText(Reference.COLOR + Reference.URL)));
            style.setColor(Reference.COLOR);
            style.setUnderlined(true);
            comp.setChatStyle(style);
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    Reference.COLOR + "--------------------------------------"));
            mc.thePlayer.addChatMessage(new ChatComponentText(Reference.COLOR
                    + ("A newer version of " + Reference.NAME + " is available! (" + UpdateChecker.latestVersion + ")")));
            mc.thePlayer.addChatMessage(comp);
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    Reference.COLOR + "--------------------------------------"));
        } catch (NullPointerException e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            updateCheck = true;
            LOGGER.error("skipping update message, bad world return!");
        }
    }

}
