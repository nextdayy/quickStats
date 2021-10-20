package com.nxtdelivery.quickStats.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import com.google.gson.JsonObject;

public class Stats {
	public static ArrayList getStats(JsonObject playerStats, JsonObject acStats, String game) {
		ArrayList returnStats = new ArrayList();
		float Fkd, Fwl;
		BigDecimal kd, wl;
		String kdString, wlString;
		try {
			JsonObject swStats = playerStats.get("SkyWars").getAsJsonObject();
			JsonObject bwStats = playerStats.get("Bedwars").getAsJsonObject();
			try {
				game = game.substring(1, game.length() - 1); // fix for too many speech marks
			} catch (Exception e) { // testing when in dev env
				game = "solo_insane";
			}
			switch (game) {
			case "SKYWARS":
			case "teams_insane":
			case "solo_insane":
				returnStats.add("SkyWars Level: " + swStats.get("levelFormatted").getAsString()
						+ "\u00A7f     Coins: \u00A76" + swStats.get("coins").getAsString());
				returnStats.add("Heads: \u00A75" + swStats.get("heads").getAsString());
				returnStats.add("Kills: " + swStats.get("kills_solo_insane").getAsString() + "     Deaths: "
						+ swStats.get("deaths_solo_insane").getAsString());
				returnStats.add("Wins: " + swStats.get("wins_solo_insane").getAsString() + "     Losses: "
						+ swStats.get("losses_solo_insane").getAsString());

				Fkd = swStats.get("kills_solo_insane").getAsFloat() / swStats.get("deaths_solo_insane").getAsFloat();
				Fwl = swStats.get("wins_solo_insane").getAsFloat() / swStats.get("losses_solo_insane").getAsFloat();
				kd = new BigDecimal(Fkd).setScale(2, RoundingMode.HALF_UP);
				wl = new BigDecimal(Fwl).setScale(2, RoundingMode.HALF_UP);

				if (kd.floatValue() > 1f) {
					kdString = "\u00A72" + kd.toString() + "\u00A7f";
				} else {
					kdString = "\u00A74" + kd.toString() + "\u00A7f";
				}
				if (wl.floatValue() > 0.4f) {
					wlString = "\u00A72" + wl.toString() + "\u00A7f";
				} else {
					wlString = "\u00A74" + wl.toString() + "\u00A7f";
				}

				returnStats.add("K/D: " + kdString + "      Win/Loss: " + wlString);
				break;

			case "BEDWARS":
			case "BEDWARS_FOUR_THREE": // TODO for now, these are stacked like this so more games work.
			case "BEDWARS_EIGHT_ONE":
			case "BEDWARS_FOUR_FOUR":
			case "BEDWARS_EIGHT_TWO":
				returnStats.add("BedWars Level: \u00A79" + acStats.get("bedwars_level").getAsString()
						+ "\u00A7f     Coins: \u00A76" + bwStats.get("coins").getAsString());
				returnStats.add("Doubles Wins: \u00A75" + bwStats.get("eight_two_wins_bedwars").getAsString());
				returnStats.add("Kills: " + bwStats.get("eight_two_kills_bedwars").getAsString() + "     Deaths: "
						+ bwStats.get("eight_two_deaths_bedwars").getAsString());
				returnStats.add("Final Kills: " + bwStats.get("eight_two_final_kills_bedwars").getAsString()
						+ "     Final Deaths: " + bwStats.get("eight_two_final_deaths_bedwars").getAsString());

				Fkd = bwStats.get("eight_two_final_kills_bedwars").getAsFloat()
						/ bwStats.get("eight_two_final_deaths_bedwars").getAsFloat();
				Fwl = bwStats.get("eight_two_wins_bedwars").getAsFloat()
						/ bwStats.get("eight_two_losses_bedwars").getAsFloat();
				kd = new BigDecimal(Fkd).setScale(2, RoundingMode.HALF_UP);
				wl = new BigDecimal(Fwl).setScale(2, RoundingMode.HALF_UP);

				if (kd.floatValue() > 1f) {
					kdString = "\u00A72" + kd.toString() + "\u00A7f";
				} else {
					kdString = "\u00A74" + kd.toString() + "\u00A7f";
				}
				if (wl.floatValue() > 0.4f) {
					wlString = "\u00A72" + wl.toString() + "\u00A7f";
				} else {
					wlString = "\u00A74" + wl.toString() + "\u00A7f";
				}

				returnStats.add("Final K/D: " + kdString + "      Win/Loss: " + wlString);
				break;
			default:
				returnStats.add("you aren't in a supported game!");
				break;
			}
			swStats = null;
			bwStats = null;
			return returnStats;
		} catch (Exception e) {
			// e.printStackTrace();
			returnStats.add("No stats for this user were found!");
			return returnStats;
		}
	}
}
