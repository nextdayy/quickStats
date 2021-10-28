package com.nxtdelivery.quickStats.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.Reference;

import gg.essential.vigilance.*;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import net.minecraft.client.Minecraft;

public class GUIConfig extends Vigilant {
	public static boolean test = false;
	
	@Property(
	        type = PropertyType.SWITCH, name = "Enable",
	        description = "Enable/Disable the mod.",
	        category = "General"
	    )
	    public static boolean modEnabled = true;
	@Property(
	        type = PropertyType.SWITCH, name = "Automatic Game Detection",
	        description = "Enable/Disable auto game detection on Hypixel.",
	        category = "General", subcategory = "General"
	    )
	    public static boolean autoGame = true;
	@Property(
	        type = PropertyType.SWITCH, name = "Sound",
	        description = "Enable/Disable sound feedback of the mod on player detection.",
	        category = "General", subcategory = "General"
	    )
	    public static boolean doSound = true;
	@Property(
	        type = PropertyType.SWITCH, name = "Compatibility",
	        description = "Change how the automatic game utility works in an attempt to increase compatibility.",
	        category = "General", subcategory = "General"
	    )
	    public static boolean locrawComp = false;
	@Property(
	        type = PropertyType.SELECTOR, name = "Default Game",
	        description = "Game to show stats for if nothing else is found.",
	        category = "General", subcategory = "General",
	        options = {"Bedwars", "Skywars", "Duels"}
	    )
	    public static int defaultGame = 0;
	
	@Property(
	        type = PropertyType.TEXT,
	        name = "API Key",
	        description = "The API key used in the mod. Can be also automatically set by typing in /api new in-game.",
	        category = "General",
	        protectedText = true, subcategory = "API"
	    )
	    public static String apiKey = "none";

	@Property(
	        type = PropertyType.SWITCH,
	        name = "Automatically Get API Key",
	        description = "Automatically get the API Key from /api new.",
	        category = "General", subcategory = "API"
	    )
	    public static boolean autoGetAPI = true;
	    
	    
	@Property(
			type = PropertyType.SWITCH,
			name = "Send Update Messages",
			description = "Send update messages on startup if a new version is available.",
			category = "General", subcategory = "Updates"
	)
		    public static boolean sendUp = true;
	
	
	
	@Property(
	        type = PropertyType.SWITCH, name = "Debug",
	        description = "Enable/disable verbose logging to help with diagnostics.",
	        category = "Support", subcategory = "General"
	    )
	    public static boolean debugMode = false;
	
	@Property(
	        type = PropertyType.BUTTON, name = "Reset Defaults",
	        description = "Reset all values to their defaults.\n \u00A7cForcibly restarts your game!",
	        category = "Support", subcategory = "General"
	    ) 
		public static void reset() {
			try {
				FileWriter writer = new FileWriter("./config/quickStats.toml");
				writer.write("this was cleared so it will be reset on next restart.");
				writer.close();

				QuickStats.LOGGER.warn("config file was cleared. Please restart your game.");
			} catch (Exception e) {
				QuickStats.LOGGER.error("failed to clear config, " + e);
			}
			throw new NullPointerException("Clearing config file");
		}
	
	
	
	
	
	
	
	@Property(
	        type = PropertyType.BUTTON, name = "Load Window",
	        description = "Toggle opening of the window so you can see what you are changing.\n\u00A7ePress the button again to close.",
	        category = "Gui Settings"
	    ) 
		public static void testWin() {
			if (!test) {
				test = true;
				new GUIStats("nxtdaydelivery");
			} else {
				test = false;
			}
		}
	@Property(
	        type = PropertyType.BUTTON, name = "Reset Window",
	        description = "Reset the window to the default values for your current GUI scale.\nYou might need to reopen the GUI and\\or restart your game for it to update.",
	        category = "Gui Settings"
	    ) 
		public static void resetGUI() {
			switch (mc.gameSettings.guiScale) {
			case 0: // AUTO scale
				winMiddle = 67;
				winTop = 28;
				winBottom = 72;
				winWidth = 62;
				break;
			case 1: // SMALL
				winMiddle = 130;
				winTop = 50;
				winBottom = 145;
				winWidth = 112;
				break;
			case 2: // NORMAL
				winMiddle = 90;
				winTop = 50;
				winBottom = 115;
				winWidth = 82;
				break;
			case 3: // LARGE
				winMiddle = 90;
				winTop = 50;
				winBottom = 115;
				winWidth = 85;
				break;
			}
			bgColor = new Color(27, 27, 27, 200);
			progColor = new Color(22, 33, 245, 140);
		}

	@Property(
	        type = PropertyType.SWITCH, name = "Custom Window",
	        description = "Enable/Disable changing of the window size and position.\nPlease note this is currently is under development and may behave unexpectedly.",
	        category = "Gui Settings", subcategory = "Size"
	    )
	    public static boolean sizeEnabled = false;
	
	@Property(
	        type = PropertyType.SLIDER, name = "Window Width",
	        description = "Change the window width.",
	        category = "Gui Settings", subcategory = "Size",
	        min = 60, max = 150
	    )
	    public static int winWidth = 82;
	@Property(
	        type = PropertyType.SLIDER, name = "Window Top",
	        description = "Change the position of the top of the window.",
	        category = "Gui Settings", subcategory = "Size",
	        min = 5, max = 200
	    )
	    public static int winTop = 50;
	@Property(
	        type = PropertyType.SLIDER, name = "Window Bottom",
	        description = "Change the position of the bottom of the window.",
	        category = "Gui Settings", subcategory = "Size",
	        min = 50, max = 250
	    )
	    public static int winBottom = 115;
	@Property(
	        type = PropertyType.SLIDER, name = "Window Position",
	        description = "Change the position of the window.",
	        category = "Gui Settings", subcategory = "Size",
	        min = -150, max = 450
	    )
	    public static int winMiddle = 90;
	
	
	
	
	
	@Property(
	        type = PropertyType.COLOR, name = "Background Color",
	        description = "Change the color of the background",
	        category = "Gui Settings", subcategory = "Colors",
	        min = 1, max = 255
	    )
	    public static Color bgColor = new Color(27,27,27,200); 
	@Property(
	        type = PropertyType.COLOR, name = "Progress Bar Color",
	        description = "Change the color of the progress bar.",
	        category = "Gui Settings", subcategory = "Colors",
	        min = 1, max = 255
	    )
	    public static Color progColor = new Color(22, 33, 245,100); 
	@Property(
	        type = PropertyType.SWITCH, name = "Text Shadow",
	        description = "Render the text with a shadow on the GUI.",
	        category = "Gui Settings", subcategory = "Text"
	    )
	    public static boolean textShadow = false; 
	
	
	
	@Property(
			type = PropertyType.NUMBER,
	        name = "Keybind",
	        description = "Keybind of the mod.",
	        category = "General",
	        protectedText = true, subcategory = "Key", hidden = true
	    )
	public static int key = 34;
	
	public static GUIConfig INSTANCE = new GUIConfig();
	private static Minecraft mc = Minecraft.getMinecraft();

	public GUIConfig() {
		super(new File("./config/quickStats.toml"), "QuickStats (" + Reference.VERSION + ")");
		initialize();

		addDependency("winWidth", "sizeEnabled");
		addDependency("winTop", "sizeEnabled");
		addDependency("winBottom", "sizeEnabled");
		addDependency("winMiddle", "sizeEnabled");

	}
}
