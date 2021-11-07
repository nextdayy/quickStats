package com.nxtdelivery.quickStats.api;

import com.google.gson.JsonObject;
import com.nxtdelivery.quickStats.gui.GUIConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

public class Stats {
    public static ArrayList<String> getStats(JsonObject playerStats, JsonObject acStats, String game) {
        ArrayList<String> returnStats = new ArrayList<>();
        String kdString, wlString;
        try {
            try {
                game = game.substring(1, game.length() - 1); // fix for too many speech marks
                // System.out.println(game);
                if(game.equals("MAIN")) {
                    throw new NullPointerException("default game");
                }
            } catch (Exception e) {
                //if(GUIConfig.debugMode) {e.printStackTrace();}
                System.out.println(GUIConfig.defaultGame);
                switch (GUIConfig.defaultGame) {
                    case 0:
                        game = "BEDWARS";
                        break;
                    case 1:
                        game = "SKYWARS";
                        break;
                    case 2:
                        game = "DUELS";
                        break;
                    case 3:
                        game = "solo";
                        break;
                    default:        // just in case!
                        game = "teams_insane";
                        break;
                }
            }
            switch (game) {
                case "SKYWARS":
                    try {
                        JsonObject swStats = playerStats.get("SkyWars").getAsJsonObject();
                        returnStats.add("Level: " + swStats.get("levelFormatted").getAsString() + "\u00A7f    Mode: \u00A75"
                                + "SkyWars");
                        returnStats.add("Heads: \u00A75" + formatInt(swStats.get("heads").getAsInt()) + "\u00A7f     Coins: \u00A76"
                                + formatInt(swStats.get("coins").getAsInt()));
                        returnStats.add("Kills: " + formatInt(acStats.get("skywars_kills_solo").getAsInt() + acStats.get("skywars_kills_team").getAsInt()) + "     Deaths: "
                                + formatInt(swStats.get("losses").getAsInt()));
                        returnStats.add("Wins: " + formatInt(acStats.get("skywars_wins_solo").getAsInt() + acStats.get("skywars_wins_team").getAsInt()) + "     Losses: "
                                + formatInt(swStats.get("losses").getAsInt()));

                        kdString = ratioCalc(swStats.get("kills").getAsFloat(),
                                swStats.get("deaths").getAsFloat(), "kd");
                        wlString = ratioCalc(swStats.get("wins").getAsFloat(),
                                swStats.get("losses").getAsFloat(), "wins");

                        returnStats.add("K/D: " + kdString + "      Win/Loss: " + wlString);
                    } catch (Exception e) {
                        returnStats.add("no more stats could be found!");
                    }
                    break;
                case "solo_insane":
                    returnStats = genericSW("Solo Insane", "solo_insane", playerStats);
                    break;
                case "solo_normal":
                    returnStats = genericSW("Solo Normal", "solo_normal", playerStats);
                    break;
                case "teams_normal":
                    returnStats = genericSW("Teams Normal", "team_normal", playerStats);
                    break;
                case "teams_insane":
                    returnStats = genericSW("Teams Insane", "team_insane", playerStats);
                    break;

                case "BEDWARS":
                    try {
                        JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
                        returnStats.add("Level: \u00A79" + acStats.get("bedwars_level").getAsString() + "✫"
                                + "\u00A7f       Game: \u00A72BedWars");
                        returnStats.add("Wins: \u00A75" + formatInt(acStats.get("bedwars_wins").getAsInt())
                                + "\u00A7f      Coins: \u00A76" + formatInt(bwStats.get("coins").getAsInt()));
                        returnStats.add("Kills: " + formatInt(bwStats.get("kills_bedwars").getAsInt()) + "     Deaths: "
                                + formatInt(bwStats.get("deaths_bedwars").getAsInt()));
                        returnStats.add("Final Kills: " + formatInt(bwStats.get("final_kills_bedwars").getAsInt())
                                + "     Final Deaths: " + formatInt(bwStats.get("final_deaths_bedwars").getAsInt()));

                        kdString = ratioCalc(bwStats.get("final_kills_bedwars").getAsFloat(),
                                bwStats.get("final_deaths_bedwars").getAsFloat(), "kd");
                        wlString = ratioCalc(bwStats.get("wins_bedwars").getAsFloat(),
                                bwStats.get("losses_bedwars").getAsFloat(), "wins");
                        returnStats.add("Final K/D: " + kdString + "      Win/Loss: " + wlString);
                    } catch (Exception e) {
                        returnStats.add("No more stats could be found!");
                    }
                    break;
                case "BEDWARS_FOUR_THREE":
                    returnStats = genericBW("BedWars Trios", "four_three", acStats, playerStats);
                    break;
                case "BEDWARS_EIGHT_ONE":
                    returnStats = genericBW("BedWars Solo", "eight_one", acStats, playerStats);
                    break;
                case "BEDWARS_FOUR_FOUR":
                    returnStats = genericBW("BedWars Fours", "four_four", acStats, playerStats);
                    break;
                case "BEDWARS_EIGHT_TWO":
                    returnStats = genericBW("BedWars Doubles", "eight_two", acStats, playerStats);
                    break;

                case "DUELS":
                    switch (GUIConfig.defaultDuel) {
                        case 0:
                            returnStats = genericDuel("Classic 1v1", "classic_duel", playerStats);
                            break;
                        case 1:
                            returnStats = genericDuel("UHC 1v1", "uhc_duel", playerStats);
                            break;
                        case 2:
                            returnStats = genericDuel("Combo 1v1", "combo_duel", playerStats);
                            break;
                        case 3:
                            returnStats = genericDuel("OP 1v1", "op_duel", playerStats);
                            break;
                        case 4:
                            returnStats = genericDuel("Blitz 1v1", "blitz_duel", playerStats);
                            break;
                        case 5:
                            returnStats = genericDuel("Sumo 1v1", "sumo_duel", playerStats);
                            break;
                        case 6:
                            returnStats = genericDuel("SkyWars 1v1", "sw_duel", playerStats);
                            break;
                        case 7:
                            returnStats = genericDuel("Bridge 1v1", "bridge_duel", playerStats);
                            break;
                        case 8:
                            returnStats = genericDuel("Bridge 2v2", "bridge_doubles", playerStats);
                            break;
                    }
                    break;
                case "DUELS_CLASSIC_DUEL":
                    returnStats = genericDuel("Classic 1v1", "classic_duel", playerStats);
                    break;
                case "DUELS_UHC_DUEL":
                    returnStats = genericDuel("UHC 1v1", "uhc_duel", playerStats);
                    break;
                case "DUELS_COMBO_DUEL":
                    returnStats = genericDuel("Combo 1v1", "combo_duel", playerStats);
                    break;
                case "DUELS_OP_DUEL":
                    returnStats = genericDuel("OP 1v1", "op_duel", playerStats);
                    break;
                case "DUELS_SUMO_DUEL":
                    returnStats = genericDuel("Sumo 1v1", "sumo_duel", playerStats);
                    break;
                case "DUELS_BLITZ_DUEL":
                    returnStats = genericDuel("Blitz 1v1", "blitz_duel", playerStats);
                    break;
                case "DUELS_BRIDGE_DUEL":
                    returnStats = genericDuel("Bridge 1v1", "bridge_duel", playerStats);
                    break;
                case "DUELS_BRIDGE_DOUBLES":
                    returnStats = genericDuel("Bridge 2v2", "bridge_doubles", playerStats);
                    break;
                case "DUELS_OP_DOUBLES":
                    returnStats = genericDuel("OP 2v2", "op_doubles", playerStats);
                    break;
                case "DUELS_SW_DOUBLES":
                    returnStats = genericDuel("SkyWars 2v2", "sw_doubles", playerStats);
                    break;
                case "DUELS_SW_DUEL":
                    returnStats = genericDuel("SkyWars 1v1", "sw_duel", playerStats);
                    break;

                case "teams":    // quake craft
                case "solo":
                    try {
                        JsonObject qkStats = playerStats.get("Quake").getAsJsonObject();
                        double lvl = getLevel(ApiRequest.exp);
                        int lvlInt = (int) Math.round(lvl);
                        returnStats.add("Level: \u00A74" + lvlInt + "\u00A7f       Mode: \u00A75 Quakecraft");
                        returnStats.add("Godlikes: \u00A75" + acStats.get("quake_godlikes").getAsString() + "\u00A7f     Coins: \u00A76"
                                + formatInt(qkStats.get("coins").getAsInt()));
                        returnStats.add("Kills: " + formatInt(qkStats.get("kills").getAsInt()) + "     Deaths: "
                                + formatInt(qkStats.get("deaths").getAsInt()));
                        returnStats.add("Wins: " + formatInt(qkStats.get("wins").getAsInt()) + "     Headshots: "
                                + formatInt(qkStats.get("headshots").getAsInt()));
                        kdString = ratioCalc(qkStats.get("kills").getAsFloat(),
                                qkStats.get("deaths").getAsFloat(), "kd");
                        wlString = ratioCalc(qkStats.get("wins").getAsFloat(),
                                qkStats.get("deaths").getAsFloat(), "wins");
                        returnStats.add("K/D: " + kdString + "      Win/Loss: " + wlString);
                    } catch (Exception e) {
                        returnStats.add("no more stats could be found!");
                    }
                    break;

                default:
                    returnStats.add("you aren't in a supported game!");
                    returnStats.add("lots more games coming soon!");
                    break;
            }
            return returnStats;
        } catch (Exception e) {
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            returnStats.add("No stats for this user were found!");
            return returnStats;
        }
    }


