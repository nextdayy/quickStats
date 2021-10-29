package com.nxtdelivery.quickStats.gui;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.api.ApiRequest;
import com.nxtdelivery.quickStats.util.LocrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class GUIStats extends Gui {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final FontRenderer fr = mc.fontRendererObj;
    long systemTime = Minecraft.getSystemTime();
    ScaledResolution resolution = new ScaledResolution(mc);
    Integer height, width, top, bottom, middle, halfWidth, seed, pad;
    long frames, framesLeft, fifth, upperThreshold, lowerThreshold;
    Float fontScale, percentComplete;
    String username, title;
    ApiRequest api;
    public static Integer guiScale;

    public GUIStats(String user) {
        height = resolution.getScaledHeight();
        width = resolution.getScaledWidth();
        guiScale = mc.gameSettings.guiScale;
        frames = 5 * 60;
        framesLeft = 7 * 60; // first number = delay before progress bar (def: 7)
        fifth = frames / 5;
        upperThreshold = frames - fifth;
        lowerThreshold = fifth;
        percentComplete = 0.0f;
        username = user;
        if (!GUIConfig.sizeEnabled) {
            switch (guiScale) {
                case 0: // AUTO scale
                    middle = width - 67;
                    top = 28;
                    bottom = 72;
                    halfWidth = 62;
                    fontScale = 0.75f;
                    pad = middle + 373; // text padding
                    break;
                case 1: // SMALL
                    middle = width - 130;
                    top = 50;
                    bottom = 145;
                    halfWidth = 112;
                    fontScale = 0.8f;
                    pad = middle + 314; // text padding
                    break;
                case 2: // NORMAL
                    middle = width - 90; // position of window, smaller number = closer to edge
                    top = 50; // top of window
                    bottom = 115; // bottom of window
                    halfWidth = 82; // width of window, larger number is larger window
                    fontScale = 0.8f; // font size
                    pad = middle + 119; // text padding
                    break;
                case 3: // LARGE
                    middle = width - 90;
                    top = 50;
                    bottom = 115;
                    halfWidth = 85;
                    fontScale = 0.8f;
                    pad = middle + 35; // text padding
                    break;
            }
        }
        this.run();
    }

    @EventHandler()
    public void delete() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @EventHandler()
    public void run() {
        MinecraftForge.EVENT_BUS.register(this);
        if (QuickStats.locraw) {
            QuickStats.locraw = false;
            LocrawUtil locrawUtil = new LocrawUtil();
            locrawUtil.register();
        }
        api = new ApiRequest(username);
        if (GUIConfig.doSound) {
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
        }


    }

    private static float clamp(float number) {
        return number < (float) 0.0 ? (float) 0.0 : Math.min(number, (float) 1.0);
    }

    private static float easeOut(float current, float goal) {
        if (Math.floor(Math.abs(goal - current) / (float) 0.01) > 0) {
            return current + (goal - current) / (float) 15.0;
        } else {
            return goal;
        }
    }

    @SubscribeEvent
	/*
	  Some of this code was taken from PopupEvents by Sk1er Club under GNU License.
	  This math logic is used to render the window smoothly. All thanks to them,
	  this window can render nice and smoothly!
	 */
    public void renderEvent(TickEvent.RenderTickEvent event) {
        if (framesLeft <= 0) {
            return;
        }

        while (systemTime < Minecraft.getSystemTime() + (1000 / 60)) {
            framesLeft--;
            systemTime += (1000 / 60);
        }
        percentComplete = clamp(easeOut(percentComplete,
                framesLeft < lowerThreshold ? 0.0f : framesLeft > upperThreshold ? 1.0f : framesLeft));

        if (GUIConfig.sizeEnabled) {
            middle = width - GUIConfig.winMiddle;
            top = GUIConfig.winTop;
            bottom = GUIConfig.winBottom;
            halfWidth = GUIConfig.winWidth;
            fontScale = 0.8f;
            pad = middle + 119;
        }

        seed = (halfWidth * 2);

        int currentWidth = (int) (halfWidth * percentComplete);
        Gui.drawRect(middle - currentWidth, top, middle + currentWidth, bottom, GUIConfig.bgColor.getRGB());

        if (percentComplete == 1.0F) {
            if (GUIConfig.test) {
                framesLeft = 100;
            }
            long length = upperThreshold - lowerThreshold;
            long current = framesLeft - lowerThreshold;
            float progress = 1F - clamp((float) current / (float) length);
            Gui.drawRect(middle - currentWidth, bottom - 2, (int) (middle - currentWidth + (seed * progress)), bottom,
                    GUIConfig.progColor.getRGB()); // 128, 226, 126
            if (guiScale == 0) {
                GL11.glPushMatrix();
                GL11.glScalef(fontScale, fontScale, fontScale); // shrink font
                fontScale = 0.6f;
            }
            title = api.formattedName;
            if (api.formattedName == null) {
                title = "Loading...";
            }
            if (api.noUser) {
                title = "User not found!";
            }
            if (api.generalError) {
                title = "An error occurred!";
            }
            if (api.noAPI) {
                title = "No valid API key!";
            }


            try {
                if (api.image != null) {
                    DynamicTexture dynamic = new DynamicTexture(api.image);
                    ResourceLocation location = mc.getTextureManager().getDynamicTextureLocation("quickstats/user", dynamic);
                    mc.getTextureManager().bindTexture(location);
                    GlStateManager.color(1F, 1F, 1F, 1f);
                    drawModalRectWithCustomSizedTexture(middle - halfWidth + 3, top + 4, 0, 0, 14, 14, 14, 14);
                }
            } catch (Exception e) {
                if (GUIConfig.debugMode) {
                    e.printStackTrace();
                }
            }

            if (guiScale != 0) {
                if (GUIConfig.textShadow) {
                    fr.drawStringWithShadow(title, middle - halfWidth + 20, top + 8, -1);
                } else {
                    fr.drawString(title, middle - halfWidth + 20, top + 8, -1);
                }
                GL11.glPushMatrix();
                GL11.glScalef(fontScale, fontScale, fontScale); // shrink font
            }
            if (guiScale == 0) {
                if (GUIConfig.textShadow) {
                    fr.drawStringWithShadow(title, 521, 43, -1);
                } else {
                    fr.drawString(title, 551 - fr.getStringWidth(title) / 2, 43, -1);
                }
                GL11.glScalef(fontScale, fontScale, fontScale);
            }
            String resultMsg;
            try {
                for (int i = 0; i < api.result.size(); i++) {
                    QuickStats.LOGGER.debug(api.result.get(i));
                    resultMsg = api.result.get(i);
                    if (GUIConfig.textShadow) {
                        fr.drawStringWithShadow(resultMsg, pad, (10 * i) + top + 40, -1);
                    } else {
                        fr.drawString(resultMsg, pad, (10 * i) + top + 40, -1);
                    }
                }
            } catch (Exception e) {
                //if(GUIConfig.debugMode) {e.printStackTrace();}
            }
            GL11.glPopMatrix();
        }

    }

}
