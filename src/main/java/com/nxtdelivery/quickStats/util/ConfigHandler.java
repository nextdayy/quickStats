package com.nxtdelivery.quickStats.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import com.google.gson.JsonObject;
import com.nxtdelivery.quickStats.QuickStats;

import net.minecraftforge.fml.common.Loader;

public class ConfigHandler {
	static File configMain = new File(Loader.instance().getConfigDir(), "QuickStats.properties");
	public static boolean modEnabled;
	public static boolean firstStart = false;
	public static boolean fileCorrupt = false;
	public static String apiKey;
	public static int key;

	public static void ConfigLoad() {
		if (configMain.exists() == true) {
			FileReader reader;
			try {
				reader = new FileReader(configMain);
				Properties prop = new Properties();
				prop.load(reader);

				modEnabled = Boolean.parseBoolean(prop.getProperty("enabled"));
				if (modEnabled == true) {
					QuickStats.LOGGER.debug("State read from config is ENABLED");
				}
				if (modEnabled == false) {
					QuickStats.LOGGER.warn("State read from config is DISABLED. Keybinds wont work.");
				}
				apiKey = prop.getProperty("apiKey");
				QuickStats.LOGGER.debug("got api key from config: " + apiKey);

				key = Integer.parseInt(prop.getProperty("key"));
				QuickStats.LOGGER.debug("Key read from config is " + key);
				reader.close();
				QuickStats.LOGGER.info("Config read complete");
			} catch (FileNotFoundException e) {
				createConfig();
			} catch (IOException e) {
				QuickStats.LOGGER.error(e);
			} catch (Exception e) {
				createConfig();
				QuickStats.LOGGER.error(e);
				fileCorrupt = true;
			}

		} else {
			QuickStats.LOGGER.warn("Config file does exist. assuming first startup.");
			firstStart = true;
			createConfig();
		}
	}

	private static void createConfig() {
		try {
			try {
				configMain.createNewFile();
			} catch (Exception e) {
				QuickStats.LOGGER.error(e);
			}
			QuickStats.LOGGER.info("Generating new config file...");
			FileWriter writer = new FileWriter(configMain);
			Properties prop = new Properties();
			prop.setProperty("enabled", "true");
			prop.setProperty("apiKey", "none");
			prop.setProperty("key", "34");
			prop.store(writer, "QuickStats configuration");
			writer.close();
			QuickStats.LOGGER.info("Config file created");
			ConfigLoad();
		} catch (IOException e) {
			QuickStats.LOGGER.error(e);
		}
	}

	public static void writeConfig(String type, String data) {
		try {
			FileWriter writer = new FileWriter(configMain);
			Properties prop = new Properties();
			QuickStats.LOGGER.debug("attempting to write new data to config file");
			if (type == "enabled") {
				prop.setProperty(type, data);
				prop.setProperty("apiKey", apiKey);
			}
			if (type == "apiKey") {
				prop.setProperty("enabled", Boolean.toString(modEnabled));
				prop.setProperty(type, data);
			}
			if (type == "key") {
				prop.setProperty("enabled", Boolean.toString(modEnabled));
				prop.setProperty("apiKey", apiKey);
				prop.setProperty(type, data);
			}
			prop.store(writer, "QuickStats configuration");
			writer.close();
			QuickStats.LOGGER.info("config written successfully. reloading config...");
			ConfigLoad();
		} catch (NullPointerException e) {
			QuickStats.LOGGER.error(e);
		} catch (Exception e) {
			QuickStats.LOGGER.error(e);
		}
	}
}