    private static String ratioCalc(float stat1, float stat2, String type) {
        float ratio = stat1 / stat2;
        String result;
        BigDecimal kd = new BigDecimal(ratio).setScale(2, RoundingMode.HALF_UP);
        if (Objects.equals(type, "wins")) {
            if (kd.floatValue() > 0.4f) {
                result = "\u00A72" + kd + "\u00A7f";
            } else {
                result = "\u00A74" + kd + "\u00A7f";
            }
        } else {
            if (kd.floatValue() > 1f) {
                result = "\u00A72" + kd + "\u00A7f";
            } else {
                result = "\u00A74" + kd + "\u00A7f";
            }
        }
        return result;
    }


    private static ArrayList<String> genericBW(String gamemodeFormatted, String gamemode, JsonObject acStats,
                                               JsonObject playerStats) { // TODO do this for more games
        ArrayList<String> result = new ArrayList<>();
        try {
            JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
            String kdString, wlString;
            result.add("Level: \u00A79" + acStats.get("bedwars_level").getAsString() + "✫" + "\u00A7f    Mode: \u00A72"
                    + gamemodeFormatted);
            if (GUIConfig.compactMode) {
                result.add("W: \u00A75" + formatInt(bwStats.get(gamemode + "_wins_bedwars").getAsInt())
                        + "\u00A7f      Coins: \u00A76" + formatInt(bwStats.get("coins").getAsInt()));
                result.add("K: " + formatInt(bwStats.get(gamemode + "_kills_bedwars").getAsInt()) + "     D: "
                        + formatInt(bwStats.get(gamemode + "_deaths_bedwars").getAsInt()));
                result.add("FK: " + formatInt(bwStats.get(gamemode + "_final_kills_bedwars").getAsInt())
                        + "     FD: " + formatInt(bwStats.get(gamemode + "_final_deaths_bedwars").getAsInt()));
                kdString = ratioCalc(bwStats.get(gamemode + "_final_kills_bedwars").getAsFloat(),
                        bwStats.get(gamemode + "_final_deaths_bedwars").getAsFloat(), "kd");
                wlString = ratioCalc(bwStats.get(gamemode + "_wins_bedwars").getAsFloat(),
                        bwStats.get(gamemode + "_losses_bedwars").getAsFloat(), "wins");
                result.add("FK/D: " + kdString + "      W/L: " + wlString);
            } else {
                result.add("Wins: \u00A75" + formatInt(bwStats.get(gamemode + "_wins_bedwars").getAsInt())
                        + "\u00A7f      Coins: \u00A76" + formatInt(bwStats.get("coins").getAsInt()));
                result.add("Kills: " + formatInt(bwStats.get(gamemode + "_kills_bedwars").getAsInt()) + "     Deaths: "
                        + formatInt(bwStats.get(gamemode + "_deaths_bedwars").getAsInt()));
                result.add("Final Kills: " + formatInt(bwStats.get(gamemode + "_final_kills_bedwars").getAsInt())
                        + "     Final Deaths: " + formatInt(bwStats.get(gamemode + "_final_deaths_bedwars").getAsInt()));

                kdString = ratioCalc(bwStats.get(gamemode + "_final_kills_bedwars").getAsFloat(),
                        bwStats.get(gamemode + "_final_deaths_bedwars").getAsFloat(), "kd");
                wlString = ratioCalc(bwStats.get(gamemode + "_wins_bedwars").getAsFloat(),
                        bwStats.get(gamemode + "_losses_bedwars").getAsFloat(), "wins");
                result.add("Final K/D: " + kdString + "      Win/Loss: " + wlString);
            }
            return result;
        } catch (Exception e) {
            result.add("no more stats could be found!");
            return result;
        }
    }


