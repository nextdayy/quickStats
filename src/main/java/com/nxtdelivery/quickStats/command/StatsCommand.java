package com.nxtdelivery.quickStats.command;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.Reference;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ReportedException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.command.CommandBase.getListOfStringsMatchingLastWord;

public class StatsCommand implements ICommand {

    private final List<String> aliases;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public StatsCommand() {
        aliases = new ArrayList<>();
        aliases.add("qsts");
        aliases.add("quickstats");
        aliases.add("qs");
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "quickStats";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "quickStats <>";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            switch (args[0]) {
                case "configure":
                case "config":
                case "cfg":
                    try {
                        new TickDelay(() -> mc.displayGuiScreen(GUIConfig.INSTANCE.gui()), 1);
                    } catch (Exception e) {
                        if (GUIConfig.debugMode) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "reload":
                    QuickStats.LOGGER.info("Reloading config and version checker...");
                    QuickStats.sendMessages("Reloading!");
                    GUIConfig.INSTANCE.initialize();
                    QuickStats.updateCheck = UpdateChecker.checkUpdate(Reference.VERSION);
                    AuthChecker.checkAuth(QuickStats.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    QuickStats.sendMessages("Reloaded! Re-log and check logs for more information.");
                    Minecraft.getMinecraft().thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
                    break;
                case "api":
                    GUIConfig.apiKey = args[1];
                    GUIConfig.INSTANCE.markDirty();
                    GUIConfig.INSTANCE.writeData();
                    QuickStats.sendMessages("set your API key as: " + args[1] + ".");
                    break;
                case "testLoc":
                    QuickStats.sendMessages("Testing locraw function...");
                    QuickStats.LocInst.send();
                    break;
                case "test":
                    QuickStats.sendMessages("Testing function...");
                    QuickStats.GuiInst.showGUI("nxtdaydelivery");
                    break;
                case "testEntity":
                    try {
                        QuickStats.sendMessages("[QuickStats] Testing getEntity function...");
                        QuickStats.LOGGER.info(GetEntity.get(0).getName());
                        QuickStats.sendMessages("entity = " + GetEntity.get(0).getName());
                    } catch (Exception e) {
                        QuickStats.LOGGER.info("entity = null");
                    }
                    break;
                default:
                    if(args[0].equals("me")) {
                        QuickStats.GuiInst.showGUI(mc.thePlayer.getName());
                    } else {
                        QuickStats.GuiInst.showGUI(args[0]);
                    }
                    try {
                        if (args.length == 2) {
                            switch (args[1].toUpperCase()) {
                                case "BEDWARS":
                                case "BW":
                                case "BEDWAR":
                                    LocrawUtil.gameType = "BEDWARS";
                                    break;
                                case "SW":
                                case "SKYWARS":
                                case "SKYWAR":
                                    LocrawUtil.gameType = "SKYWARS";
                                    break;
                                case "QC":
                                case "QK":
                                case "QUAKE":
                                    LocrawUtil.gameType = "solo";
                                    break;
                                case "DUEL":
                                case "DUELS":
                                case "DL":
                                    LocrawUtil.gameType = "DUELS";
                                    break;
                                default:
                                    LocrawUtil.gameType = args[1].toUpperCase();
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        if (GUIConfig.debugMode) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            if (e instanceof SecurityException) throw new ReportedException(new CrashReport("Mismatch in mod hash", new SecurityException("Mismatch in mod hash")));
            sender.addChatMessage(new ChatComponentText(Reference.COLOR
                    + "[QuickStats] Command menu (mod version " + Reference.VERSION + ")"));
            sender.addChatMessage(new ChatComponentText(Reference.COLOR
                    + "[QuickStats] Command usage: /quickstats <name>, /quickstats configure, /quickstats reload, /quickstats api <api key>"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        try {
            Collection<NetworkPlayerInfo> players = mc.getNetHandler().getPlayerInfoMap();
            List<String> list = new ArrayList<>();
            for (NetworkPlayerInfo info : players) {
                list.add(info.getGameProfile().getName());
            }

            if(args.length == 1) return getListOfStringsMatchingLastWord(args, list.toArray(new String[0]));
            else return null;
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

}
