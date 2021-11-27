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
    Integer height, width, top, bottom, middle, halfWidth, fullWidth, pad, padY, scaledX, scaledY, midX, midY, frame, progFrame, currentWidth, fullHeight;
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
        frame = 0;
        progFrame = 0;
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
            default:
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
        }
        if (!GUIConfig.sizeEnabled) {
            middle = midX + (scaledX * xOffset);
            fontScale = 0.8f;
            halfWidth = 82;
            fullWidth = (halfWidth * 2);
            fullHeight = bottom - top;
            pad = roundIntWithFloat(middle, 1.25f) - halfWidth - 10;
            padY = roundIntWithFloat(top, 1.25f) + 26;
            switch (guiScale) {
                case 0: // AUTO scale, I have no idea why it is so different
                    top = 28;
                    bottom = 70;
                    halfWidth = 62;
                    fontScale = 0.75f;
                    pad = 810;
                    padY = 100;
                    break;
                case 3: // LARGE
                    middle = midX + (scaledX * (xOffset - 3));
                    break;
            }
        }
        switch (GUIConfig.colorPreset) {
            case 1:         // essential
                this.progColor = new Color(14, 156, 91, 255);
                this.bgColor = new Color(22, 22, 24, 255);
                this.textColor = new Color(255, 255, 255, 255);
                break;
            case 2:         // red
                this.progColor = new Color(149, 0, 0, 150);
                this.bgColor = new Color(17, 17, 27, 200);
                this.textColor = new Color(53, 0, 1, 255);
                break;
            case 3:         // PINKULU
                this.progColor = new Color(247, 101, 163, 200);
                this.bgColor = new Color(255, 164, 182, 100);
                this.textColor = new Color(255, 164, 182, 255);
                break;
            case 4:         // transparent
                this.bgColor = new Color(50, 50, 50, 30);
                this.progColor = new Color(43, 43, 43, 40);
                this.textColor = new Color(255, 255, 255, 220);
                break;
            case 5:         // white theme
                this.progColor = new Color(0, 0, 0, 110);
                this.bgColor = new Color(255, 255, 255, 73);
                this.textColor = new Color(241, 241, 241, 255);
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
        //if (GUIConfig.debugMode) {
        //    QuickStats.LOGGER.info("---- Window Debug Information ----");
        //    QuickStats.LOGGER.info("SCALE=" + guiScale + " HEIGHT=" + height + " WIDTH=" + width + " SCALEDX=" + scaledX + " SCALEDY=" + scaledY);
        //    QuickStats.LOGGER.info("MIDDLE=" + middle + " TOP=" + top + " BOTTOM=" + bottom + " WIDTH=" + halfWidth + " SCALE=" + fontScale + " PADX=" + pad + " PADY=" + padY);
        //}
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

    /**
     * This math function was taken from PopupEvents by Sk1er LLC under GNU General License.
     * This code is used to add smooth animations to the window.
     *
     * @param number that needs to be clamped into range of 0 to 1
     * @return clamped number
     */
    private static float clamp(float number) {
        return number < (float) 0.0 ? (float) 0.0 : Math.min(number, (float) 1.0);
    }

    /**
     * This math function was taken from PopupEvents by Sk1er LLC under GNU General License.
     * This code is used to add smooth animations to the window.
     *
     * @param current the current number in question
     * @param goal    the target number
     * @return integer number
     */
    private static float easeOut(float current, float goal) {
        if (Math.floor(Math.abs(goal - current) / (float) 0.01) > 0) {
            return current + (goal - current) / (float) 15.0;
        } else {
            return goal;
        }
    }

    /**
     * Multiply an Integer with a float value, then round it back to an Integer
     *
     * @param number     the Integer that needs to be multiplied
     * @param multiplier the Float that will multiply the Integer
     * @return the value multiplied then rounded back to an Integer.
     */
    private static int roundIntWithFloat(int number, float multiplier) {
        float tempF = number * multiplier;
        return (int) tempF;
    }

    @SubscribeEvent
    public void renderEvent(TickEvent.RenderTickEvent event) {
        if (framesLeft <= 0) {
            return;
        }
        if (GUIConfig.sizeEnabled) {
            middle = width - GUIConfig.winMiddle;
            top = GUIConfig.winTop;
            bottom = GUIConfig.winBottom;
            halfWidth = GUIConfig.winWidth;
            fontScale = 0.8f;
            pad = roundIntWithFloat(middle, 1.25f) - halfWidth;
            padY = roundIntWithFloat(top, 1.25f) + 26;
            fullWidth = halfWidth * 2;
            fullHeight = bottom - top;
            this.bgColor = GUIConfig.bgColor;
            this.textColor = GUIConfig.textColor;
            this.progColor = GUIConfig.progColor;
        }
        if (GUIConfig.colorPreset == 0) {
            this.progColor = GUIConfig.progColor;
            this.bgColor = GUIConfig.bgColor;
            this.textColor = GUIConfig.textColor;
        }


        while (systemTime < Minecraft.getSystemTime() + (1000 / 60)) {
            if (progFrame == GUIConfig.framesToSkipP) {
                framesLeft--;
                progFrame = -1;
            }
            progFrame++;
            systemTime += (1000 / 60);
        }
        if (frame == GUIConfig.framesToSkip) {
            frame = 0;
            percentComplete = clamp(easeOut(percentComplete,
                    framesLeft < lowerThreshold ? 0.0f : framesLeft > upperThreshold ? 1.0f : framesLeft));
        } else {
            frame++;
        }
        switch (GUIConfig.animationPreset) {
            default:
            case 0:
                currentWidth = (int) (halfWidth * percentComplete);
                Gui.drawRect(middle - currentWidth, top, middle + currentWidth, bottom, this.bgColor.getRGB());
                break;
            case 1:
                int rightSide = middle + halfWidth;
                currentWidth = (int) (rightSide - fullWidth * percentComplete);
                Gui.drawRect(currentWidth, top, rightSide, bottom, this.bgColor.getRGB());
                break;
            case 2:
                int leftSide = middle - halfWidth;
                currentWidth = (int) (leftSide + fullWidth * percentComplete);
                Gui.drawRect(leftSide, top, currentWidth, bottom, this.bgColor.getRGB());
                break;
            case 3:
                currentWidth = (int) (halfWidth * percentComplete);
                int currentTop = (int) (top * percentComplete);
                int currentBottom = (int) (bottom * percentComplete);
                Gui.drawRect(middle - currentWidth, currentTop, middle + currentWidth, currentBottom, this.bgColor.getRGB());
                break;

        }

        if (percentComplete == 1.0F) {
            if (GUIConfig.test) {
                framesLeft = 100;
            }
            long length = upperThreshold - lowerThreshold;
            long current = framesLeft - lowerThreshold;
            float progress = 1F - clamp((float) current / (float) length);
            Gui.drawRect(middle - halfWidth, bottom - 2, (int) (middle - halfWidth + (fullWidth * progress)), bottom,
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
            if (api.slowDown) {
                title = "You're requesting too fast!";
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
