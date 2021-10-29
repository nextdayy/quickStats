package com.nxtdelivery.quickStats.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import com.nxtdelivery.quickStats.util.LocrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ApiRequest extends Thread {
    private static final Minecraft mc = Minecraft.getMinecraft();
    String username, rank, rankColor, playerName;
    public JsonObject rootStats, achievementStats;
    public String formattedName;
    public ArrayList<String> result;
    public static double exp;
    public boolean noUser = false;
    public boolean generalError = false;
    public boolean noAPI = false;
    public String uuid;
    public BufferedImage image;

    /**
     * Create a new instance of the API request function, with a username.
     */
    public ApiRequest(String uname) {
        username = uname;
        this.setName("QuickStats API");
        this.start();
    }

    public void run() {
        /* get UUID from Mojang */
        try {
            InputStream input = new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            JsonObject jsonObject = new JsonParser().parse(responseStrBuilder.toString()).getAsJsonObject();
            uuid = jsonObject.get("id").getAsString();
            // System.out.println(uuid);
        } catch (IllegalStateException e) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_GRAY + "[QuickStats] Player not found: " + username));
            noUser = true;
            return;
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                    + "[QuickStats] an unexpected error occurred. Check logs for more info."));
        }
        /* get head texture */
        try {
            if(GUIConfig.avatarHead) {
                image = ImageIO.read(new URL("https://cravatar.eu/helmhead/" + uuid));
            } else {
                image = ImageIO.read(new URL("https://cravatar.eu/helmavatar/" + uuid));
            }
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
        }

        /* process request from Hypixel */
        try {
            String url = "https://api.hypixel.net/player?key=" + GUIConfig.apiKey + "&uuid=" + uuid;
            if (GUIConfig.debugMode) {
                QuickStats.LOGGER.info(url);
            }
            InputStream input = new URL(url).openStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            // System.out.println(responseStrBuilder.toString);
            JsonObject js1 = new JsonParser().parse(responseStrBuilder.toString()).getAsJsonObject();
            /*
             * for(String key: flattenJson.keySet()){ // DEBUG: print all keys
             * System.out.println(key); }
             */

            boolean success = js1.get("success").getAsBoolean();
            if (success) {
                QuickStats.LOGGER.info("successfully processed from Hypixel");
                JsonObject js2 = js1.get("player").getAsJsonObject();
                try { // get rank and name
                    exp = js2.get("networkExp").getAsDouble();
                    playerName = js2.get("displayname").getAsString();
                    rank = js2.get("newPackageRank").getAsString();
                    if (rank.equals("MVP_PLUS")) {
                        try {
                            rankColor = js2.get("rankPlusColor").getAsString(); // get plus color
                            if (js2.get("monthlyPackageRank").getAsString().equals("SUPERSTAR")) { // test for mvp++
                                rank = "SUPERSTAR";
                            }
                        } catch (Exception e) {
                            rank = "MVP_PLUS";
                            rankColor = "PINK";
                            // if(GUIConfig.debugMode) {e.printStackTrace();}
                        }
                        try { // youtuber
                            rank = js2.get("rank").getAsString();
                        } catch (Exception ignored) {
                        }
                    }
                } catch (NullPointerException e) {
                    rank = "non";
                }
                if(playerName.equals("Technoblade")) {      // Technoblade never dies
                    formattedName = "\u00A7d[PIG\u00A7b+++\u00A7d] Technoblade";
                } else {
                    formattedName = getFormattedName(playerName, rank, rankColor);
                }

                rootStats = js2.get("stats").getAsJsonObject();
                achievementStats = js2.get("achievements").getAsJsonObject();
                result = Stats.getStats(rootStats, achievementStats, LocrawUtil.gameType);
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                        + "[QuickStats] The Hypixel API didn't process the request properly. Try again."));
                generalError = true;
                QuickStats.LOGGER.error("error occurred when building after API request, closing");
            }

        } catch (IOException e) {
            if (GUIConfig.apiKey.equals("none")) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                        + "[QuickStats] You haven't set an API key yet! Type /api new to get one, and the mod should grab it."));
                noAPI = true;
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                        + "[QuickStats] failed to contact Hypixel API. This is usually due to an invalid API key."));
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                        + "[QuickStats] On Hypixel, type /api new to get a new key and the mod should automatically grab it."));
                generalError = true;
            }
        } catch (Exception e) {
            // QuickStats.LOGGER.error(e.getStackTrace().toString());
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY
                    + "[QuickStats] an unexpected error occurred. Check logs for more info."));
            generalError = true;
        }
    }

    private String getFormattedName(String name, String rank, String color) {
        QuickStats.LOGGER.debug(color);
        String formattedName;
        boolean getColor = false;
        int plusA = 0;
        switch (rank) {
            case "VIP":
                formattedName = "\u00A7a[VIP] " + name;
                break;
            case "VIP_PLUS":
                formattedName = "\u00A7a[VIP\u00A76+\u00A7a] " + name;
                break;
            case "MVP":
                formattedName = "\u00A7b[MVP] " + name;
                break;
            case "MVP_PLUS":
                getColor = true;
                plusA = 1;
                formattedName = "\u00A7b[MVP";
                break;
            case "SUPERSTAR":
                getColor = true;
                plusA = 2;
                formattedName = "\u00A76[MVP";
                break;
            case "YOUTUBER":
                formattedName = "\u00A7c[\u00A7fYOUTUBE\u00A7c] " + name;
                break;
            case "ADMIN":
                formattedName = "\u00A7c[ADMIN] " + name;
                break;
            default:
                formattedName = "\u00A77" + name;
                break;
        }
        if (getColor) {
            // System.out.println(color);
            switch (color) {
                case "DARK_RED":
                    formattedName += "\u00A74+";
                    break;
                case "DARK_GREEN":
                    formattedName += "\u00A72+";
                    break;
                case "BLACK":
                    formattedName += "\u00A70+";
                    break;
                case "LIGHT_PURPLE":
                case "PINK":
                    formattedName += "\u00A7d+";
                    break;
                case "BLUE":
                    formattedName += "\u00A79+";
                    break;
                case "DARK_GRAY":
                    formattedName += "\u00A77+";
                    break;
                case "GOLD":
                    formattedName += "\u00A76+";
                    break;
                case "GREEN":
                    formattedName += "\u00A7a+";
                    break;
                case "YELLOW":
                    formattedName += "\u00A7e+";
                    break;
                case "WHITE":
                    formattedName += "\u00A7f+";
                    break;
                case "DARK_PURPLE":
                    formattedName += "\u00A75+";
                    break;
                case "DARK_BLUE":
                    formattedName += "\u00A71+";
                    break;
                case "DARK_AQUA":
                    formattedName += "\u00A73+";
                    break;
                default:
                    formattedName += "+";
            }
            if (plusA == 2) {
                formattedName += "+\u00A76] " + name;
            } else {
                formattedName += "\u00A7b] " + name;
            }
        }

        return formattedName;
    }

}