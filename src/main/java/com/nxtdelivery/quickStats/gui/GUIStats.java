package com.nxtdelivery.quickStats.gui;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.api.ApiRequest;
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

import java.awt.*;

public class GUIStats extends Gui {
    private static final Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fr = mc.fontRendererObj;
    long systemTime = Minecraft.getSystemTime();
    Integer height, width, top, bottom, middle, halfWidth, seed, pad, padY, scaledX, scaledY, midX, midY;
    long frames, framesLeft, fifth, upperThreshold, lowerThreshold;
    Color progColor, bgColor, textColor;
    Float fontScale, percentComplete;
    String username, title;
    ApiRequest api;
    public static Integer guiScale;

    public GUIStats() {
        register();
    }

    public void showGUI(String user) {
        fr = mc.fontRendererObj;
        height = new ScaledResolution(mc).getScaledHeight();
        width = new ScaledResolution(mc).getScaledWidth();
        scaledX = width / 100;
        scaledY = height / 100;
        midX = width / 2;
        midY = height / 2;
        guiScale = mc.gameSettings.guiScale;
        systemTime = Minecraft.getSystemTime();
        frames = 5 * 60;
        framesLeft = ((long) GUIConfig.GUITime) * 60; // first number = delay before progress bar (def: 7)
        fifth = frames / 5;
        upperThreshold = frames - fifth;
        lowerThreshold = fifth;
        percentComplete = 0.0f;
        username = user;
        int xOffset;
        switch (GUIConfig.winPreset) {
            case 0:
                xOffset = 40;
                top = 50;
                bottom = 115;
                break;
            case 1:
                xOffset = -40;
                top = 50;
                bottom = 115;
                break;
            case 2:
                xOffset = 40;
                top = 450;
                bottom = 515;
                break;
            case 3:
                xOffset = -40;
                top = 450;
                bottom = 515;
                break;
            default:        // no null pointer exceptions for me
                xOffset = 40;
                top = 50;
                bottom = 116;
                break;
        }
        if (!GUIConfig.sizeEnabled) {
            middle = midX + (scaledX * xOffset);
            fontScale = 0.8f;
            halfWidth = 82;
            seed = (halfWidth * 2);
            pad = roundIntWithFloat(middle, 1.25f) - halfWidth - 10;
            padY = roundIntWithFloat(top, 1.25f) + 26;
            switch (guiScale) {         // TODO add color presets!
                case 0: // AUTO scale, I have no idea why it is so different
                    top = 28;
                    bottom = 70;
                    halfWidth = 62;
                    fontScale = 0.75f;
                    pad = 810;
                    padY = 100;
                    break;
                case 3: // LARGE
                    middle = midX + (scaledX * 37);
                    break;
            }
        }
        switch (GUIConfig.colorPreset) {
            case 1:         // essential
                this.progColor = new Color(14, 156, 91, 255);
                this.bgColor = new Color(22, 22, 24, 255);
                //GUIConfig.textColor = new Color(183, 185, 189, 255);
                break;
            case 2:         // red
                this.progColor = new Color(149, 0, 0, 150);
                this.bgColor = new Color(17, 17, 27, 200);
                //GUIConfig.textColor = new Color(53, 0, 1, 255);
                break;
            case 3:         // PINKULU
                this.progColor = new Color(247, 101, 163, 200);
                this.bgColor = new Color(255, 164, 182, 100);
                //GUIConfig.textColor = new Color(255, 164, 182, 255);
                break;
            default:
                this.progColor = GUIConfig.progColor;
                this.bgColor = GUIConfig.bgColor;
                this.textColor = GUIConfig.textColor;

        }

        if (QuickStats.locraw) {
            QuickStats.locraw = false;
            QuickStats.LocInst.send();
        }
        api = new ApiRequest(username);
        if (GUIConfig.doSound) {
            mc.thePlayer.playSound("minecraft:random.successful_hit", 1.0F, 1.0F);
        }
        /*if (GUIConfig.debugMode) {
            System.out.println("SCALE=" + guiScale + " HEIGHT=" + height + " WIDTH=" + width + " SCALEDX=" + scaledX + " SCALEDY=" + scaledY);
            System.out.println("MIDDLE=" + middle + " TOP=" + top + " BOTTOM=" + bottom + " WIDTH=" + halfWidth + " SCALE=" + fontScale + " PADX=" + pad + " PADY=" + padY + "  if this is in production build then feel free to hit me");
        }*/
        this.register();
    }

