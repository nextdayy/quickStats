package com.nxtdelivery.quickStats.util;

import com.nxtdelivery.quickStats.QuickStats;
import com.nxtdelivery.quickStats.Reference;
import com.nxtdelivery.quickStats.gui.GUIConfig;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class AuthChecker {
    public static boolean mismatch = false;
    private static String url;
    private static String hash;

    public static void checkAuth(@NotNull String filename) {
        if (GUIConfig.securityLevel == 0) {
            return;
        }
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytesBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            hash = convertByteArrayToHexString(hashedBytes);
            url = "https://raw.githubusercontent.com/nxtdaydelivery/quickStats/master/hashes/" + Reference.NAME + "-v" + Reference.VERSION + "_hash.sha256";
            String expectedHash = new BufferedReader(new InputStreamReader(new URL(url).openStream())).readLine();
            if (GUIConfig.debugMode) {
                QuickStats.LOGGER.debug("Generated hash: " + hash + " from file (this) " + filename);
                QuickStats.LOGGER.debug("Fetched hash " + expectedHash + " from URL " + url);
            }
            if (!hash.equals(expectedHash)) {
                QuickStats.LOGGER.error("Mismatch in the provided hashes. This could mean that the mod has been modified.");
                QuickStats.LOGGER.error("Local hash that was expected: " + hash + "\tHash that was received from mirror: " + expectedHash);
                QuickStats.LOGGER.error("Your account information could be at risk to attackers who modified this code!");
                QuickStats.LOGGER.error("Make sure that you downloaded the mod from the OFFICIAL mirror, and try again. " + Reference.URL);
                mismatch = true;
                if (GUIConfig.securityLevel == 3) {
                    throw new ReportedException(new CrashReport("[QuickStats] Mod hash mismatch. Game start halted.", new SecurityException("Hash mismatch of the mod. Game was halted startup to prevent data theft.")));
                }
            } else {
                QuickStats.LOGGER.info("Hashes match for version. Continuing normally.");
            }
        } catch (Exception e) {
            if (e instanceof MalformedURLException || e instanceof FileNotFoundException) {
                QuickStats.LOGGER.error("hash file doesn't exist on mirror! Skipping!");
                if (GUIConfig.debugMode) {
                    QuickStats.LOGGER.warn("---- Debug Information ----");
                    QuickStats.LOGGER.warn("Expected there to be a hash at: " + url);
                    QuickStats.LOGGER.warn("This file's hash: " + hash);
                }
                return;
            }
            e.printStackTrace();
            QuickStats.LOGGER.error("failed to generate and check hash of mod.");
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}