    private static ArrayList<String> genericSW(String gamemodeFormatted, String gamemode,
                                               JsonObject playerStats) {
        ArrayList<String> result = new ArrayList<>();
        try {
            JsonObject swStats = playerStats.get("SkyWars").getAsJsonObject();
            String kdString, wlString;
            result.add("Level: " + swStats.get("levelFormatted").getAsString() + "\u00A7f    Mode: \u00A75"
                    + gamemodeFormatted);
            result.add("Heads: \u00A75" + formatInt(swStats.get("heads").getAsInt()) + "\u00A7f     Coins: \u00A76"
                    + formatInt(swStats.get("coins").getAsInt()));
            result.add("Kills: " + formatInt(swStats.get("kills_" + gamemode).getAsInt()) + "     Deaths: "
                    + formatInt(swStats.get("deaths_" + gamemode).getAsInt()));
            result.add("Wins: " + formatInt(swStats.get("wins_" + gamemode).getAsInt()) + "     Losses: "
                    + formatInt(swStats.get("losses_" + gamemode).getAsInt()));

            kdString = ratioCalc(swStats.get("kills_" + gamemode).getAsFloat(),
                    swStats.get("deaths_" + gamemode).getAsFloat(), "kd");
            wlString = ratioCalc(swStats.get("wins_" + gamemode).getAsFloat(),
                    swStats.get("losses_" + gamemode).getAsFloat(), "wins");

            result.add("K/D: " + kdString + "      Win/Loss: " + wlString);
            return result;
        } catch (Exception e) {
            result.add("no more stats could be found!");
            return result;
        }
    }