    @EventHandler()
    public void delete() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @EventHandler()
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
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


    private static int roundIntWithFloat(int number, float multiplier) {
        float tempF = number * multiplier;
        return (int) tempF;
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
            pad = roundIntWithFloat(middle, 1.25f) - halfWidth;
            padY = roundIntWithFloat(top, 1.25f) + 26;
            seed = halfWidth * 2;
            this.bgColor = GUIConfig.bgColor;
            this.textColor = GUIConfig.textColor;
            this.progColor = GUIConfig.progColor;
        }
        if (GUIConfig.colorPreset == 0) {
            this.progColor = GUIConfig.progColor;
            this.bgColor = GUIConfig.bgColor;
            this.textColor = GUIConfig.textColor;
        }


        int currentWidth = (int) (halfWidth * percentComplete);
        Gui.drawRect(middle - currentWidth, top, middle + currentWidth, bottom, this.bgColor.getRGB());

        if (percentComplete == 1.0F) {
            if (GUIConfig.test) {
                framesLeft = 100;
            }
            long length = upperThreshold - lowerThreshold;
            long current = framesLeft - lowerThreshold;
            float progress = 1F - clamp((float) current / (float) length);
            Gui.drawRect(middle - currentWidth, bottom - 2, (int) (middle - currentWidth + (seed * progress)), bottom,
                    this.progColor.getRGB()); // 128, 226, 126
            if (guiScale == 0) {
                GL11.glPushMatrix();
                GL11.glScalef(fontScale, fontScale, fontScale); // shrink font
                fontScale = 0.65f;
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
            if (api.timeOut) {
                title = "Request timed out!";
            }

            try {
                if (api.image != null) {
                    DynamicTexture dynamic = new DynamicTexture(api.image);
                    ResourceLocation location = mc.getTextureManager().getDynamicTextureLocation("quickstats/user", dynamic);
                    mc.getTextureManager().bindTexture(location);
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    if (guiScale != 0) {
                        drawModalRectWithCustomSizedTexture(middle - halfWidth + 3, top + 4, 0, 0, 14, 14, 14, 14);
                    } else {
                        drawModalRectWithCustomSizedTexture(roundIntWithFloat(middle - halfWidth + 3, 1.54f), top + 18, 0, 0, 14, 14, 14, 14);
                    }
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
                    fr.drawStringWithShadow(title, roundIntWithFloat(middle - halfWidth + 20, 1.52f), top + 24, -1);
                } else {
                    fr.drawString(title, roundIntWithFloat(middle - halfWidth + 20, 1.52f), top + 24, -1);
                }
                GL11.glScalef(fontScale, fontScale, fontScale);
            }
            String resultMsg;
            try {
                for (int i = 0; i < api.result.size(); i++) {
                    QuickStats.LOGGER.debug(api.result.get(i));
                    resultMsg = api.result.get(i);
                    if (GUIConfig.textShadow) {
                        fr.drawStringWithShadow(resultMsg, pad, (10 * i) + padY, this.textColor.getRGB());
                    } else {
                        fr.drawString(resultMsg, pad, (10 * i) + padY, this.textColor.getRGB());
                    }
                }
            } catch (Exception e) {
                //if(GUIConfig.debugMode) {e.printStackTrace();}
            }
            GL11.glPopMatrix();
        }

    }

}
