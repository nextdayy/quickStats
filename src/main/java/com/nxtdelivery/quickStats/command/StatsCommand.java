package com.nxtdelivery.quickStats.command;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.Reference;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.gui.GUIStats;
import com.nxtdelivery.quickStats.util.GetEntity;
import com.nxtdelivery.quickStats.util.LocrawUtil;
import com.nxtdelivery.quickStats.util.TickDelay;
import com.nxtdelivery.quickStats.util.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
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
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.DARK_GRAY + "[QuickStats] Reloading!"));
                    GUIConfig.INSTANCE.initialize();
                    QuickStats.updateCheck = UpdateChecker.updateNeeded(Reference.VERSION);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                            + "[QuickStats] Reloaded! Re-log and check logs for more information."));
                    Minecraft.getMinecraft().thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
                    break;
                case "api":
                    GUIConfig.apiKey = args[1];
                    GUIConfig.INSTANCE.markDirty();
                    GUIConfig.INSTANCE.writeData();
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.DARK_GRAY + ("[QuickStats] set your API key as: " + args[1] + ".")));
                    break;
                case "testLoc":
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.DARK_GRAY + "[QuickStats] Testing locraw function..."));
                    QuickStats.LocInst.send();
                    break;
                case "test":
                    sender.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.DARK_GRAY + "[QuickStats] Testing function..."));
                    QuickStats.GuiInst.showGUI("nxtdaydelivery");
                    break;
                case "testEntity":
                    try {
                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.DARK_GRAY + "[QuickStats] Testing getEntity function..."));
                        QuickStats.LOGGER.info(GetEntity.get(0).getName());
                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.DARK_GRAY + "[QuickStats] entity = " + GetEntity.get(0).getName()));
                    } catch (Exception e) {
                        QuickStats.LOGGER.info("entity = null");
                        sender.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.DARK_GRAY + "[QuickStats] entity = null"));
                    }
                    break;
                default:
                    QuickStats.GuiInst.showGUI(args[0]);
                    break;
            }
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                    + "[QuickStats] Command menu (mod version " + Reference.VERSION + ")"));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
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
            //String output = String.join(", ", list);
            return getListOfStringsMatchingLastWord(args, list.toArray(new String[0]));
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