    private static ArrayList<String> genericDuel(String gamemodeFormatted, String gamemode,
                                                 JsonObject playerStats) {
        ArrayList<String> result = new ArrayList<>();
        try {
            JsonObject duelStats = playerStats.get("Duels").getAsJsonObject();
            double lvl = getLevel(ApiRequest.exp);
            int lvlInt = (int) Math.round(lvl);
            String kdString, wlString;
            String winstreak;
            result.add("Level: \u00A74" + lvlInt + "\u00A7f       Mode: \u00A75" + gamemodeFormatted);
            try {
                winstreak = duelStats.get("best_winstreak_mode_" + gamemode).getAsString();
            } catch (Exception e) {
                winstreak = "0";
            }
            result.add("Best Winstreak: \u00A75" + winstreak + "\u00A7f     Coins: \u00A76"
                    + formatInt(duelStats.get("coins").getAsInt()));
            if (gamemode.contains("bridge")) { // bridge is different for some reason... why
                result.add("Kills: " + formatInt(duelStats.get(gamemode + "_bridge_kills").getAsInt()) + "           Deaths: "
                        + formatInt(duelStats.get(gamemode + "_bridge_deaths").getAsInt()));
                kdString = ratioCalc(duelStats.get(gamemode + "_bridge_kills").getAsFloat(),
                        duelStats.get(gamemode + "_bridge_deaths").getAsFloat(), "kd");
            } else {
                result.add("Kills: " + formatInt(duelStats.get(gamemode + "_kills").getAsInt()) + "           Deaths: "
                        + formatInt(duelStats.get(gamemode + "_deaths").getAsInt()));
                kdString = ratioCalc(duelStats.get(gamemode + "_kills").getAsFloat(),
                        duelStats.get(gamemode + "_deaths").getAsFloat(), "kd");
            }
            result.add("Melee Hits: " + formatInt(duelStats.get(gamemode + "_melee_hits").getAsInt()) + "   Melee Swings: "
                    + formatInt(duelStats.get(gamemode + "_melee_swings").getAsInt()));

            wlString = ratioCalc(duelStats.get(gamemode + "_melee_hits").getAsFloat(),
                    duelStats.get(gamemode + "_melee_swings").getAsFloat(), "wins");

            result.add("K/D: " + kdString + "        Melee H/M: " + wlString);
        } catch (Exception e) {
            result.add("no more stats could be found!");
            if (GUIConfig.debugMode) {
                e.printStackTrace();
            }
            return result;
        }
        return result;
    }

    /**
     * Method to get network level of player. Taken from Hypixel API documentation.
     */
    private static double getLevel(double exp) {
        double BASE = 10_000;
        double GROWTH = 2_500;
        double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
        double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
        double GROWTH_DIVIDES_2 = 2 / GROWTH;
        return exp < 0 ? 1 : Math.floor(1 + REVERSE_PQ_PREFIX + Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * exp));
    }

    private static String formatInt(Integer num) {
        if (GUIConfig.numberFormat) {
            try {
                NumberFormat form = NumberFormat.getInstance();
                form.setGroupingUsed(true);
                return form.format(num);
            } catch (Exception e) {
                return num.toString();
            }
        } else {
            return num.toString();
        }
    }
}
