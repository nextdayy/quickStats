package com.nxtdelivery.quickStats.api;

import com.google.gson.JsonObject;
import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import org.jetbrains.annotations.NotNull;

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
                if(game.startsWith("\"")) {
                    game = game.substring(1, game.length() - 1); // fix for too many speech marks
                }
                // System.out.println(game);
                if(game.equals("MAIN") || game.equals("LIMBO") || game.equals("DEFAULT") || !GUIConfig.autoGame) {
                    throw new NullPointerException("default game");
                }
            } catch (Exception e) {
                //if(GUIConfig.debugMode) {e.printStackTrace();}
                QuickStats.LOGGER.debug("default game: " + GUIConfig.defaultGame);
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
                        returnStats.add("Level: " + getNullProtectedString("levelFormatted",swStats) + "\u00A7r    Mode: \u00A75"
                                + "SkyWars");
                        returnStats.add("Heads: \u00A75" + getFormattedInt("heads",swStats) + "\u00A7r     Coins: \u00A76"
                                + getFormattedInt("coins",swStats));
                        returnStats.add("Kills: " + getFormattedInt(getNullProtectedInt("skywars_kills_solo",acStats) + getNullProtectedInt("skywars_kills_team",acStats)) + "     Deaths: "
                                + getFormattedInt("losses",swStats));
                        returnStats.add("Wins: " + getFormattedInt(getNullProtectedInt("skywars_wins_solo",acStats) + getNullProtectedInt("skywars_wins_team",acStats)) + "     Losses: "
                                + getFormattedInt("losses",swStats));

                        kdString = ratioCalc(getNullProtectedFloat("kills",swStats),
                                getNullProtectedFloat("deaths",swStats), "kd");
                        wlString = ratioCalc(getNullProtectedFloat("wins",swStats),
                                getNullProtectedFloat("losses",swStats), "wins");

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
                        returnStats.add("Level: \u00A79" + getNullProtectedString("bedwars_level",acStats) + "✫"
                                + "\u00A7r       Game: \u00A72BedWars");
                        returnStats.add("Wins: \u00A75" + getFormattedInt("bedwars_wins",acStats)
                                + "\u00A7r      Coins: \u00A76" + getFormattedInt("coins",bwStats));
                        returnStats.add("Kills: " + getFormattedInt("kills_bedwars",bwStats) + "     Deaths: "
                                + getFormattedInt("deaths_bedwars",bwStats));
                        returnStats.add("Final Kills: " + getFormattedInt("final_kills_bedwars",bwStats)
                                + "     Final Deaths: " + getFormattedInt("final_deaths_bedwars",bwStats));

                        kdString = ratioCalc(getNullProtectedFloat("final_kills_bedwars",bwStats),
                                getNullProtectedFloat("final_deaths_bedwars",bwStats), "kd");
                        wlString = ratioCalc(getNullProtectedFloat("wins_bedwars",bwStats),
                                getNullProtectedFloat("losses_bedwars",bwStats), "wins");
                        returnStats.add("Final K/D: " + kdString + "      Win/Loss: " + wlString);
                    } catch (Exception e) {
                        e.printStackTrace();
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
                        returnStats.add("Level: \u00A74" + lvlInt + "\u00A7r       Mode: \u00A75 Quakecraft");
                        returnStats.add("Godlikes: \u00A75" + getNullProtectedString("quake_godlikes",acStats) + "\u00A7r     Coins: \u00A76"
                                + getFormattedInt("coins",qkStats));
                        returnStats.add("Kills: " + getFormattedInt("kills",qkStats) + "     Deaths: "
                                + getFormattedInt("deaths",qkStats));
                        returnStats.add("Wins: " + getFormattedInt("wins",qkStats) + "     Headshots: "
                                + getFormattedInt("headshots",qkStats));
                        kdString = ratioCalc(getNullProtectedFloat("kills",qkStats),
                                getNullProtectedFloat("deaths",qkStats), "kd");
                        wlString = ratioCalc(getNullProtectedFloat("wins",qkStats),
                                getNullProtectedFloat("deaths",qkStats), "wins");
                        returnStats.add("K/D: " + kdString + "      Win/Loss: " + wlString);
                    } catch (Exception e) {
                        returnStats.add("no more stats could be found!");
                        e.printStackTrace();
                    }
                    break;

                default:
                    QuickStats.LOGGER.warn("Unsupported game: " + game);
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



    private static ArrayList<String> genericBW(String gamemodeFormatted, String gamemode, JsonObject acStats,
                                               JsonObject playerStats) { // TODO do this for more games
        ArrayList<String> result = new ArrayList<>();
        try {
            JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
            String kdString, wlString;
            result.add("Level: \u00A79" + getNullProtectedString("bedwars_level",acStats) + "✫" + "\u00A7r    Mode: \u00A72"
                    + gamemodeFormatted);
            if (GUIConfig.compactMode) {
                result.add("W: \u00A75" + getFormattedInt(gamemode + "_wins_bedwars",bwStats)
                        + "\u00A7r      Coins: \u00A76" + getFormattedInt("coins",bwStats));
                result.add("K: " + getFormattedInt(gamemode + "_kills_bedwars",bwStats) + "     D: "
                        + getFormattedInt(gamemode + "_deaths_bedwars",bwStats));
                result.add("FK: " + getFormattedInt(gamemode + "_final_kills_bedwars",bwStats)
                        + "     FD: " + getFormattedInt(gamemode + "_final_deaths_bedwars",bwStats));
                kdString = ratioCalc(getNullProtectedFloat(gamemode + "_final_kills_bedwars",bwStats),
                        getNullProtectedFloat(gamemode + "_final_deaths_bedwars",bwStats), "kd");
                wlString = ratioCalc(getNullProtectedFloat(gamemode + "_wins_bedwars",bwStats),
                        getNullProtectedFloat(gamemode + "_losses_bedwars",bwStats), "wins");
                result.add("FK/D: " + kdString + "      W/L: " + wlString);
            } else {
                result.add("Wins: \u00A75" + getFormattedInt(gamemode + "_wins_bedwars",bwStats)
                        + "\u00A7r      Coins: \u00A76" + getFormattedInt("coins",bwStats));
                result.add("Kills: " + getFormattedInt(gamemode + "_kills_bedwars",bwStats) + "     Deaths: "
                        + getFormattedInt(gamemode + "_deaths_bedwars",bwStats));
                result.add("Final Kills: " + getFormattedInt(gamemode + "_final_kills_bedwars",bwStats)
                        + "     Final Deaths: " + getFormattedInt(gamemode + "_final_deaths_bedwars",bwStats));

                kdString = ratioCalc(getNullProtectedFloat(gamemode + "_final_kills_bedwars",bwStats),
                        getNullProtectedFloat(gamemode + "_final_deaths_bedwars",bwStats), "kd");
                wlString = ratioCalc(getNullProtectedFloat(gamemode + "_wins_bedwars",bwStats),
                        getNullProtectedFloat(gamemode + "_losses_bedwars",bwStats), "wins");
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
            result.add("Level: " + getNullProtectedString("levelFormatted",swStats) + "\u00A7r    Mode: \u00A75"
                    + gamemodeFormatted);
            result.add("Heads: \u00A75" + getFormattedInt("heads",swStats) + "\u00A7r     Coins: \u00A76"
                    + getFormattedInt("coins",swStats));
            result.add("Kills: " + getFormattedInt("kills_" + gamemode,swStats) + "     Deaths: "
                    + getFormattedInt("deaths_" + gamemode,swStats));
            result.add("Wins: " + getFormattedInt("wins_" + gamemode,swStats) + "     Losses: "
                    + getFormattedInt("losses_" + gamemode,swStats));

            kdString = ratioCalc(getNullProtectedFloat("kills",swStats),
                    getNullProtectedFloat("deaths",swStats), "kd");
            wlString = ratioCalc(getNullProtectedFloat("wins",swStats),
                    getNullProtectedFloat("losses",swStats), "wins");

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
            result.add("Level: \u00A74" + lvlInt + "\u00A7r       Mode: \u00A75" + gamemodeFormatted);
            try {
                winstreak = getNullProtectedString("best_winstreak_mode_" + gamemode,duelStats);
            } catch (Exception e) {
                winstreak = "0";
            }
            result.add("Best Winstreak: \u00A75" + winstreak + "\u00A7r     Coins: \u00A76"
                    + getFormattedInt("coins",duelStats));
            if (gamemode.contains("bridge")) { // bridge is different for some reason... why
                result.add("Kills: " + getFormattedInt(gamemode + "_bridge_kills",duelStats) + "           Deaths: "
                        + getFormattedInt(gamemode + "_bridge_deaths",duelStats));
                kdString = ratioCalc(getNullProtectedFloat(gamemode + "_bridge_kills",duelStats),
                        getNullProtectedFloat(gamemode + "_bridge_deaths",duelStats), "kd");
            } else {
                result.add("Kills: " + getFormattedInt(gamemode + "_kills",duelStats) + "           Deaths: "
                        + getFormattedInt(gamemode + "_deaths",duelStats));
                kdString = ratioCalc(getNullProtectedFloat(gamemode + "_kills",duelStats),
                        getNullProtectedFloat(gamemode + "_deaths",duelStats), "kd");
            }
            result.add("Melee Hits: " + getFormattedInt(gamemode + "_melee_hits",duelStats) + "   Melee Swings: "
                    + getFormattedInt(gamemode + "_melee_swings",duelStats));

            wlString = ratioCalc(getNullProtectedFloat(gamemode + "_melee_hits",duelStats),
                    getNullProtectedFloat(gamemode + "_melee_swings",duelStats), "wins");

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

    private static String getFormattedInt(String key, @NotNull JsonObject jsonObject) {
        Integer num;
        try {
            num = jsonObject.get(key).getAsInt();
        } catch (NullPointerException e) {
            return "0";
        }
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
    private static String getFormattedInt(Integer num) {
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

    private static String getNullProtectedString(String key, @NotNull JsonObject jsonObject) {
        try {
            return jsonObject.get(key).getAsString();
        } catch (NullPointerException e) {
            return "0";
        }
    }
    private static int getNullProtectedInt(String key, @NotNull JsonObject jsonObject) {
        try {
            return jsonObject.get(key).getAsInt();
        } catch (NullPointerException e) {
            return 0;
        }
    }
    private static float getNullProtectedFloat(String key, @NotNull JsonObject jsonObject) {
        try {
            return jsonObject.get(key).getAsFloat();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    private static String ratioCalc(float stat1, float stat2, String type) {
        float ratio = stat1 / stat2;
        String result;
        BigDecimal kd = new BigDecimal(ratio).setScale(2, RoundingMode.HALF_UP);
        if (Objects.equals(type, "wins")) {
            if (kd.floatValue() > 0.4f) {
                result = "\u00A72" + kd + "\u00A7r";
            } else {
                result = "\u00A74" + kd + "\u00A7r";
            }
        } else {
            if (kd.floatValue() > 1f) {
                result = "\u00A72" + kd + "\u00A7r";
            } else {
                result = "\u00A74" + kd + "\u00A7r";
            }
        }
        return result;
    }
}
