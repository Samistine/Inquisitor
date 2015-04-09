/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Utils {

    public static final Logger logger = Logger.getLogger("Minecraft");

    public static void info(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.INFO, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void warning(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.WARNING, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void severe(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.SEVERE, String.format("[%s] %s", Global.pluginName, msg));
    }

    public static void severe(Throwable t, String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.SEVERE, String.format("[%s] %s", Global.pluginName, msg), t);
    }

    public static void debug(String msg, Object ... args) {
        if (! Config.getDebug()) return;
        if (args.length > 0)
            msg = String.format(msg, args);
        msg = ChatColor.stripColor(msg);
        if (msg.isEmpty()) return;
        logger.log(Level.INFO, String.format("[%s] (DEBUG) %s", Global.pluginName, msg));
    }

    public static boolean copyFileFromJar(String resPath, File dstFolder, boolean overwriteIfOlder) {
//System.out.println(String.format("copyFileFromJar(%s, %s, %s)", resPath, dstFolder.getAbsolutePath(), overwriteIfOlder));
        File dstFile;
        int pos = resPath.lastIndexOf('/');
        if (pos != -1)
            dstFile = new File(dstFolder, resPath.substring(pos + 1));
        else
            dstFile = new File(dstFolder, resPath);
        if (dstFile.exists()) {
// System.out.println(dstFile + " exists");
            if (! overwriteIfOlder) return false;
            try {
                File jarFile = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//System.out.println(jarFile.lastModified() + " <= " + dstFile.lastModified());
                if (jarFile.lastModified() <= dstFile.lastModified()) return false;
//System.out.println(dstFile + " will overwrite");
            } catch (URISyntaxException e) {}
        }
        if (! dstFolder.exists())
            dstFolder.mkdirs();
        try {
            ReadableByteChannel ic = Channels.newChannel(Utils.class.getResourceAsStream(resPath));
            FileChannel oc = new FileOutputStream(dstFile).getChannel();
            oc.transferFrom(ic, 0, 1 << 24);
            ic.close();
            oc.close();
        } catch (IOException ioe) {
            warning("IOException while copying %s to %s: %s", resPath, dstFile, ioe.getMessage());
            return false;
        }
        return true;
    }

    public static boolean installManifest(String manifestPath, File dstFolder, boolean overwriteIfOlder) {
//System.out.println(String.format("installManifest(%s, %s, %s)", manifestPath, dstFolder.getAbsolutePath(), overwriteIfOlder));
        boolean installed = false;
        if (! dstFolder.exists()) {
            dstFolder.mkdirs();
            installed = true;
        }
        InputStream is = Utils.class.getResourceAsStream(manifestPath);
        if (is == null) {
            severe("unknown manifest file '%s'", manifestPath);
            return false;
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean deleting;
        try {
            while ((line = r.readLine()) != null) {
                line = line.replaceAll("^\\s+|\\s+$|\\s*#.*", "");
                if (line.length() == 0) continue;

                if (line.startsWith("del ")) {
                    deleting = true;
                    line = line.substring(4).trim();
                } else
                    deleting = false;

                if (line.endsWith("/")) {
                    int pos = line.lastIndexOf("/", line.length() - 2);
                    String subFolderName;
                    if (pos == -1)
                        subFolderName = line.substring(0, line.length() - 1);
                    else
                        subFolderName = line.substring(pos + 1, line.length() - 1);
                    if (deleting)
                        installed = deleteFolder(new File(dstFolder, subFolderName)) || installed;
                    else {
                        line += "manifest";
                        File subFolder = new File(dstFolder, subFolderName);
                        installed = installManifest(line, subFolder, overwriteIfOlder) || installed;
                    }
                } else if (deleting) {
                    File dstFile;
                    int pos = line.lastIndexOf('/');
                    if (pos != -1)
                        dstFile = new File(dstFolder, line.substring(pos + 1));
                    else
                        dstFile = new File(dstFolder, line);
                    if (! dstFile.exists()) continue;
                    installed = dstFile.delete() || installed;
                } else
                    installed = copyFileFromJar(line, dstFolder, overwriteIfOlder) || installed;
            }
        } catch (IOException ioe) {}
        return installed;
    }

    public static boolean deleteFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (! deleteFolder(file)) return false;
            } else
                file.delete();
        }
        return folder.delete();
    }

    public static <T extends Enum<T>> T valueOf(Class<T> cls, String s) {
        if ((s == null) || s.isEmpty() || s.equals("*") || s.equals("-")) return null;
        try {
            return Enum.valueOf(cls, s);
        } catch (IllegalArgumentException e) {}
        s = s.toLowerCase();
        T theOne = null;
        for (T value : cls.getEnumConstants()) {
            if (value.toString().toLowerCase().equals(s))
                return value;
            if (value.toString().toLowerCase().startsWith(s)) {
                if (theOne == null)
                    theOne = value;
                else
                    throw new IllegalArgumentException("ambiguous");
            }
        }
        if (theOne == null)
            throw new IllegalArgumentException("invalid");
        return theOne;
    }

    public static String titleCase(String in) {
        StringBuilder sb = new StringBuilder(in);
        boolean cap = true;
        for (int i = 0; i < sb.length(); i++) {
            char ch = sb.charAt(i);
            if (ch == '_') {
                sb.setCharAt(i, ' ');
                cap = true;
                continue;
            }
            if (cap) {
                if (! Character.isTitleCase(ch)) {
                    sb.setCharAt(i, Character.toTitleCase(ch));
                    cap = false;
                }
                continue;
            }
            if (! Character.isLowerCase(ch))
                sb.setCharAt(i, Character.toLowerCase(ch));
        }
        return sb.toString();
    }

    public static int fire(Runnable run) {
        if (! Global.enabled) return -1;
        return Global.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Global.plugin, run);
    }

    // delay is millis
    public static int fireDelayed(Runnable run, long delay) {
        if (! Global.enabled) return -1;
        long ticks = delay / 50;
        return Global.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Global.plugin, run, ticks);
    }

    public static int worker(Runnable run) {
        if (! Global.enabled) return -1;
        return Global.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Global.plugin, run);
    }

    // delay is millis
    public static int workerDelayed(Runnable run, long delay) {
        if (! Global.enabled) return -1;
        long ticks = delay / 50;
        return Global.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Global.plugin, run, ticks);
    }

    public static String normalizeEntityTypeName(EntityType type) {
        switch (type) {
            case MUSHROOM_COW:
                return "MOOSHROOM";
            default:
                return type.getName();
        }
    }

    public static boolean downloadFile(URL url, File dst, boolean warn) {
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setInstanceFollowRedirects(true);
            if (conn.getResponseCode() != 200) {
                if (warn)
                    warning("Unable to download %s: %s %s", url, conn.getResponseCode(), conn.getResponseMessage());
                return false;
            }
            debug("downloading %s", url);
            ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
            FileOutputStream os = new FileOutputStream(dst);
            os.getChannel().transferFrom(rbc, 0, 1 << 24);
            rbc.close();
            os.close();
            return true;
        } catch (IOException ioe) {
            if (warn)
                warning("Unable to download %s: %s", url, ioe.getMessage());
            return false;
        }
    }

    public static void checkVersion() {
        try {
            String urlStr = "http://code.google.com/p/inquisitor-mc/wiki/VERSION";
            URL url = new URL(urlStr);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setInstanceFollowRedirects(true);
            int statusCode = http.getResponseCode();
            if (statusCode != 200) {
                debug("got status %s during plugin version check", statusCode);
                http.disconnect();
                return;
            }
            InputStreamReader isr = new InputStreamReader(http.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String content;
            StringBuilder sb = new StringBuilder();
            while ((content = br.readLine()) != null)
                sb.append(content);
            br.close();

            int myVersion = versionToInt(Global.pluginVersion);
            boolean iAmBeta = isBetaVersion(Global.pluginVersion);

            Pattern pattern = Pattern.compile("RELEASE:(.+):RELEASE");
            Matcher matcher = pattern.matcher(sb);
            if (! matcher.find()) {
                debug("couldn't find release version string!");
                return;
            }
            String releaseVersionStr = matcher.group(1);
            int releaseVersion = versionToInt(releaseVersionStr);

            pattern = Pattern.compile("BETA:(.+):BETA");
            matcher = pattern.matcher(sb);
            String betaVersionStr = matcher.find() ? matcher.group(1) : null;
            int betaVersion = versionToInt(releaseVersionStr);

            if (releaseVersionStr.equals(Global.pluginVersion)) {
                debug("plugin is current");
                return;
            }

            String upgradeVersion;

            if (iAmBeta) {
                if (releaseVersion > myVersion)
                    upgradeVersion = releaseVersionStr;
                else if (betaVersion > myVersion)
                    upgradeVersion = betaVersionStr;
                else {
                    debug("beta version is current");
                    return;
                }
            } else {
                if (releaseVersion > myVersion)
                    upgradeVersion = releaseVersionStr;
                else {
                    debug("release version is current");
                    return;
                }
            }
            info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            info("!! This is an outdated version of %s.", Global.pluginName);
            info("!! Please upgrade to %s.", upgradeVersion);
            info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (UnsupportedEncodingException uee) {
        } catch (MalformedURLException mue) {
        } catch (IOException ioe) {
        }
    }

    private static boolean isBetaVersion(String version) {
        return version.contains("beta");
    }

    private static int versionToInt(String version) {
        if (version == null) return 0;
        Matcher matcher = Pattern.compile("v?(\\d+)\\.(\\d+)(?:beta)?(\\d+)?").matcher(version);
        if (! matcher.find()) return 0;

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int beta = (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : 0;

        if (beta > 0) {
            minor--;
            if (minor < 0) {
                minor = 99;
                major--;
            }
        }
        int v = (major * 10000) + (minor * 100) + beta;
        return v;
    }

}
