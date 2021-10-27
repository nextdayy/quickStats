package com.nxtdelivery.quickStats.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.google.gson.JsonObject;
import com.nxtdelivery.quickStats.gui.GUIConfig;

public class Stats {
	/**
	 * Checks game, then return stats specific for that game.
	 * 
	 * @param playerStats
	 * @param acStats
	 * @param game
	 * @return ArrayList game-specific stats
	 */
	public static ArrayList getStats(JsonObject playerStats, JsonObject acStats, String game) {			// TODO comma separation of numbers?
		ArrayList returnStats = new ArrayList();
		float Fkd, Fwl;
		BigDecimal kd, wl;
		String kdString, wlString;
		try {
			try {
				game = game.substring(1, game.length() - 1); // fix for too many speech marks
				// System.out.println(game);
			} catch (Exception e) {
				//e.printStackTrace();
				//game = "DUELS"; // testing when in dev env
				System.out.println(GUIConfig.defaultGame);
				switch(GUIConfig.defaultGame) {
				case 0:
					game = "BEDWARS";
					break;
				case 1:
					game = "SKYWARS";
					break;
				case 2:
					game = "DUELS";
					break;
				default:		// just in case!
					game = "teams_insane";
					break;
				}
			}
			switch (game) {
			case "SKYWARS":
			case "solo_insane":
				returnStats = genericSW("Solo Insane", "solo_insane", acStats, playerStats);
				break;
			case "solo_normal":
				returnStats = genericSW("Solo Normal", "solo_normal", acStats, playerStats);
				break;
			case "teams_normal":
				returnStats = genericSW("Teams Normal", "team_normal", acStats, playerStats);
				break;
			case "teams_insane":
				returnStats = genericSW("Teams Insane", "team_insane", acStats, playerStats);
				break;

			case "BEDWARS":
				try {
					JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
					returnStats.add("Level: \u00A79" + acStats.get("bedwars_level").getAsString() + "✫"
							+ "\u00A7f       Game: \u00A72BedWars");
					returnStats.add("Wins: \u00A75" + acStats.get("bedwars_wins").getAsString()
							+ "\u00A7f      Coins: \u00A76" + bwStats.get("coins").getAsString());
					returnStats.add("Kills: " + bwStats.get("kills_bedwars").getAsString() + "     Deaths: "
							+ bwStats.get("deaths_bedwars").getAsString());
					returnStats.add("Final Kills: " + bwStats.get("final_kills_bedwars").getAsString()
							+ "     Final Deaths: " + bwStats.get("final_deaths_bedwars").getAsString());

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
			case "DUELS_CLASSIC_DUEL":
				returnStats = genericDuel("Classic 1v1", "classic_duel", acStats, playerStats);
				break;
			case "DUELS_UHC_DUEL":
				returnStats = genericDuel("UHC 1v1", "uhc_duel", acStats, playerStats);
				break;
			case "DUELS_COMBO_DUEL":
				returnStats = genericDuel("Combo 1v1", "combo_duel", acStats, playerStats);
				break;
			case "DUELS_OP_DUEL":
				returnStats = genericDuel("OP 1v1", "op_duel", acStats, playerStats);
				break;
			case "DUELS_SUMO_DUEL":
				returnStats = genericDuel("Sumo 1v1", "sumo_duel", acStats, playerStats);
				break;
			case "DUELS_BLITZ_DUEL":
				returnStats = genericDuel("Blitz 1v1", "blitz_duel", acStats, playerStats);
				break;
			case "DUELS_BRIDGE_DUEL":
				returnStats = genericDuel("Bridge 1v1", "bridge_duel", acStats, playerStats);
				break;
			case "DUELS_BRIDGE_DOUBLES":
				returnStats = genericDuel("Bridge 2v2", "bridge_doubles", acStats, playerStats);
				break;
			case "DUELS_OP_DOUBLES":
				returnStats = genericDuel("OP 2v2", "op_doubles", acStats, playerStats);
				break;
			case "DUELS_SW_DOUBLES":
				returnStats = genericDuel("SkyWars 2v2", "sw_doubles", acStats, playerStats);
				break;
			case "DUELS_SW_DUEL":
				returnStats = genericDuel("SkyWars 1v1", "sw_duel", acStats, playerStats);
				break;

			default:
				returnStats.add("you aren't in a supported game!");
				returnStats.add("lots more games coming soon!");
				break;
			}
			return returnStats;
		} catch (Exception e) {
			e.printStackTrace();
			returnStats.add("No stats for this user were found!");
			return returnStats;
		}
	}

	/**
	 * Calculates general ration, with String formatting.
	 * 
	 * @param stat1
	 * @param stat2
	 * @param type
	 * @return formatted RATIO
	 */
	private static String ratioCalc(float stat1, float stat2, String type) {
		float ratio = stat1 / stat2;
		String result;
		BigDecimal kd = new BigDecimal(ratio).setScale(2, RoundingMode.HALF_UP);
		if (type == "wins") {
			if (kd.floatValue() > 0.4f) {
				result = "\u00A72" + kd.toString() + "\u00A7f";
			} else {
				result = "\u00A74" + kd.toString() + "\u00A7f";
			}
		} else {
			if (kd.floatValue() > 1f) {
				result = "\u00A72" + kd.toString() + "\u00A7f";
			} else {
				result = "\u00A74" + kd.toString() + "\u00A7f";
			}
		}
		return result;
	}

	/**
	 * Constructor for general bedwars gamemodes.
	 * 
	 * @param gamemodeFormatted
	 * @param gamemode
	 * @param acStats
	 * @param playerStats
	 * @return ArrayList stats
	 */
	private static ArrayList genericBW(String gamemodeFormatted, String gamemode, JsonObject acStats,
			JsonObject playerStats) { // TODO do this for more games
		ArrayList result = new ArrayList();
		try {
			JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
			String kdString, wlString;
			result.add("Level: \u00A79" + acStats.get("bedwars_level").getAsString() + "✫" + "\u00A7f    Mode: \u00A72"
					+ gamemodeFormatted);
			result.add("Wins: \u00A75" + bwStats.get(gamemode + "_wins_bedwars").getAsString()
					+ "\u00A7f      Coins: \u00A76" + bwStats.get("coins").getAsString());
			result.add("Kills: " + bwStats.get(gamemode + "_kills_bedwars").getAsString() + "     Deaths: "
					+ bwStats.get(gamemode + "_deaths_bedwars").getAsString());
			result.add("Final Kills: " + bwStats.get(gamemode + "_final_kills_bedwars").getAsString()
					+ "     Final Deaths: " + bwStats.get(gamemode + "_final_deaths_bedwars").getAsString());

			kdString = ratioCalc(bwStats.get(gamemode + "_final_kills_bedwars").getAsFloat(),
					bwStats.get(gamemode + "_final_deaths_bedwars").getAsFloat(), "kd");
			wlString = ratioCalc(bwStats.get(gamemode + "_wins_bedwars").getAsFloat(),
					bwStats.get(gamemode + "_losses_bedwars").getAsFloat(), "wins");
			result.add("Final K/D: " + kdString + "      Win/Loss: " + wlString);
			return result;
		} catch (Exception e) {
			result.add("no more stats could be found!");
			return result;
		}
	}

	/**
	 * Constructor for general Skywars stats gamemodes.
	 * 
	 * @param gamemodeFormatted
	 * @param gamemode
	 * @param acStats
	 * @param playerStats
	 * @return ArrayList stats
	 */
	private static ArrayList genericSW(String gamemodeFormatted, String gamemode, JsonObject acStats,
			JsonObject playerStats) {
		ArrayList result = new ArrayList();
		try {
			JsonObject swStats = playerStats.get("SkyWars").getAsJsonObject();
			String kdString, wlString;
			result.add("Level: " + swStats.get("levelFormatted").getAsString() + "\u00A7f    Mode: \u00A75"
					+ gamemodeFormatted);
			result.add("Heads: \u00A75" + swStats.get("heads").getAsString() + "\u00A7f     Coins: \u00A76"
					+ swStats.get("coins").getAsString());
			result.add("Kills: " + swStats.get("kills_" + gamemode).getAsString() + "     Deaths: "
					+ swStats.get("deaths_" + gamemode).getAsString());
			result.add("Wins: " + swStats.get("wins_" + gamemode).getAsString() + "     Losses: "
					+ swStats.get("losses_" + gamemode).getAsString());

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

	/**
	 * constructor for general duel stats.
	 * 
	 * @param gamemodeFormatted
	 * @param gamemode
	 * @param acStats
	 * @param playerStats
	 * @return ArrayList stats
	 */
	private static ArrayList genericDuel(String gamemodeFormatted, String gamemode, JsonObject acStats,
			JsonObject playerStats) {
		ArrayList result = new ArrayList();
		try {
			JsonObject duelStats = playerStats.get("Duels").getAsJsonObject();
			double lvl = getLevel(ApiRequest.exp);
			Integer lvlInt = (int) Math.round(lvl);
			String kdString, wlString;
			String winstreak;
			result.add("Level: \u00A74" + lvlInt + "\u00A7f       Mode: \u00A75" + gamemodeFormatted);
			try {
				winstreak = duelStats.get("best_winstreak_mode_" + gamemode).getAsString();
			} catch (Exception e) {
				winstreak = "0";
			}
			result.add("Best Winstreak: \u00A75" + winstreak + "\u00A7f     Coins: \u00A76"
					+ duelStats.get("coins").getAsString());
			if (gamemode.contains("bridge")) { // bridge is different for some reason... why
				result.add("Kills: " + duelStats.get(gamemode + "_bridge_kills").getAsString() + "           Deaths: "
						+ duelStats.get(gamemode + "_bridge_deaths").getAsString());
				kdString = ratioCalc(duelStats.get(gamemode + "_bridge_kills").getAsFloat(),
						duelStats.get(gamemode + "_bridge_deaths").getAsFloat(), "kd");
			} else {
				result.add("Kills: " + duelStats.get(gamemode + "_kills").getAsString() + "           Deaths: "
						+ duelStats.get(gamemode + "_deaths").getAsString());
				kdString = ratioCalc(duelStats.get(gamemode + "_kills").getAsFloat(),
						duelStats.get(gamemode + "_deaths").getAsFloat(), "kd");
			}
			result.add("Melee Hits: " + duelStats.get(gamemode + "_melee_hits").getAsString() + "   Melee Swings: "
					+ duelStats.get(gamemode + "_melee_swings").getAsString());

			wlString = ratioCalc(duelStats.get(gamemode + "_melee_hits").getAsFloat(),
					duelStats.get(gamemode + "_melee_swings").getAsFloat(), "wins");

			result.add("K/D: " + kdString + "        Melee H/M: " + wlString);
		} catch (Exception e) {
			result.add("no more stats could be found!");
			e.printStackTrace();
			return result;
		}
		return result;
	}

	/**
	 * Method to get network level of player. Taken from Hypixel API documentation.
	 */
	static double getLevel(double exp) {
		double BASE = 10_000;
		double GROWTH = 2_500;
		double HALF_GROWTH = 0.5 * GROWTH;
		double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
		double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
		double GROWTH_DIVIDES_2 = 2 / GROWTH;
		return exp < 0 ? 1 : Math.floor(1 + REVERSE_PQ_PREFIX + Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * exp));
	}
}
